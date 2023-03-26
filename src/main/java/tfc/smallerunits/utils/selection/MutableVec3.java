package tfc.smallerunits.utils.selection;

import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class MutableVec3 extends Vec3 {
	public MutableVec3(double pX, double pY, double pZ) {
		super(pX, pY, pZ);
	}
	
	public MutableVec3(Vector3f pFloatVector) {
		super(pFloatVector);
	}
	
	public MutableVec3 set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public MutableVec3 set(Vec3 vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		return this;
	}
	
	@Override
	public Vec3 add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vec3 subtract(BlockPos pos) {
		return subtract(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public Vec3 add(BlockPos pos) {
		return add(pos.getX(), pos.getY(), pos.getZ());
	}
}
