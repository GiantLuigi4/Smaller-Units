package tfc.smallerunits.utils.spherebox;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.utils.selection.MutableVec3;

public class Box {
	Vec3[] points;
	Quaternion quaternion;
	AABB lsBounds;
	AABB wsBounds;
	Vec3 offset;
	
	public Box(Vec3[] points, Quaternion quaternion, Vector4f worker, Vec3 offset) {
		this.points = points;
		this.quaternion = quaternion;
		this.offset = offset;
		calcAABB(worker);
	}
	
	public void lsVec(Vec3 other, Vector4f dst) {
		quaternion.conj();
		VecMath.rotate(other, quaternion, dst);
		quaternion.conj();
	}
	
	public void calcAABB(Vector4f worker) {
		double wsMinX = Double.POSITIVE_INFINITY;
		double wsMinY = Double.POSITIVE_INFINITY;
		double wsMinZ = Double.POSITIVE_INFINITY;
		double wsMaxX = Double.NEGATIVE_INFINITY;
		double wsMaxY = Double.NEGATIVE_INFINITY;
		double wsMaxZ = Double.NEGATIVE_INFINITY;
		
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		
		MutableVec3 mv3 = new MutableVec3(0, 0, 0);
		for (Vec3 point : points) {
			wsMinX = Math.min(wsMinX, point.x());
			wsMinY = Math.min(wsMinY, point.y());
			wsMinZ = Math.min(wsMinZ, point.z());
			wsMaxX = Math.max(wsMaxX, point.x());
			wsMaxY = Math.max(wsMaxY, point.y());
			wsMaxZ = Math.max(wsMaxZ, point.z());
			
			mv3.set(point.x + offset.x, point.y + offset.y, point.z + offset.z);
			VecMath.rotate(mv3, quaternion, worker);
			minX = Math.min(minX, worker.x());
			minY = Math.min(minY, worker.y());
			minZ = Math.min(minZ, worker.z());
			maxX = Math.max(maxX, worker.x());
			maxY = Math.max(maxY, worker.y());
			maxZ = Math.max(maxZ, worker.z());
		}
		
		wsBounds = new AABB(wsMinX, wsMinY, wsMinZ, wsMaxX, wsMaxY, wsMaxZ).move(offset);
		lsBounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public AABB getLsAABB(Vector4f worker) {
		return lsBounds;
	}
	
	public AABB getWsAABB(Vector4f worker) {
		return wsBounds;
	}
}
