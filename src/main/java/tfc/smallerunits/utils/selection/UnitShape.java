package tfc.smallerunits.utils.selection;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import tfc.smallerunits.UnitEdge;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.mixin.optimization.VoxelShapeAccessor;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

// best of 1.18 and 1.12, neat
public class UnitShape extends VoxelShape {
	public final boolean visual;
	protected final ArrayList<UnitBox> boxesTrace = new ArrayList<>();
	protected final ArrayList<UnitBox> boxesCollide = new ArrayList<>();
	
	public final UnitSpace space;
	protected AABB totalBB = null;
	protected Vec3 offset = new Vec3(0, 0, 0);
	
	public CollisionContext collisionContext;
	
	public UnitShape(UnitSpace space, boolean visual, CollisionContext pContext) {
		super(new UnitDiscreteShape(0, 0, 0));
		((UnitDiscreteShape) ((VoxelShapeAccessor) this).getShape()).sp = this;
		this.space = space;
		this.visual = visual;
		this.collisionContext = pContext;
	}
	
	private static double swivelOffset(AxisCycle axiscycle, AABB pCollisionBox, AABB box, double offsetX) {
		Direction.Axis xSwivel = axiscycle.cycle(Direction.Axis.X);
		Direction.Axis ySwivel = axiscycle.cycle(Direction.Axis.Y);
		Direction.Axis zSwivel = axiscycle.cycle(Direction.Axis.Z);
		
		double tMaxX = box.max(xSwivel);
		double tMinX = box.min(xSwivel);
		double tMaxY = box.max(zSwivel);
		double tMinY = box.min(zSwivel);
		double tMinZ = box.min(ySwivel);
		double tMaxZ = box.max(ySwivel);
		double oMaxY = pCollisionBox.max(zSwivel);
		double oMaxX = pCollisionBox.max(xSwivel);
		double oMinX = pCollisionBox.min(xSwivel);
		double oMaxZ = pCollisionBox.max(ySwivel);
		double oMinZ = pCollisionBox.min(ySwivel);
		double oMinY = pCollisionBox.min(zSwivel);
		if (oMaxY > tMinY && oMinY < tMaxY && oMaxZ > tMinZ && oMinZ < tMaxZ) {
			// due to the fact that I'm scaling the bounding box, I end up losing some precision
			// because of this, I have to  use a more lenient check
			if (offsetX > 0.0D && oMaxX <= (tMinX + 0.000001)) {
				double deltaX = tMinX - oMaxX;
				
				if (deltaX < offsetX) return deltaX;
//			} else if (offsetX < 0.0D && oMinX >= (tMaxX - 0.000001)) {
			} else if (offsetX < 0.0D && oMinX >= (tMaxX - 0.000001)) {
				double deltaX = tMaxX - oMinX;
				
				if (deltaX > offsetX) return deltaX;
			}
		}
		return offsetX;
	}
	
