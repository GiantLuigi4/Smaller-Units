//#if FORGE==1
//$$package tfc.smallerunits.utils.config.annoconfg.forge;
//$$
//$$import net.minecraftforge.common.ForgeConfigSpec;
//$$import tfc.smallerunits.utils.config.annoconfg.builder.CategoryBuilder;
//$$
//$$import java.util.function.Supplier;
//$$
//$$public class ForgeCategoryBuilder extends CategoryBuilder {
//$$	String root;
//$$	ForgeConfigSpec.Builder builder;
//$$
//$$	public ForgeCategoryBuilder(String root, ForgeConfigSpec.Builder builder) {
//$$		this.root = root;
//$$		this.builder = builder;
//$$	}
//$$
//$$	@Override
//$$	public CategoryBuilder subcategory(String translationName) {
//$$		builder.push(translationName.substring(root.length() + 1));
//$$		return new ForgeCategoryBuilder(translationName, builder);
//$$	}
//$$
//$$	@Override
//$$	public void finish(CategoryBuilder builder2) {
//$$		if (builder2 instanceof ForgeCategoryBuilder builder) {
//$$			builder.builder.pop();
//$$		} else {
//$$			throw new RuntimeException("Can only add forge categories to a category");
//$$		}
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Integer> intRange(String translationName, int min, int defaultV, int max) {
//$$		return builder.defineInRange(translationName.substring(root.length() + 1), defaultV, min, max);
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Integer> intValue(String translationName, int v) {
//$$		return builder.define(translationName.substring(root.length() + 1), v);
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Boolean> boolValue(String translationName, boolean v) {
//$$		return builder.define(translationName.substring(root.length() + 1), v);
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Long> longRange(String translationName, long min, long defaultV, long max) {
//$$		return builder.defineInRange(translationName.substring(root.length() + 1), defaultV, min, max);
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Long> longValue(String translationName, long v) {
//$$		return builder.define(translationName.substring(root.length() + 1), v);
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Double> doubleRange(String translationName, double min, double defaultV, double max) {
//$$		return builder.defineInRange(translationName.substring(root.length() + 1), defaultV, min, max);
//$$	}
//$$
//$$	@Override
//$$	public Supplier<Double> doubleValue(String translationName, double v) {
//$$		return builder.define(translationName.substring(root.length() + 1), v);
//$$	}
//$$}
//#endif
