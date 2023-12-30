package tfc.smallerunits.utils.spherebox;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.Vec3;

public class VecMath {
	protected static final ThreadLocal<Quaternion> point = ThreadLocal.withInitial(() -> new Quaternion(0, 0, 0, 0));
	protected static final ThreadLocal<Quaternion> newPoint = ThreadLocal.withInitial(() -> new Quaternion(0, 0, 0, 0));
	
	public static void rotate(Vec3 src, Quaternion quaternion, Vector4f dst) {
		Quaternion point = VecMath.point.get();
		point.set((float) src.x, (float) src.y, (float) src.z, 0);
		Quaternion newPoint = VecMath.newPoint.get();
		newPoint.set(quaternion.i(), quaternion.j(), quaternion.k(), quaternion.r());
		point.mul(newPoint);
		newPoint.conj();
		newPoint.mul(point);
		
		dst.set(newPoint.i(), newPoint.j(), newPoint.k(), 0);
	}
}
