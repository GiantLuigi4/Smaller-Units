package badvox.traceables;

import badvox.TraceResult;
import badvox.Traceable;
import net.minecraft.world.phys.Vec3;

/* NOT, ONLY_FIRST, ONLY_SECOND */
public class Negation extends Traceable {
	final Traceable first, second;
	
	public Negation(Traceable first, Traceable second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public TraceResult trace(Vec3 start, Vec3 end) {
		TraceResult positiveResult = first.trace(start, end);
		if (positiveResult == null) return null;
		TraceResult negativeResult = second.trace(start, end);
		if (negativeResult == null) return positiveResult;
		double pd = positiveResult.point.distanceTo(start);
		double nd = negativeResult.point.distanceTo(start);
		if (nd <= pd) {
			// TODO: handle this slightly better
			TraceResult result = second.trace(end, negativeResult.point);
			if (result == null) {
				// ???
				// idrk what to do for this scenario yet
				return null;
			}
			// if the positive part's result is further from the start than the negative part's result, then clearly the negative part has done nothing, and should be ignored
			double ndb = result.point.distanceTo(start);
			if (pd > ndb) {
				return positiveResult;
			}
			TraceResult res = first.trace(result.point.subtract(start.normalize().scale(0.01d)), end); /* TODO: optimization? */
			return new TraceResult(result.dir == null ? null : result.dir.getOpposite(), result.point, positiveResult.context, res.successful);
		} else {
			return positiveResult;
		}
	}
	
	@Override
	public boolean intersects(Traceable traceable) {
		return first.intersects(traceable); // TODO
	}
	
	@Override
	public boolean contains(Vec3 vec) {
		if (second.contains(vec)) return false;
		return first.contains(vec);
	}
	
	@Override
	public TraceResult traceBack(Vec3 start, Vec3 end) {
		return null;
	}
	
	@Override
	public Vec3 min() {
		// TODO: do this properly
		return first.min();
	}
	
	@Override
	public Vec3 max() {
		// TODO: do this properly
		return first.max();
	}
}
