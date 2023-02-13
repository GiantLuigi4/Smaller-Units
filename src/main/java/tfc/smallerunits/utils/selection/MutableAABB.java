package tfc.smallerunits.utils.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MutableAABB extends AABB {
	public MutableAABB(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
		super(pX1, pY1, pZ1, pX2, pY2, pZ2);
	}
	
	public MutableAABB(BlockPos pPos) {
		super(pPos);
	}
	
	public MutableAABB(BlockPos pStart, BlockPos pEnd) {
		super(pStart, pEnd);
	}
	
	public MutableAABB(Vec3 pStart, Vec3 pEnd) {
		super(pStart, pEnd);
	}
	
	public MutableAABB set(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
		this.minX = pX1;
		this.minY = pY1;
		this.minZ = pZ1;
		this.maxX = pX2;
		this.maxY = pY2;
		this.maxZ = pZ2;
		return this;
	}
	
	public MutableAABB set(AABB box) {
		this.minX = box.minX;
		this.minY = box.minY;
		this.minZ = box.minZ;
		this.maxX = box.maxX;
		this.maxY = box.maxY;
		this.maxZ = box.maxZ;
		return this;
	}
	
	@Override
	public AABB move(double x, double y, double z) {
		minX += x;
		minY += y;
		minZ += z;
		maxX += x;
		maxY += y;
		maxZ += z;
		return this;
	}
	
	@Override
	public AABB move(BlockPos pPos) {
		minX += pPos.getX();
		minY += pPos.getY();
		minZ += pPos.getZ();
		maxX += pPos.getX();
		maxY += pPos.getY();
		maxZ += pPos.getZ();
		return this;
	}
}
