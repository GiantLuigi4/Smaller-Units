package tfc.smallerunits.utils.config;

import tfc.smallerunits.plat.config.AnnoCFG;
import tfc.smallerunits.utils.config.annoconfg.ConfigSide;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.*;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.Default;

@Config(type = ConfigSide.COMMON, namespace = "smallerunits")
public class CommonConfig {
    private static final AnnoCFG CFG = new AnnoCFG(CommonConfig.class);
	
	private static boolean getFalse() {
        return false;
    }

    @Comment("Options for debug")
    @CFGSegment("debug_options")
    public static class DebugOptions {
        @Name("crash_on_null_interacter")
        @Comment("If this option is on, SU will deliberately crash the game when a null interacting entity is added")
        @Translation("config.smaller_units.crash_on_null_interacter")
        @Default(valueBoolean = true)
        public static final boolean crashOnNullInteracter = !getFalse();
    }
    
    @Name("disable_version_check")
    @Comment("Disables network version checking. Turning this off allows fabric clients to join forge servers and vice versa, but also allows mismatches network versions, which will cause problems.")
    @Translation("config.smaller_units.disable_net_check")
    @Default(valueBoolean = false)
    public static boolean disableVersionCheck = getFalse();

    public static void init() {
//		CFG.create(ModConfig.Type.SERVER, ModLoadingContext.get().getActiveNamespace() + "_server.toml");
    }
}
