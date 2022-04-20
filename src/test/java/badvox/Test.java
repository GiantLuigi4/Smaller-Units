package badvox;

import badvox.traceables.Negation;
import badvox.traceables.NonTraceable;
import badvox.traceables.TraceableBox;
import badvox.traceables.TraceableList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;

public class Test {
	public static void main(String[] args) {
//		TraceableBox box0 = new TraceableBox(new AABB(0, 0, 0, 1, 1, 1));
//		TraceableBox box1 = new TraceableBox(new AABB(0, 0, 0, 0.4, 0.4, 0.4));
//		TraceableBox box2 = new TraceableBox(new AABB(0.6, 0.6, 0.6, 1, 1, 1));
		Traceable box0 = new TraceableBox(new AABB(0.5, 0, 0, 1, 1, 0.5));
		Traceable box1 = new TraceableBox(new AABB(0, 0, 0.5, 0.5, 1, 1));
		Traceable box2 = new TraceableBox(new AABB(0, 0.5, 0, 0.5, 1, 0.5));
		Traceable box3 = new TraceableBox(new AABB(0.5, 0.5, 0.5, 1, 0.5, 1));

//		Traceable traceable = new Negation(box0, box1);
//		traceable = new Negation(traceable, box2);
		TraceableList traceable = new TraceableList();
		traceable.addTraceable(box0);
		traceable.addTraceable(box1);
		traceable.addTraceable(box2);
		traceable.addTraceable(box3);
		System.out.println(traceable.trace(new Vec3(-2, -2, -2), new Vec3(2, 2, 2)));
		System.out.println(traceable.trace(new Vec3(2, 2, 2), new Vec3(-2, -2, -2)));
	}
	
	public Traceable combine(BooleanOp op, Traceable first, Traceable... others) {
		if (op == BooleanOp.AND) {
			// TODO: I don't actually currently know how to do this
		} else if (op == BooleanOp.OR) {
			TraceableList list = new TraceableList();
			list.addTraceable(first);
			for (Traceable other : others) list.addTraceable(other);
			return list;
		} else if (op == BooleanOp.ONLY_FIRST) {
			Traceable last = first;
			for (Traceable other : others) last = new Negation(last, other);
			return last;
		} else if (op == BooleanOp.ONLY_SECOND) {
			if (others.length == 0) throw new RuntimeException("Cannot use ONLY_SECOND without a second box");
			Traceable last = others[1];
			others[1] = first;
			for (Traceable other : others) last = new Negation(last, other);
			return last;
		} else if (op == BooleanOp.FIRST) return first;
		else if (op == BooleanOp.SECOND) {
			if (others.length == 0) throw new RuntimeException("Cannot use SECOND without a second box");
			return others[1];
		} else if (op == BooleanOp.FALSE) {
			return new NonTraceable();
		} else if (op == BooleanOp.TRUE) {
			return new TraceableBox(new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
		} else if (op == BooleanOp.NOT_FIRST) {
			return new Negation(combine(BooleanOp.TRUE, null), first);
		} else if (op == BooleanOp.NOT_SECOND) {
			if (others.length == 0) throw new RuntimeException("Cannot use NOT_SECOND without a second box");
			return combine(BooleanOp.NOT_FIRST, others[1]);
		} else if (op == BooleanOp.NOT_OR) {
			return new Negation(combine(BooleanOp.TRUE, null), combine(BooleanOp.OR, first, others));
		} else if (op == BooleanOp.NOT_AND) {
			return new Negation(combine(BooleanOp.TRUE, null), combine(BooleanOp.AND, first, others));
		} else if (op == BooleanOp.NOT_SAME) {
			Traceable or = combine(BooleanOp.OR, first, others);
			Traceable and = combine(BooleanOp.AND, first, others);
			return new Negation(or, and);
		} else if (op == BooleanOp.SAME) {
			return combine(BooleanOp.NOT_FIRST, combine(BooleanOp.NOT_SAME, first, others));
		} else if (op == BooleanOp.CAUSES) {
			return new Negation(combine(BooleanOp.TRUE, null), combine(BooleanOp.ONLY_FIRST, first, others));
		} else if (op == BooleanOp.CAUSED_BY) {
			return new Negation(combine(BooleanOp.TRUE, null), combine(BooleanOp.ONLY_SECOND, first, others));
		}
		return null;
	}
}
