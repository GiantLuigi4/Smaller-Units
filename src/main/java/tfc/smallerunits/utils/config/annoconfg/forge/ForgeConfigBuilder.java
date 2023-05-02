//#if FORGE==1
//$$package tfc.smallerunits.utils.config.annoconfg.forge;
//$$
//$$import net.minecraft.client.gui.screens.Screen;
//$$import net.minecraftforge.common.ForgeConfigSpec;
//$$import tfc.smallerunits.utils.config.annoconfg.Config;
//$$import tfc.smallerunits.utils.config.annoconfg.builder.CategoryBuilder;
//$$import tfc.smallerunits.utils.config.annoconfg.builder.CfgBuilder;
//$$
//$$import java.util.function.Function;
//$$
//$$public class ForgeConfigBuilder extends CfgBuilder {
//$$	String root;
//$$	ForgeConfigSpec.Builder builder;
//$$
//$$	public ForgeConfigBuilder(String root) {
//$$		this.root = root;
//$$		this.builder = new ForgeConfigSpec.Builder();
//$$	}
//$$
//$$	@Override
//$$	public CategoryBuilder rootCategory() {
//$$		return new ForgeCategoryBuilder(root, builder);
//$$	}
//$$
//$$	@Override
//$$	public void setSaveFunction(Runnable onConfigChange) {
//$$	}
//$$
//$$	@Override
//$$	public Function<Screen, Screen> createScreen() {
//$$		return null;
//$$	}
//$$
//$$	@Override
//$$	public Config build() {
//$$		return new ForgeConfig(builder.build());
//$$	}
//$$}
//#endif
