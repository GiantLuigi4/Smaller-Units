package tfc.smallerunits.utils.selection;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnitShape extends VoxelShape {
	private final ArrayList<AABB> boxes = new ArrayList<>();
	private AABB totalBB = new AABB(
			Double.POSITIVE_INFINITY,
			Double.POSITIVE_INFINITY,
			Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY,
			Double.NEGATIVE_INFINITY,
			Double.NEGATIVE_INFINITY
	);
	
	public UnitShape() {
		super(null);
	}
	
	public void addBox(AABB box) {
		boxes.add(box);
		totalBB = new AABB(
				Math.min(totalBB.minX, box.minX),
				Math.min(totalBB.minY, box.minY),
				Math.min(totalBB.minZ, box.minZ),
				Math.min(totalBB.maxX, box.maxX),
				Math.min(totalBB.maxY, box.maxY),
				Math.min(totalBB.maxZ, box.maxZ)
		);
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
	
	@Override
	public AABB bounds() {
		return totalBB;
	}
	
	@Override
	public List<AABB> toAabbs() {
		return boxes;
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
		return Mth.binarySearch(0, size(pAxis) + 1, (p_166066_) -> pPosition < this.get(pAxis, p_166066_)) - 1;
	}
	
	@Nullable
	public BlockHitResult clip(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		if (this.isEmpty()) {
			return null;
		} else {
			Vec3 vec3 = pEndVec.subtract(pStartVec);
			if (vec3.lengthSqr() < 1.0E-7D) {
				return null;
			} else {
				Vec3 vec31 = pStartVec.add(vec3.scale(0.001D));
//				return this.shape.isFullWide(this.findIndex(Direction.Axis.X, vec31.x - (double) pPos.getX()), this.findIndex(Direction.Axis.Y, vec31.y - (double) pPos.getY()), this.findIndex(Direction.Axis.Z, vec31.z - (double) pPos.getZ())) ? new BlockHitResult(vec31, Direction.getNearest(vec3.x, vec3.y, vec3.z).getOpposite(), pPos, true) : AABB.clip(this.toAabbs(), pStartVec, pEndVec, pPos);
				for (AABB box : boxes) {
					box = box.move(pPos);
					if (box.contains(pStartVec)) {
						Optional<Vec3> vec = box.clip(vec31, pEndVec);
						return new BlockHitResult(
								vec.orElse(vec31),
								Direction.getNearest(vec3.x, vec3.y, vec3.z).getOpposite(),
								pPos,
								true
						);
					}
				}
				return AABB.clip(this.toAabbs(), pStartVec, pEndVec, pPos);
			}
		}
	}
	
	@Override
	public double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		return super.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
	}
	
	@Override
	protected double collideX(AxisCycle pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		// TODO: what the heck is a AxisCycle
//		return super.collideX(pMovementAxis, pCollisionBox, pDesiredOffset);
		return pDesiredOffset;
	}
	
	@Override
	public VoxelShape getFaceShape(Direction pSide) {
		// TODO: figure out what the heck this does
		return this;
	}
}
