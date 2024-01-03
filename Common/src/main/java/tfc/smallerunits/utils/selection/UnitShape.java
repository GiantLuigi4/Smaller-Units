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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import tfc.smallerunits.UnitEdge;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.mixin.optimization.VoxelShapeAccessor;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.math.Math3d;

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
	
	// TODO:
//	@Override
//	public double min(Direction.Axis p_166079_, double p_166080_, double p_166081_) {
//		return p_166079_.choose(offset.x, offset.y, offset.z);
//	}
//
//	@Override
//	public double max(Direction.Axis p_83291_, double p_83292_, double p_83293_) {
//		return p_83291_.choose(offset.x, offset.y, offset.z) + 1;
//	}
	
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
	
	@Override
	public boolean isEmpty() {
//		return boxes.isEmpty();
		return false;
	}
	
	@Override
	public double min(Direction.Axis pAxis) {
		if (totalBB == null) return pAxis.choose(offset.x, offset.y, offset.z);
		return totalBB.min(pAxis);
	}
	
	@Override
	public double max(Direction.Axis pAxis) {
		if (totalBB == null) return pAxis.choose(offset.x, offset.y, offset.z) + 1;
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
	
	Vec3 calc(Vec3 pStart, Vec3 pEnd) {
		AABB bx = new AABB(space.pos);
		Optional<Vec3> vec = bx.clip(pStart, pEnd);
		return vec.orElse(pStart);
	}
	
	public BlockHitResult clip(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		if (collisionContext instanceof EntityCollisionContext entityCollisionContext) {
			Entity entity = entityCollisionContext.getEntity();
			PositionalInfo info = new PositionalInfo(entity, false);
			info.adjust(entity, space);
			if (entity instanceof Player player)
				info.scalePlayerReach(player, space.unitsPerBlock);
			collisionContext = CollisionContext.of(entity);
			BlockHitResult d = clip$(calc(pStartVec, pEndVec), calc(pEndVec, pStartVec), pPos);
			collisionContext = entityCollisionContext;
			info.reset(entity);
			return d;
		}
		return clip$(calc(pStartVec, pEndVec), calc(pEndVec, pStartVec), pPos);
	}
	
	private BlockHitResult clip$(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		Vec3 vec3 = pEndVec.subtract(pStartVec);
		if (vec3.lengthSqr() < 1.0E-7D) return null;
		Vec3 vec31 = pStartVec.add(vec3.scale(0.001D));
		
		double divisor = 1d / space.unitsPerBlock;
		
		ArrayList<ScaledShape> scaledShapes = new ArrayList<>();
		
		double d0 = pEndVec.x - vec31.x;
		double d1 = pEndVec.y - vec31.y;
		double d2 = pEndVec.z - vec31.z;
		double[] adouble = new double[]{1.0D};
		
		collectShape((box) -> {
			if (box.contains(pStartVec)) return true;
			return UnitShape.intersects(box, pStartVec, d0, d1, d2, adouble);
		}, (pos, state) -> {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			VoxelShape sp;
			if (visual) sp = state.getVisualShape(space.getMyLevel(), space.getOffsetPos(pos), collisionContext);
			else sp = state.getShape(space.getMyLevel(), space.getOffsetPos(pos), collisionContext);
			
			scaledShapes.add(new ScaledShape(
					pos.immutable(), sp,
					new Vec3(x * divisor + offset.x, y * divisor + offset.y, z * divisor + offset.z),
					divisor
			));
		}, space, pPos);
		
		BlockHitResult closest = null;
		double bestDist = Double.POSITIVE_INFINITY;
		
		for (ScaledShape scaledShape : scaledShapes) {
			BlockHitResult r = scaledShape.clip(pPos, pStartVec, pEndVec);
			if (r != null) {
				double dist = r.getLocation().distanceTo(pStartVec);
				if (dist <= bestDist) {
					bestDist = dist;
					closest = r;
				}
			}
		}
		
		if (closest != null) return closest;
		
		double scl = 1d / Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
		scl /= space.unitsPerBlock;
		
		double d00 = d0 * scl;
		double d10 = d1 * scl;
		double d20 = d2 * scl;
		
		pStartVec.x = pEndVec.x - d00;
		pStartVec.y = pEndVec.y - d10;
		pStartVec.z = pEndVec.z - d20;
		pEndVec.x += d00;
		pEndVec.y += d10;
		pEndVec.z += d20;
		
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
						z = (z | 0xF + 1);
						continue;
					}
					
					for (int y = 0; y < upbInt; y++) {
						int sectionIndex = chunk.getSectionIndex(y + origin.getY());
						LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
						if (section == null || section.hasOnlyAir()) {
							y = (y | 0xF + 1);
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
				case X ->
						motionBox = motionBox.expandTowards(pDesiredOffset, 0, 0).contract(-signNum * pCollisionBox.getXsize(), 0, 0);
				case Y ->
						motionBox = motionBox.expandTowards(0, pDesiredOffset, 0).contract(0, -signNum * pCollisionBox.getYsize(), 0);
				case Z ->
						motionBox = motionBox.expandTowards(0, 0, pDesiredOffset).contract(0, 0, -signNum * pCollisionBox.getZsize());
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
						z = (z | 0xF + 1);
						continue;
					}
					
					for (int y = minY; y <= maxY; y++) {
						int sectionIndex = chunk.getSectionIndex(y);
						LevelChunkSection section = chunk.getSectionNullable(sectionIndex);
						if (section == null || section.hasOnlyAir()) {
							y = (y | 0xF + 1);
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
		
		UnitHitResult h = null;
		double d0 = pEndVec.x - pStartVec.x;
		double d1 = pEndVec.y - pStartVec.y;
		double d2 = pEndVec.z - pStartVec.z;
		
		double[] doubles = new double[]{1};
		
		Vec3 u = new Vec3(0, 0, 0);
		Vec3 r = new Vec3(0, 0, 0);
		
		MutableAABB box = new MutableAABB(0, 0, 0, 1, 1, 1);
		MutableAABB offsetBox = new MutableAABB(0, 0, 0, 1, 1, 1);
		// neighbor blocks
		for (Direction value : Direction.values()) {
			VoxelShape shape1 = neighbors[value.ordinal()];
			if (shape1 == null || shape1.isEmpty()) continue;
			shape1 = shape1.getFaceShape(value.getOpposite());
			shape1 = shape1.move(value.getStepX(), value.getStepY(), value.getStepZ());
			
			BlockHitResult bhr = shape1.clip(pStartVec, pEndVec, pPos);
			if (bhr != null && bhr.getType() != HitResult.Type.MISS) {
				if (bhr.getDirection() != value.getOpposite()) continue;
				
				Vec3 loc = bhr.getLocation();
				loc.x -= pPos.getX();
				loc.y -= pPos.getY();
				loc.z -= pPos.getZ();
				
				loc.x *= space.unitsPerBlock;
				loc.y *= space.unitsPerBlock;
				loc.z *= space.unitsPerBlock;
				
				Direction up = Math3d.getUp(value);
				Direction right = Math3d.getRight(value);
				
				u.x = (int) (up.getStepX() * loc.x);
				u.y = (int) (up.getStepY() * loc.y);
				u.z = (int) (up.getStepZ() * loc.z);
				
				r.x = (int) (right.getStepX() * loc.x);
				r.y = (int) (right.getStepY() * loc.y);
				r.z = (int) (right.getStepZ() * loc.z);
				
				box.set(
						up.getStepX() * u.x + right.getStepX() * r.x,
						up.getStepY() * u.y + right.getStepY() * r.y,
						up.getStepZ() * u.z + right.getStepZ() * r.z,
						up.getStepX() * u.x + right.getStepX() * r.x + 1,
						up.getStepY() * u.y + right.getStepY() * r.y + 1,
						up.getStepZ() * u.z + right.getStepZ() * r.z + 1
				);
				
				box.scale(1d / space.unitsPerBlock);
				
				switch (value) {
					case UP -> box.move(0, 1, 0);
					case EAST -> box.move(1, 0, 0);
					case SOUTH -> box.move(0, 0, 1);
					case DOWN -> box.move(0, 1d / -space.unitsPerBlock, 0);
					case WEST -> box.move(1d / -space.unitsPerBlock, 0, 0);
					case NORTH -> box.move(0, 0, 1d / -space.unitsPerBlock);
				}
				
				offsetBox.set(box);
				offsetBox.move(pPos);
				
				if (intersects(offsetBox, pStartVec, d0, d1, d2, doubles)) {
					double hX = pStartVec.x + d0 * doubles[0];
					double hY = pStartVec.y + d1 * doubles[0];
					double hZ = pStartVec.z + d2 * doubles[0];
					doubles[0] = 1;
					
					Vec3 hvec = new Vec3(hX, hY, hZ);
					if (Shapes.joinIsNotEmpty(
							Shapes.create(box),
							shape1,
							BooleanOp.AND
					)) {
						BlockPos pos = new BlockPos(
								up.getStepX() * u.x + right.getStepX() * r.x,
								up.getStepY() * u.y + right.getStepY() * r.y,
								up.getStepZ() * u.z + right.getStepZ() * r.z
						);
						
						switch (value) {
							case UP -> pos = pos.above(space.unitsPerBlock);
							case EAST -> pos = pos.east(space.unitsPerBlock);
							case SOUTH -> pos = pos.south(space.unitsPerBlock);
							case DOWN -> pos = pos.below();
							case WEST -> pos = pos.west();
							case NORTH -> pos = pos.north();
						}
						
						h = new UnitHitResult(
//									hvec.subtract(d0, d1, d2),
								hvec,
								bhr.getDirection(),
								space.pos,
								bhr.isInside(),
								pos,
								box
						);
					}
				}
			}
		}
		
		return h;
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
	
	public static boolean intersects(AABB box, Vec3 start, double d0, double d1, double d2, double[] adouble) {
		adouble[0] = 1;
		// TODO: use a specialized ray-box intersection algorithm which doesn't go over all directions but instead just goes until it finds a hit
		AABB.getDirection(box, start, adouble, null, d0, d1, d2);
		return adouble[0] != 1;
	}
}
