package tfc.smallerunits.plat.config.fabric;

import java.util.function.Supplier;

public abstract class CategoryBuilder {
	public abstract CategoryBuilder subcategory(String translationName);
	
	public abstract void finish(CategoryBuilder builder2);
	
	public abstract Supplier<Integer> intRange(String translationName, int min, int defaultV, int max);
	
	public abstract Supplier<Integer> intValue(String translationName, int defaultV);
	
	public abstract Supplier<Boolean> boolValue(String translationName, boolean valueBoolean);
	
	public abstract Supplier<Long> longRange(String translationName, long min, long v, long max);
	
	public abstract Supplier<Long> longValue(String translationName, long v);
	
	public abstract Supplier<Double> doubleRange(String translationName, double min, double v, double max);
	
	public abstract Supplier<Double> doubleValue(String translationName, double v);
}