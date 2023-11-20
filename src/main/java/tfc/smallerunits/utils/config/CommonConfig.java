package tfc.smallerunits.utils.config;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfc.smallerunits.utils.config.annoconfg.AnnoCFG;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.*;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.Default;

@Config(type = ModConfig.Type.COMMON)
public class CommonConfig {
    private static final AnnoCFG CFG = new AnnoCFG(FMLJavaModLoadingContext.get().getModEventBus(), CommonConfig.class);

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

    public static void init() {
//		CFG.create(ModConfig.Type.SERVER, ModLoadingContext.get().getActiveNamespace() + "_server.toml");
    }
}
