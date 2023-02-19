package tfc.smallerunits.utils.spherebox;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SphereBox {
	private static final ThreadLocal<Vector4f> worker = ThreadLocal.withInitial(() -> new Vector4f(0, 0, 0, 0));
	private static final ThreadLocal<Vector3f> offset = ThreadLocal.withInitial(() -> new Vector3f(0, 0, 0));
	private static final ThreadLocal<Vector3f> localCenter = ThreadLocal.withInitial(() -> new Vector3f(0, 0, 0));
	
	public static boolean intersects(Box box, Vec3 point, float radius) {
		Vector4f worker = SphereBox.worker.get();
		Vector3f offset = SphereBox.offset.get();
		Vector3f localCenter = SphereBox.localCenter.get();
		
		AABB bounds = box.getLsAABB(worker);
		
		localCenter.set(
				(float) (bounds.minX + bounds.maxX) / 2f,
				(float) (bounds.minY + bounds.maxY) / 2f,
				(float) (bounds.minZ + bounds.maxZ) / 2f
		);
		
		Quaternion quaternion = box.quaternion;
		
		VecMath.rotate(point, quaternion, worker);
		
		offset.set(
				localCenter.x() - worker.x(),
				localCenter.y() - worker.y(),
				localCenter.z() - worker.z()
		);
		offset.normalize();
		offset.mul(radius);
		
		return bounds.contains(worker.x() + offset.x(), worker.y() + offset.y(), worker.z() + offset.z()) || bounds.contains(worker.x(), worker.y(), worker.z());

//		Quaternion point = new Quaternion((float) pos.x(), (float) pos.y(), (float) pos.z(), 0);
//		Quaternion newPoint = quaternion.copy();
//		point.mul(newPoint);
//		newPoint.conj();
//		newPoint.mul(point);
//
//		boolean intersection = box.contains(rotatedPos);
//		Vec3 offset = center.subtract(rotatedPos).normalize().scale(0.5);
//		intersection = box.contains(rotatedPos.add(offset));
	}
}