	@Override
	public void forAllEdges(Shapes.DoubleLineConsumer pAction) {
		for (AABB box : boxesTrace) {
			pAction.consume(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
			pAction.consume(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
			pAction.consume(box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ);
			
			pAction.consume(box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
			pAction.consume(box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
			pAction.consume(box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ);
			
			pAction.consume(box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ);
			pAction.consume(box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ);
			pAction.consume(box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);
			
			pAction.consume(box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
			pAction.consume(box.maxX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ);
			pAction.consume(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
		}
	}
	
	@Override
	public void forAllBoxes(Shapes.DoubleLineConsumer pAction) {
		for (AABB box : boxesTrace) {
			pAction.consume(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		}
	}
	
	@Override
	protected DoubleList getCoords(Direction.Axis pAxis) {
		// TODO: cache
		DoubleArrayList arrayList = new DoubleArrayList();
		for (AABB box : boxesTrace) {
			arrayList.add(box.min(pAxis));
			arrayList.add(box.max(pAxis));
		}
		return arrayList;
	}
	
	private static boolean swivelCheck(AxisCycle axiscycle, AABB pCollisionBox, AABB box) {
		Direction.Axis ySwivel = axiscycle.cycle(Direction.Axis.Y);
		Direction.Axis zSwivel = axiscycle.cycle(Direction.Axis.Z);
		
		double tMaxY = box.max(zSwivel);
		double tMinY = box.min(zSwivel);
		double tMinZ = box.min(ySwivel);
		double tMaxZ = box.max(ySwivel);
		double oMaxY = pCollisionBox.max(zSwivel);
		double oMaxZ = pCollisionBox.max(ySwivel);
		double oMinZ = pCollisionBox.min(ySwivel);
		double oMinY = pCollisionBox.min(zSwivel);
		return oMaxY > tMinY && oMinY < tMaxY && oMaxZ > tMinZ && oMinZ < tMaxZ;
	}
	
	public void addBox(UnitBox box) {
//		if (boxes.contains(box)) return;
		boxesTrace.add(box);
		if (totalBB == null) {
			totalBB = box;
		} else {
			totalBB = new AABB(
					Math.min(totalBB.minX, box.minX),
					Math.min(totalBB.minY, box.minY),
					Math.min(totalBB.minZ, box.minZ),
					Math.max(totalBB.maxX, box.maxX),
					Math.max(totalBB.maxY, box.maxY),
					Math.max(totalBB.maxZ, box.maxZ)
			);
		}
	}
	
	@Override
	public boolean isEmpty() {
//		return boxes.isEmpty();
		return false;
	}
	
	@Override
	public double min(Direction.Axis pAxis) {
		return totalBB.min(pAxis);
	}
	
	@Override
	public double max(Direction.Axis pAxis) {
		return totalBB.max(pAxis);
	}
	
	@Override
	protected double get(Direction.Axis pAxis, int pIndex) {
		return getCoords(pAxis).get(pIndex);
	}
	
	public int size(Direction.Axis axis) {
		return (int) axis.choose(
				max(Direction.Axis.X) - min(Direction.Axis.X),
				max(Direction.Axis.Y) - min(Direction.Axis.Y),
				max(Direction.Axis.Z) - min(Direction.Axis.Z)
		);
	}
	
	@Override
	protected int findIndex(Direction.Axis pAxis, double pPosition) {
		// what actually is this?
		return Mth.binarySearch(0, size(pAxis) + 1, (p_166066_) -> pPosition < this.get(pAxis, p_166066_)) - 1;
	}
	
	@Override
	public AABB bounds() {
		if (totalBB == null) {
			// TODO: is this a good solution?
			return new AABB(0, 0, 0, 1, 1, 1);
		}
		if (this.isEmpty()) throw Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
		return totalBB;
	}
	
	@Override
	public double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		return super.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
	}
	
	@Override
	public List<AABB> toAabbs() {
		return ImmutableList.copyOf(boxesTrace);
	}
	
	VoxelShape[] neighbors = new VoxelShape[Direction.values().length];
	
	public BlockHitResult clip(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		if (collisionContext instanceof EntityCollisionContext entityCollisionContext) {
			Entity entity = entityCollisionContext.getEntity();
			PositionalInfo info = new PositionalInfo(entity, false);
			info.adjust(entity, space);
			if (entity instanceof Player player)
				info.scalePlayerReach(player, space.unitsPerBlock);
			collisionContext = CollisionContext.of(entity);
			BlockHitResult d = clip$(pStartVec, pEndVec, pPos);
			collisionContext = entityCollisionContext;
			info.reset(entity);
			return d;
		}
		return clip$(pStartVec, pEndVec, pPos);
	}
	
	private BlockHitResult clip$(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		Vec3 vec3 = pEndVec.subtract(pStartVec);
		if (vec3.lengthSqr() < 1.0E-7D) return null;
		Vec3 vec31 = pStartVec.add(vec3.scale(0.001D));
		
		double upbDouble = space.unitsPerBlock;
		// TODO: make this not rely on block pos, maybe?
		collectShape((box) -> {
			return box.contains(pStartVec) || box.clip(vec31, pEndVec).isPresent();
		}, (pos, state) -> {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			VoxelShape sp;
			if (visual) sp = state.getVisualShape(space.getMyLevel(), space.getOffsetPos(pos), collisionContext);
			else sp = state.getShape(space.getMyLevel(), space.getOffsetPos(pos), collisionContext);
			for (AABB toAabb : sp.toAabbs()) {
				toAabb = toAabb.move(x, y, z).move(offset);
				UnitBox b = (UnitBox) new UnitBox(
						toAabb.minX / upbDouble,
						toAabb.minY / upbDouble,
						toAabb.minZ / upbDouble,
						toAabb.maxX / upbDouble,
						toAabb.maxY / upbDouble,
						toAabb.maxZ / upbDouble,
						new BlockPos(x, y, z)
				);
				addBox(b);
			}
		}, space, pPos);
		
		if (this.isEmpty()) {
			return computeEdgeResult(pStartVec, pEndVec, pPos);
		}
		
		if (totalBB != null && this.totalBB.contains(pStartVec.subtract(pPos.getX(), pPos.getY(), pPos.getZ()))) {
			for (UnitBox box : boxesTrace) {
				box = (UnitBox) box.move(pPos);
				if (box.contains(pStartVec)) {
					Optional<Vec3> vec = box.clip(vec31, pEndVec);
					return new UnitHitResult(
							vec.orElse(vec31),
							Direction.getNearest(vec3.x, vec3.y, vec3.z).getOpposite(),
							pPos,
							true,
							box.pos, box
					);
				}
			}
		}
		
		UnitHitResult h = null;
		double dbest = Double.POSITIVE_INFINITY;
		double[] percent = {1};
		double d0 = pEndVec.x - pStartVec.x;
		double d1 = pEndVec.y - pStartVec.y;
		double d2 = pEndVec.z - pStartVec.z;
		
		for (UnitBox box : boxesTrace) {
			box = (UnitBox) box.move(pPos);
			Direction direction = AABB.getDirection(box, pStartVec, percent, (Direction) null, d0, d1, d2);
			double percentile = percent[0];
			percent[0] = 1;
			if (direction == null) continue;
			Vec3 vec = pStartVec.add(d0 * percentile, d1 * percentile, d2 * percentile);
			double d = vec.distanceTo(pStartVec);
			if (d < dbest) {
				h = new UnitHitResult(vec, direction, pPos, true, box.pos, box);
				dbest = d;
			}
		}
		if (h != null) return h;
		
		return computeEdgeResult(pStartVec, pEndVec, pPos);
	}
	
	// TODO: this has some precision issues
	public void collectShape(Function<MutableAABB, Boolean> simpleChecker, BiConsumer<BlockPos, BlockState> boxFiller, UnitSpace space, BlockPos pPos) {
		int upbInt = space.unitsPerBlock;
		double upbDouble = upbInt;
		
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos origin = space.getOffsetPos(new BlockPos(0, 0, 0));
		// TODO: use a more efficient loop
		MutableAABB box = new MutableAABB(0, 0, 0, 1, 1, 1);
		
		BlockPos.MutableBlockPos bbOffset = new BlockPos.MutableBlockPos(offset.x + pPos.getX(), offset.y + pPos.getY(), offset.z + pPos.getZ());
		for (int x = 0; x < upbInt; x++) {
			for (int z = 0; z < upbInt; z++) {
				box.set(
						x / upbDouble + bbOffset.getX(), bbOffset.getY(), z / upbDouble + bbOffset.getZ(),
						(x + 1) / upbDouble + bbOffset.getX(), upbInt / upbDouble + bbOffset.getY(), (z + 1) / upbDouble + bbOffset.getZ()
				);
				
				if (simpleChecker.apply(box)) {
					int pX = SectionPos.blockToSectionCoord(x + origin.getX());
					int pZ = SectionPos.blockToSectionCoord(z + origin.getZ());
					BasicVerticalChunk chunk = (BasicVerticalChunk) space.getMyLevel().getChunk(pX, pZ, ChunkStatus.FULL, false);
					if (chunk == null) {
						if (z == (z >> 4) << 4) {
							z += 15;
						} else {
							z = ((z >> 4) << 4) + 15;
						}
						continue;
					}
					
					for (int y = 0; y < upbInt; y++) {
						int sectionIndex = chunk.getSectionIndex(y + origin.getY());
						LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
						if (section == null || section.hasOnlyAir()) {
							if (y == (y >> 4) << 4) {
								y += 15;
							} else {
								y = ((y >> 4) << 4) + 15;
							}
							continue;
						}
						
						mutableBlockPos.set(x, y, z);
						
						box.set(
								x / upbDouble + bbOffset.getX(), y / upbDouble + bbOffset.getY(), z / upbDouble + bbOffset.getZ(),
								(x + 1) / upbDouble + bbOffset.getX(), (y + 1) / upbDouble + bbOffset.getY(), (z + 1) / upbDouble + bbOffset.getZ()
						);
						if (simpleChecker.apply(box)) {
							mutableBlockPos.set((x + origin.getX()) & 15, y + origin.getY(), (z + origin.getZ()) & 15);
							BlockState state = chunk.getBlockState(mutableBlockPos);
							if (state.isAir()) continue;
							mutableBlockPos.set(x, y, z);
							boxFiller.accept(mutableBlockPos, state);
						}
					}
				}
			}
		}
	}
	
	@Override
	public VoxelShape optimize() {
		UnitShape copy = new UnitShape(space, visual, collisionContext);
		for (AABB box : boxesTrace) copy.addBox((UnitBox) box);
		return this;
	}
	
	@Override
	protected double collideX(AxisCycle pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		if (collisionContext instanceof EntityCollisionContext entityCollisionContext) {
			Entity entity = entityCollisionContext.getEntity();
			PositionalInfo info = new PositionalInfo(entity, false);
			info.adjust(entity, space);
			if (entity instanceof Player player)
				info.scalePlayerReach(player, space.unitsPerBlock);
			collisionContext = CollisionContext.of(entity);
			double d = collideX$(pMovementAxis, pCollisionBox, pDesiredOffset);
			collisionContext = entityCollisionContext;
			info.reset(entity);
			return d;
		}
		return collideX$(pMovementAxis, pCollisionBox, pDesiredOffset);
	}
	
	protected double collideX$(AxisCycle pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		if (Math.abs(pDesiredOffset) < 1.0E-7D) return 0.0D;
		
		AxisCycle axiscycle = pMovementAxis.inverse();
		
		BlockPos pos = space.pos;
		if (swivelCheck(axiscycle, pCollisionBox, new AABB(pos))) {
			Direction.Axis ySwivel = axiscycle.cycle(Direction.Axis.X);
			// x->z
			// y->x
			// z->y
			pCollisionBox = HitboxScaling.getOffsetAndScaledBox(
					pCollisionBox,
					pCollisionBox.getCenter().multiply(1, 0, 1).add(0, pCollisionBox.minY, 0),
					space.unitsPerBlock,
					space.regionPos
			);
			pDesiredOffset *= space.unitsPerBlock;
			AABB motionBox = pCollisionBox;
			double signNum = Math.signum(pDesiredOffset);
			switch (ySwivel) {
				case X -> motionBox = motionBox.expandTowards(pDesiredOffset, 0, 0).contract(-signNum * pCollisionBox.getXsize(), 0, 0);
				case Y -> motionBox = motionBox.expandTowards(0, pDesiredOffset, 0).contract(0, -signNum * pCollisionBox.getYsize(), 0);
				case Z -> motionBox = motionBox.expandTowards(0, 0, pDesiredOffset).contract(0, 0, -signNum * pCollisionBox.getZsize());
			}
			// TODO: got an issue with tall block collision (fences, walls, etc)
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			mutableBlockPos.set(0, 0, 0);
			
			BlockPos bp = space.getOffsetPos(mutableBlockPos);
			int minX = (int) (motionBox.minX - 1);
			minX = Math.max(bp.getX(), minX);
			int minY = (int) (motionBox.minY - 1);
			minY = Math.max(bp.getY(), minY);
			int minZ = (int) (motionBox.minZ - 1);
			minZ = Math.max(bp.getZ(), minZ);
			int maxX = (int) Math.ceil(motionBox.maxX + 1);
			maxX = Math.min(bp.getX() + space.unitsPerBlock, maxX);
			int maxY = (int) Math.ceil(motionBox.maxY + 1);
			maxY = Math.min(bp.getY() + space.unitsPerBlock, maxY);
			int maxZ = (int) Math.ceil(motionBox.maxZ + 1);
			maxZ = Math.min(bp.getZ() + space.unitsPerBlock, maxZ);
			
			MutableAABB box = new MutableAABB(0, 0, 0, 1, 1, 1);
			
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					{
						box.set(
								x, minY, z,
								(x + 1), maxY, (z + 1)
						);
						if (!box.intersects(motionBox)) {
							continue;
						}
					}
					
					int pX = SectionPos.blockToSectionCoord(x);
					int pZ = SectionPos.blockToSectionCoord(z);
					BasicVerticalChunk chunk = (BasicVerticalChunk) space.getMyLevel().getChunk(pX, pZ, ChunkStatus.FULL, false);
					if (chunk == null) continue;
					
					for (int y = minY; y <= maxY; y++) {
						int sectionIndex = chunk.getSectionIndex(y);
						LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
						if (section == null || section.hasOnlyAir()) {
							continue;
						}
						
						mutableBlockPos.set(x, y, z);
						BlockState state = chunk.getBlockStateSmallOnly(mutableBlockPos);
						if (!state.isAir() && !(state.getBlock() instanceof UnitEdge)) {
							VoxelShape shape = state.getCollisionShape(space.getMyLevel(), mutableBlockPos, collisionContext);
							if (shape.isEmpty())
								continue;
							
							for (AABB toAabb : shape.toAabbs()) {
								box.set(toAabb).move(x, y, z);
								if (swivelCheck(axiscycle, pCollisionBox, box)) {
									pDesiredOffset = swivelOffset(axiscycle, pCollisionBox, box, pDesiredOffset);
									if (Math.abs(pDesiredOffset / space.unitsPerBlock) < 1.0E-7D) return 0.0D;
								}
							}
						}
					}
				}
			}
			pDesiredOffset /= space.unitsPerBlock;
		}
		
		return pDesiredOffset;
	}
	
	@Override
	public VoxelShape move(double pXOffset, double pYOffset, double pZOffset) {
		UnitShape copy = new UnitShape(space, visual, collisionContext);
		copy.offset = offset.add(pXOffset, pYOffset, pZOffset);
		for (AABB box : boxesTrace) copy.addBox((UnitBox) box.move(pXOffset, pYOffset, pZOffset));
		return copy;
	}
	
	@Override
	public VoxelShape getFaceShape(Direction pSide) {
		// TODO: figure out what the heck this does
		return this;
	}
	
	public Boolean intersects(VoxelShape pShape2) {
		if (collisionContext instanceof EntityCollisionContext entityCollisionContext) {
			Entity entity = entityCollisionContext.getEntity();
			PositionalInfo info = new PositionalInfo(entity, false);
			info.adjust(entity, space);
			if (entity instanceof Player player)
				info.scalePlayerReach(player, space.unitsPerBlock);
			collisionContext = CollisionContext.of(entity);
			boolean d = intersects$(pShape2);
			collisionContext = entityCollisionContext;
			info.reset(entity);
			return d;
		}
		return intersects$(pShape2);
	}
	
	protected boolean intersects$(VoxelShape pShape2) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		mutableBlockPos.set(0, 0, 0);
		
		for (AABB toAabb : pShape2.toAabbs()) {
			for (AABB box : boxesTrace) {
				if (box.intersects(toAabb)) {
					return true;
				}
			}
			AABB scaledBox = HitboxScaling.getOffsetAndScaledBox(toAabb, toAabb.getCenter().multiply(1, 0, 1).add(0, toAabb.minY, 0), space.unitsPerBlock, space.regionPos);
			
			mutableBlockPos.set(0, 0, 0);
			BlockPos bp = space.getOffsetPos(mutableBlockPos);
			int minX = (int) (scaledBox.minX - 1);
			minX = Math.max(bp.getX(), minX);
			int minY = (int) (scaledBox.minY - 1);
			minY = Math.max(bp.getY(), minY);
			int minZ = (int) (scaledBox.minZ - 1);
			minZ = Math.max(bp.getZ(), minZ);
			int maxX = (int) Math.ceil(scaledBox.maxX + 1);
			maxX = Math.min(bp.getX() + space.unitsPerBlock, maxX);
			int maxY = (int) Math.ceil(scaledBox.maxY + 1);
			maxY = Math.min(bp.getY() + space.unitsPerBlock, maxY);
			int maxZ = (int) Math.ceil(scaledBox.maxZ + 1);
			maxZ = Math.min(bp.getZ() + space.unitsPerBlock, maxZ);
			
			MutableAABB box = new MutableAABB(0, 0, 0, 1, 1, 1);
			
			for (int x = minX; x <= maxX; x++) {
				for (int z = minZ; z <= maxZ; z++) {
					{
						box.set(
								x, minY, z,
								(x + 1), maxY, (z + 1)
						);
						if (!box.intersects(scaledBox)) {
							continue;
						}
					}
					
					int pX = SectionPos.blockToSectionCoord(x);
					int pZ = SectionPos.blockToSectionCoord(z);
					BasicVerticalChunk chunk = (BasicVerticalChunk) space.getMyLevel().getChunk(pX, pZ, ChunkStatus.FULL, false);
					if (chunk == null) {
						if (z == (z >> 4) << 4) {
							z += 15;
						} else {
							z = ((z >> 4) << 4) + 15;
						}
						continue;
					}
					
					for (int y = minY; y <= maxY; y++) {
						int sectionIndex = chunk.getSectionIndex(y);
						LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
						if (section == null || section.hasOnlyAir()) {
							if (y == (y >> 4) << 4) {
								y += 15;
							} else {
								y = ((y >> 4) << 4) + 15;
							}
							continue;
						}

//						AABB box = new AABB(
//								mutableBlockPos
//						).inflate(1);
//						if (!scaledBox.intersects(box))
//							continue;
						
						mutableBlockPos.set(x, y, z);
						BlockState state = chunk.getBlockStateSmallOnly(mutableBlockPos);
						VoxelShape shape;
						if (!state.isAir() && !(state.getBlock() instanceof UnitEdge))
							shape = state.getCollisionShape(space.getMyLevel(), mutableBlockPos, collisionContext);
						else
							shape = Shapes.empty();
						if (shape.isEmpty()) continue;
						
						// TODO: is there a scenario where the cache helps?
//						BlockPos immut = mutableBlockPos.immutable();
//						VoxelShape shape = positionsChecked.get(immut);
//						if (shape == null) {
//							BlockState state = chunk.getBlockStateSmallOnly(immut);
//							if (!state.isAir() && !(state.getBlock() instanceof UnitEdge))
//								shape = state.getCollisionShape(space.getMyLevel(), immut, collisionContext);
//							else
//								shape = Shapes.empty();
//						} else
//							shape = positionsChecked.get(immut);
//						positionsChecked.put(immut, shape);
//						if (shape.isEmpty()) continue;
						
						for (AABB toAabb1 : shape.toAabbs()) {
							box.set(toAabb1).move(x, y, z);
							if (scaledBox.intersects(box)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	protected BlockHitResult computeEdgeResult(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		if (visual) return null;
		
		double upbDouble = space.unitsPerBlock;
		
		double dbest = Double.POSITIVE_INFINITY;
		UnitHitResult h = null;
		double[] percent = new double[]{1};
		double d0 = pEndVec.x - pStartVec.x;
		double d1 = pEndVec.y - pStartVec.y;
		double d2 = pEndVec.z - pStartVec.z;
		
		Vec3 totalOffset = new Vec3(pPos.getX() + offset.x, pPos.getY() + offset.y, pPos.getZ() + offset.z);
		
		MutableAABB box = new MutableAABB(0, 0, 0, 1, 1, 1);
		MutableAABB offsetBox = new MutableAABB(0, 0, 0, 1, 1, 1);
		// neighbor blocks
		for (Direction value : Direction.values()) {
			VoxelShape shape1 = neighbors[value.ordinal()];
			if (shape1 == null || shape1.isEmpty()) continue;
			shape1 = shape1.move(value.getStepX(), value.getStepY(), value.getStepZ());
			for (int xo = 0; xo < space.unitsPerBlock; xo++) {
				for (int zo = 0; zo < space.unitsPerBlock; zo++) {
					double x;
					double xSize = 1;
					double y;
					double ySize = 1;
					double z;
					double zSize = 1;
					if (value.equals(Direction.WEST) || value.equals(Direction.EAST)) {
						x = value.equals(Direction.EAST) ? (space.unitsPerBlock - 0.999) : -0.001;
						xSize = 0.001;
						y = xo;
						z = zo;
					} else if (value.equals(Direction.UP) || value.equals(Direction.DOWN)) {
						x = xo;
						y = value.equals(Direction.UP) ? (space.unitsPerBlock - 0.999) : -0.001;
						ySize = 0.001;
						z = zo;
					} else {
						x = xo;
						y = zo;
						z = value.equals(Direction.SOUTH) ? (space.unitsPerBlock - 0.999) : -0.001;
						zSize = 0.001;
					}
					box.set(
							x / upbDouble, y / upbDouble, z / upbDouble,
							(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
					);
					offsetBox.set(box).move(totalOffset);
					// less expensive than voxel shape computations
					if (offsetBox.contains(pStartVec) || offsetBox.clip(pStartVec, pEndVec).isPresent()) {
						if (value.getStepX() == 1) x += 1;
						else if (value.getStepY() == 1) y += 1;
						else if (value.getStepZ() == 1) z += 1;
						BlockPos pos = new BlockPos(x, y, z);
						VoxelShape shape2 = Shapes.joinUnoptimized(shape1, Shapes.create(box), BooleanOp.AND);
						if (shape2.isEmpty()) continue;
						for (AABB toAabb : shape2.toAabbs()) {
							UnitBox box1 = new UnitBox(
									toAabb.minX, toAabb.minY, toAabb.minZ,
									toAabb.maxX, toAabb.maxY, toAabb.maxZ,
									pos
							);
							Direction direction = AABB.getDirection(box1.move(totalOffset), pStartVec, percent, (Direction) null, d0, d1, d2);
							double percentile = percent[0];
							percent[0] = 1;
							if (direction == null) continue;
							Vec3 vec = pStartVec.add(d0 * percentile, d1 * percentile, d2 * percentile);
							double d = vec.distanceTo(pStartVec);
							if (d < dbest) {
								h = new UnitHitResult(
										vec, direction, pPos, true, box1.pos,
//										new AABB(
//												x / upbDouble, y / upbDouble, z / upbDouble,
//												(x + xSize) / upbDouble, (y + ySize) / upbDouble, (z + zSize) / upbDouble
//										)
										null
//										box
								);
							}
						}
					}
				}
			}
		}
		
		if (h != null) return h;
		return null;
	}
	
	public void setupNeigbors(BlockGetter pLevel, BlockPos pPos) {
		for (Direction value : Direction.values()) {
			BlockState state = pLevel.getBlockState(pPos.relative(value));
			if (!state.isAir()) {
				if (!(state.getBlock() instanceof UnitSpaceBlock)) {
					VoxelShape shape = state.getShape(pLevel, pPos); // TODO: entity context
					neighbors[value.ordinal()] = shape;
				}
			} else {
				neighbors[value.ordinal()] = Shapes.empty();
			}
		}
	}
}
