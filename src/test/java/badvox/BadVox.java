package badvox;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BadVox extends VoxelShape {
	Traceable traceable;
	
	public BadVox(Traceable traceable) {
		super(null);
		this.traceable = traceable;
	}
	
	@Override
	protected DoubleList getCoords(Direction.Axis pAxis) {
		return null;
	}
	
	@Override
	public double min(Direction.Axis pAxis) {
		return super.min(pAxis);
	}
	
	@Override
	public double max(Direction.Axis pAxis) {
		return super.max(pAxis);
	}
	
	@Override
	public AABB bounds() {
		return super.bounds();
	}
	
	@Override
	protected double get(Direction.Axis pAxis, int pIndex) {
		return super.get(pAxis, pIndex);
	}
	
	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}
	
	@Override
	public VoxelShape move(double pXOffset, double pYOffset, double pZOffset) {
		return super.move(pXOffset, pYOffset, pZOffset);
	}
	
	@Override
	public VoxelShape optimize() {
		return super.optimize();
	}
	
	@Override
	public void forAllEdges(Shapes.DoubleLineConsumer pAction) {
		super.forAllEdges(pAction);
	}
	
	@Override
	public void forAllBoxes(Shapes.DoubleLineConsumer pAction) {
		super.forAllBoxes(pAction);
	}
	
	@Override
	public List<AABB> toAabbs() {
		return super.toAabbs();
	}
	
	@Override
	public double min(Direction.Axis pAxis, double pPrimaryPosition, double pSecondaryPosition) {
		return super.min(pAxis, pPrimaryPosition, pSecondaryPosition);
	}
	
	@Override
	public double max(Direction.Axis pAxis, double pPrimaryPosition, double pSecondaryPosition) {
		return super.max(pAxis, pPrimaryPosition, pSecondaryPosition);
	}
	
	@Override
	protected int findIndex(Direction.Axis pAxis, double pPosition) {
		return super.findIndex(pAxis, pPosition);
	}
	
	@Nullable
	@Override
	public BlockHitResult clip(Vec3 pStartVec, Vec3 pEndVec, BlockPos pPos) {
		return super.clip(pStartVec, pEndVec, pPos);
	}
	
	@Override
	public Optional<Vec3> closestPointTo(Vec3 pPoint) {
		return super.closestPointTo(pPoint);
	}
	
	@Override
	public VoxelShape getFaceShape(Direction pSide) {
		return super.getFaceShape(pSide);
	}
	
	@Override
	public double collide(Direction.Axis pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		return super.collide(pMovementAxis, pCollisionBox, pDesiredOffset);
	}
	
	@Override
	protected double collideX(AxisCycle pMovementAxis, AABB pCollisionBox, double pDesiredOffset) {
		return super.collideX(pMovementAxis, pCollisionBox, pDesiredOffset);
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
