package tfc.smallerunits.utils.world;

import net.minecraft.profiler.IProfileResult;
import net.minecraft.profiler.Profiler;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class BlankProfiler extends Profiler {
	public BlankProfiler(LongSupplier p_i231482_1_, IntSupplier p_i231482_2_, boolean p_i231482_3_) {
		super(p_i231482_1_, p_i231482_2_, p_i231482_3_);
	}
	
	@Override
	public void startTick() {
	}
	
	@Override
	public void endTick() {
	}
	
	@Override
	public void startSection(String name) {
	}
	
	@Override
	public void startSection(Supplier<String> nameSupplier) {
	}
	
	@Override
	public void endSection() {
	}
	
	@Override
	public void endStartSection(String name) {
	}
	
	@Override
	public void endStartSection(Supplier<String> nameSupplier) {
	}
	
	@Override
	public void func_230035_c_(String p_230035_1_) {
	}
	
	@Override
	public void func_230036_c_(Supplier<String> p_230036_1_) {
	}
	
	@Override
	public IProfileResult getResults() {
		return super.getResults();
	}
}
