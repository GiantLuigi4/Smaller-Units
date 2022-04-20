package badvox.traceables;

import badvox.TraceResult;
import badvox.Traceable;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;

public class TraceableBox extends Traceable {
	private final AABB box;
	private final Object context;
	
	public TraceableBox(AABB box, Object context) {
		this.box = box;
		this.context = context;
	}
	
	public TraceableBox(AABB box) {
		this.box = box;
		this.context = null;
	}
	
	public static Direction getDirectionInverse(AABB pAabb, Vec3 pStart, double[] pMinDistance, @Nullable Direction pFacing, double pDeltaX, double pDeltaY, double pDeltaZ) {
		if (pDeltaX < 1.0E-7D) {
			pFacing = clipPoint(pMinDistance, pFacing, pDeltaX, pDeltaY, pDeltaZ, pAabb.minX, pAabb.minY, pAabb.maxY, pAabb.minZ, pAabb.maxZ, Direction.WEST, pStart.x, pStart.y, pStart.z);
		} else if (pDeltaX > -1.0E-7D) {
			pFacing = clipPoint(pMinDistance, pFacing, pDeltaX, pDeltaY, pDeltaZ, pAabb.maxX, pAabb.minY, pAabb.maxY, pAabb.minZ, pAabb.maxZ, Direction.EAST, pStart.x, pStart.y, pStart.z);
		}
		
		if (pDeltaY < 1.0E-7D) {
			pFacing = clipPoint(pMinDistance, pFacing, pDeltaY, pDeltaZ, pDeltaX, pAabb.minY, pAabb.minZ, pAabb.maxZ, pAabb.minX, pAabb.maxX, Direction.DOWN, pStart.y, pStart.z, pStart.x);
		} else if (pDeltaY > -1.0E-7D) {
			pFacing = clipPoint(pMinDistance, pFacing, pDeltaY, pDeltaZ, pDeltaX, pAabb.maxY, pAabb.minZ, pAabb.maxZ, pAabb.minX, pAabb.maxX, Direction.UP, pStart.y, pStart.z, pStart.x);
		}
		
		if (pDeltaZ < 1.0E-7D) {
			pFacing = clipPoint(pMinDistance, pFacing, pDeltaZ, pDeltaX, pDeltaY, pAabb.minZ, pAabb.minX, pAabb.maxX, pAabb.minY, pAabb.maxY, Direction.NORTH, pStart.z, pStart.x, pStart.y);
		} else if (pDeltaZ > -1.0E-7D) {
			pFacing = clipPoint(pMinDistance, pFacing, pDeltaZ, pDeltaX, pDeltaY, pAabb.maxZ, pAabb.minX, pAabb.maxX, pAabb.minY, pAabb.maxY, Direction.SOUTH, pStart.z, pStart.x, pStart.y);
		}
		
		return pFacing;
	}
	
	private static Direction clipPoint(double[] pMinDistance, @Nullable Direction pPrevDirection, double pDistanceSide, double pDistanceOtherA, double pDistanceOtherB, double pMinSide, double pMinOtherA, double pMaxOtherA, double pMinOtherB, double pMaxOtherB, Direction pHitSide, double pStartSide, double pStartOtherA, double pStartOtherB) {
		double d0 = (pMinSide - pStartSide) / pDistanceSide;
		double d1 = pStartOtherA + d0 * pDistanceOtherA;
		double d2 = pStartOtherB + d0 * pDistanceOtherB;
		if (0.0D < d0 && d0 < pMinDistance[0] && pMinOtherA - 1.0E-7D < d1 && d1 < pMaxOtherA + 1.0E-7D && pMinOtherB - 1.0E-7D < d2 && d2 < pMaxOtherB + 1.0E-7D) {
			pMinDistance[0] = d0;
			return pHitSide;
		} else {
			return pPrevDirection;
		}
	}
	
	@Override
	public TraceResult trace(Vec3 start, Vec3 end) {
		if (box.contains(start)) {
			Vec3 vec3 = end.subtract(start);
			Vec3 vec31 = start.add(vec3.scale(0.001D));
			Optional<Vec3> vec = box.clip(vec31, end);
			return new TraceResult(Direction.getNearest(vec3.x, vec3.y, vec3.z).getOpposite(), vec.orElse(vec31), context, this);
		}
		double[] percent = new double[]{1};
		double d0 = end.x - start.x;
		double d1 = end.y - start.y;
		double d2 = end.z - start.z;
		Direction direction = AABB.getDirection(box, start, percent, null, d0, d1, d2);
		double percentile = percent[0];
		Vec3 vec = new Vec3(
				((1 - percentile) * start.x) + ((percentile) * end.x),
				((1 - percentile) * start.y) + ((percentile) * end.y),
				((1 - percentile) * start.z) + ((percentile) * end.z)
		);
		return new TraceResult(direction, vec, context, this);
	}
	
	@Override
	public boolean intersects(Traceable traceable) {
		if (traceable instanceof TraceableBox) {
			return ((TraceableBox) traceable).box.intersects(this.box);
		}
		return traceable.intersects(this);
	}
	
	@Override
	public TraceResult traceBack(Vec3 start, Vec3 end) {
//		if (box.contains(start)) return null;
		double[] percent = new double[]{1};
		double d0 = end.x - start.x;
		double d1 = end.y - start.y;
		double d2 = end.z - start.z;
		Direction direction = getDirectionInverse(box, start, percent, null, d0, d1, d2);
		double percentile = percent[0];
		Vec3 vec = new Vec3(
				((1 - percentile) * start.x) + ((percentile) * end.x),
				((1 - percentile) * start.y) + ((percentile) * end.y),
				((1 - percentile) * start.z) + ((percentile) * end.z)
		);
		return new TraceResult(direction, vec, context, this);
	}
	
	@Override
	public boolean contains(Vec3 vec) {
		return box.contains(vec);
	}
	
	@Override
	public Vec3 min() {
		return new Vec3(box.minX, box.minY, box.minZ);
	}
	
	@Override
	public Vec3 max() {
		return new Vec3(box.maxX, box.maxY, box.maxZ);
	}
}
