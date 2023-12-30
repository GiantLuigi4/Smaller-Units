package tfc.smallerunits.utils.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class UnitBox extends AABB {
	BlockPos pos;
	
	public UnitBox(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2, BlockPos pos) {
		super(pX1, pY1, pZ1, pX2, pY2, pZ2);
		this.pos = pos;
	}
	
	public UnitBox(BlockPos pPos, BlockPos pos) {
		super(pPos);
		this.pos = pos;
	}
	
	public UnitBox(BlockPos pStart, BlockPos pEnd, BlockPos pos) {
		super(pStart, pEnd);
		this.pos = pos;
	}
	
	public UnitBox(Vec3 pStart, Vec3 pEnd, BlockPos pos) {
		super(pStart, pEnd);
		this.pos = pos;
	}
	
	@Override
	public AABB move(double pX, double pY, double pZ) {
		return new UnitBox(this.minX + pX, this.minY + pY, this.minZ + pZ, this.maxX + pX, this.maxY + pY, this.maxZ + pZ, pos);
	}
	
	@Override
	public AABB move(BlockPos pPos) {
		return new UnitBox(this.minX + (double) pPos.getX(), this.minY + (double) pPos.getY(), this.minZ + (double) pPos.getZ(), this.maxX + (double) pPos.getX(), this.maxY + (double) pPos.getY(), this.maxZ + (double) pPos.getZ(), pos);
	}
}
