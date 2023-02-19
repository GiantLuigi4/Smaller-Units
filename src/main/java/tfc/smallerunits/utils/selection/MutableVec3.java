package tfc.smallerunits.utils.selection;

import com.mojang.math.Vector3f;
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
}
