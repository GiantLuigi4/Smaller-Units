package badvox;

import net.minecraft.world.phys.Vec3;

public abstract class Traceable {
	public abstract TraceResult trace(Vec3 start, Vec3 end);
	
	public abstract TraceResult traceBack(Vec3 start, Vec3 end);
	
	public abstract boolean intersects(Traceable traceable);
	
	public abstract boolean contains(Vec3 vec);
	
	public abstract Vec3 min();
	
	public abstract Vec3 max();
}
