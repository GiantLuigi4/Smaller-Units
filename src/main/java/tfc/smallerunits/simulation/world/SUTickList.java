package tfc.smallerunits.simulation.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Supplier;

// TODO: this should be more of a tick manager than a tick list
public class SUTickList<T> extends LevelTicks<T> {
	// TODO
	
	ArrayList<ScheduledTick<T>> ticks = new ArrayList<>();
	long tick = 0;
	
	public SUTickList(LongPredicate p_193211_, Supplier<ProfilerFiller> p_193212_) {
		super(p_193211_, p_193212_);
	}
	
	@Override
	public void tick(long time, int maxTicks, BiConsumer<BlockPos, T> ticker) {
		tick = time;
		// TODO: max ticks
		ArrayList<ScheduledTick<T>> toRemove = new ArrayList<>();
		//noinspection unchecked
		ScheduledTick<T>[] ticks = this.ticks.toArray(new ScheduledTick[0]);
		for (ScheduledTick<T> tScheduledTick : ticks) {
			if (tScheduledTick.triggerTick() <= this.tick) {
//				toRemove.add(tScheduledTick);
				this.ticks.remove(tScheduledTick);
				ticker.accept(tScheduledTick.pos(), tScheduledTick.type());
			}
		}
//		toRemove.forEach(this.ticks::remove);
	}
	
	@Override
	public boolean willTickThisTick(BlockPos pos, T type) {
		for (ScheduledTick<T> tick : ticks) {
			if (tick.type() == type && tick.pos().equals(pos)) {
				return tick.triggerTick() <= this.tick;
			}
		}
		return false;
	}
	
	@Override
	public void schedule(ScheduledTick<T> p_193428_) {
		ticks.add(p_193428_);
	}
	
	@Override
	public boolean hasScheduledTick(BlockPos pos, T type) {
		for (ScheduledTick<T> tick : ticks) {
			if (tick.type() == type && tick.pos().equals(pos)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int count() {
		return ticks.size();
	}
	
	public ArrayList<ScheduledTick<T>> getTicksInArea(AABB aabb) {
		ArrayList<ScheduledTick<T>> ticksL = new ArrayList<>();
		// TODO: optimization
		ScheduledTick<T>[] ticks = this.ticks.toArray(new ScheduledTick[0]);
		for (ScheduledTick<T> tScheduledTick : ticks) {
			if (aabb.contains(tScheduledTick.pos().getX(), tScheduledTick.pos().getY(), tScheduledTick.pos().getZ())) {
				ticksL.add(tScheduledTick);
			}
		}
		return ticksL;
	}
	
	public void clearBox(AABB box) {
		ArrayList<ScheduledTick<T>> toRemove = new ArrayList<>();
		//noinspection unchecked
		ScheduledTick<T>[] ticks = this.ticks.toArray(new ScheduledTick[0]);
		for (ScheduledTick<T> tScheduledTick : ticks) {
			if (box.contains(tScheduledTick.pos().getX(), tScheduledTick.pos().getY(), tScheduledTick.pos().getZ())) {
				toRemove.add(tScheduledTick);
			}
		}
		toRemove.forEach(this.ticks::remove);
	}
}
