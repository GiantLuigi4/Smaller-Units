package tfc.smallerunits.utils.config.annoconfg.forge;

import net.minecraftforge.fml.config.IConfigSpec;
import tfc.smallerunits.utils.config.annoconfg.Config;

import java.io.File;

public class ForgeConfig extends Config {
	//@formatter:off
	IConfigSpec<?> configSpec;
	public ForgeConfig(IConfigSpec<?> configSpec) { this.configSpec = configSpec; }
	@Override public void write(File fl) { }
	@Override public void read(File fl) { }
	public IConfigSpec<?> getConfigSpec() { return configSpec; }
	//@formatter:on
}
