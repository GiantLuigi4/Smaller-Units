package badvox.traceables;

import badvox.TraceResult;
import badvox.Traceable;
import net.minecraft.world.phys.Vec3;

public class NonTraceable extends Traceable {
	@Override
	public TraceResult trace(Vec3 start, Vec3 end) {
		return null;
	}
	
	@Override
	public TraceResult traceBack(Vec3 start, Vec3 end) {
		return null;
	}
	
	@Override
	public boolean intersects(Traceable traceable) {
		return false;
	}
	
	@Override
	public boolean contains(Vec3 vec) {
		return false;
	}
	
	@Override
	public Vec3 min() {
		return new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	@Override
	public Vec3 max() {
		return new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
}
