package tfc.smallerunits.utils.selection;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.mixin.optimization.VoxelShapeAccessor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

// best of 1.18 and 1.12, neat
public class UnitShape extends VoxelShape {
	private final ArrayList<UnitBox> boxes = new ArrayList<>();
	private AABB totalBB = null;
	
	public final UnitSpace space;
	Vec3 offset = new Vec3(0, 0, 0);
	
	public UnitShape(UnitSpace space) {
		super(new UnitDiscreteShape(0, 0, 0));
		((UnitDiscreteShape) ((VoxelShapeAccessor) this).getShape()).sp = this;
		this.space = space;
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
			if (offsetX > 0.0D && oMaxX <= tMinX) {
				double deltaX = tMinX - oMaxX;
				
				if (deltaX < offsetX) return deltaX;
			} else if (offsetX < 0.0D && oMinX >= tMaxX) {
				double deltaX = tMaxX - oMinX;
				
				if (deltaX > offsetX) return deltaX;
			}
		}
		return offsetX;
	}
	
	@Override
	public void forAllEdges(Shapes.DoubleLineConsumer pAction) {
		// oh, well that wasn't that bad
		for (AABB box : boxes) {
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
		return;
	}
	
	@Override
	public void forAllBoxes(Shapes.DoubleLineConsumer pAction) {
		for (AABB box : boxes) {
			pAction.consume(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		}
	}
	
	@Override
	protected DoubleList getCoords(Direction.Axis pAxis) {
		// TODO: cache
		DoubleArrayList arrayList = new DoubleArrayList();
		for (AABB box : boxes) {
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
		boxes.add(box);
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
		return boxes.isEmpty();
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
		if (this.isEmpty()) throw Util.pauseInIde(new UnsupportedOperationException("No bounds for empty shape."));
		return totalBB;
	}
	
	@Override
	public double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		return super.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
	}
	
	@Override
	public List<AABB> toAabbs() {
		return ImmutableList.copyOf(boxes);
	}
	
	VoxelShape[] neighbors = new VoxelShape[Direction.values().length];
	
	@Nullable
	public BlockHitResult clip(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		Vec3 vec3 = pEndVec.subtract(pStartVec);
		if (vec3.lengthSqr() < 1.0E-7D) return null;
		Vec3 vec31 = pStartVec.add(vec3.scale(0.001D));
		
		double upbDouble = space.unitsPerBlock;
		// TODO: make this not rely on block pos
		collectShape((pos) -> {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			AABB box = new AABB(
					x / upbDouble, y / upbDouble, z / upbDouble,
					(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
			).move(offset).move(pPos);
			return box.contains(pStartVec) || box.clip(vec31, pEndVec).isPresent();
		}, (pos, state) -> {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			VoxelShape sp = state.getShape(space.getMyLevel(), space.getOffsetPos(pos));
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
		}, space);
		
		if (this.isEmpty()) {
			return computeEdgeResult(pStartVec, pEndVec, pPos);
		}
		
		if (totalBB != null && this.totalBB.contains(pStartVec.subtract(pPos.getX(), pPos.getY(), pPos.getZ()))) {
			for (UnitBox box : boxes) {
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
		
		for (UnitBox box : boxes) {
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
	
	public void collectShape(Function<BlockPos, Boolean> simpleChecker, BiConsumer<BlockPos, BlockState> boxFiller, UnitSpace space) {
		int upbInt = space.unitsPerBlock;
		
		for (int x = 0; x < upbInt; x++) {
			for (int y = 0; y < upbInt; y++) {
				for (int z = 0; z < upbInt; z++) {
					BlockState state = space.getBlock(x, y, z);
					if (state.isAir()) continue;
//					if (state == null) continue;
					if (simpleChecker.apply(new BlockPos(x, y, z))) {
						boxFiller.accept(new BlockPos(x, y, z), state);
					}
				}
			}
		}
	}
	
	@Override
	public VoxelShape optimize() {
		UnitShape copy = new UnitShape(space);
		for (AABB box : boxes) copy.addBox((UnitBox) box);
		return this;
	}
	
	@Override
	protected double collideX(AxisCycle pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		if (this.isEmpty()) return pDesiredOffset;
		else if (Math.abs(pDesiredOffset) < 1.0E-7D) return 0.0D;
		
		AxisCycle axiscycle = pMovementAxis.inverse();
		
		if (swivelCheck(axiscycle, pCollisionBox, this.totalBB)) {
			for (AABB box : boxes) {
				pDesiredOffset = swivelOffset(axiscycle, pCollisionBox, box, pDesiredOffset);
				if (Math.abs(pDesiredOffset) < 1.0E-7D) return 0.0D;
			}
		}
		return pDesiredOffset;
	}
	
	@Override
	public VoxelShape move(double pXOffset, double pYOffset, double pZOffset) {
		UnitShape copy = new UnitShape(space);
		copy.offset = offset.add(pXOffset, pYOffset, pZOffset);
		for (AABB box : boxes) copy.addBox((UnitBox) box.move(pXOffset, pYOffset, pZOffset));
		return copy;
	}
	
	@Override
	public VoxelShape getFaceShape(Direction pSide) {
		// TODO: figure out what the heck this does
		return this;
	}
	
	public Boolean intersects(VoxelShape pShape2) {
		for (AABB toAabb : pShape2.toAabbs()) {
			for (AABB box : boxes) {
				if (box.intersects(toAabb)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected BlockHitResult computeEdgeResult(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		double upbDouble = space.unitsPerBlock;
		
		double dbest = Double.POSITIVE_INFINITY;
		UnitHitResult h = null;
		double[] percent = new double[]{1};
		double d0 = pEndVec.x - pStartVec.x;
		double d1 = pEndVec.y - pStartVec.y;
		double d2 = pEndVec.z - pStartVec.z;
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
					AABB box = new AABB(
							x / upbDouble, y / upbDouble, z / upbDouble,
							(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
					);
					// less expensive than voxel shape computations
					if (box.move(pPos).move(offset).contains(pStartVec) || box.move(pPos).move(offset).clip(pStartVec, pEndVec).isPresent()) {
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
							Direction direction = AABB.getDirection(box1.move(pPos).move(offset), pStartVec, percent, (Direction) null, d0, d1, d2);
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
