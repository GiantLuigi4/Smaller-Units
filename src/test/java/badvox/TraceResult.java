package badvox;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class TraceResult {
	public final Direction dir;
	public final Vec3 point;
	public final Object context;
	public final Traceable successful;
	
	public TraceResult(Direction dir, Vec3 point, Object context, Traceable successful) {
		this.dir = dir;
		this.point = point;
		this.context = context;
		this.successful = successful;
	}
	
	@Override
	public String toString() {
		return "TraceResult{" +
				"dir=" + dir +
				", point=" + point +
				", context=" + context +
				'}';
	}
}
