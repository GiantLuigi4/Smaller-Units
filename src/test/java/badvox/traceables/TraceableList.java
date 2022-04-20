package badvox.traceables;

import badvox.TraceResult;
import badvox.Traceable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/* OR */
public class TraceableList extends Traceable {
	private final ArrayList<Traceable> tracables = new ArrayList<>();
	
	public TraceableList() {
	}
	
	public void addTraceable(Traceable tracable) {
		tracables.add(tracable);
	}
	
	@Override
	public TraceResult trace(Vec3 start, Vec3 end) {
		TraceResult best = null;
		double bestDist = Double.POSITIVE_INFINITY;
		AABB traceBox = new AABB(start, end);
		for (Traceable traceable : tracables) {
			if (new AABB(traceable.min(), traceable.max()).intersects(traceBox)) {
				TraceResult result = traceable.trace(start, end);
				if (result != null) {
					double d = result.point.distanceTo(start);
					if (d < bestDist) {
						bestDist = Math.min(d, bestDist);
						best = result;
					}
				}
			}
		}
		return best;
	}
	
	@Override
	public TraceResult traceBack(Vec3 start, Vec3 end) {
		TraceResult initialResult = trace(start, end);
		if (initialResult == null) return null;
		Vec3 o = start.normalize().scale(0.01);
		Vec3 from = initialResult.point.add(o);
		Traceable traceable = initialResult.successful;
		TraceResult last = initialResult;
		while (true) {
			initialResult = trace(from, end);
			if (initialResult == null) return last;
			if (!initialResult.successful.intersects(traceable)) {
				return last.successful.traceBack(start, end);
			}
			last = initialResult;
		}
//		for (Traceable tracable : tracables) {
//		}
//		return null;
	}
	
	@Override
	public boolean intersects(Traceable traceable) {
		for (Traceable traceable1 : tracables) {
			if (traceable.intersects(traceable1)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean contains(Vec3 vec) {
		for (Traceable tracable : tracables) {
			if (tracable.contains(vec)) return true;
		}
		return false;
	}
	
	@Override
	public Vec3 min() {
		Vec3 min = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		for (Traceable tracable : tracables) {
			Vec3 tmin = tracable.min();
			min = new Vec3(
					Math.min(tmin.x, min.x),
					Math.min(tmin.y, min.y),
					Math.min(tmin.z, min.z)
			);
		}
		return min;
	}
	
	@Override
	public Vec3 max() {
		Vec3 max = new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		for (Traceable tracable : tracables) {
			Vec3 tmax = tracable.max();
			max = new Vec3(
					Math.max(tmax.x, max.x),
					Math.max(tmax.y, max.y),
					Math.max(tmax.z, max.z)
			);
		}
		return max;
	}
}
