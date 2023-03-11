package tfc.smallerunits.utils.config;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfc.smallerunits.utils.config.annoconfg.AnnoCFG;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.*;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.Default;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.DoubleRange;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.IntRange;

@Config(type = ModConfig.Type.SERVER)
public class ServerConfig {
	private static boolean getFalse() {
		return false;
	}
	
	private static final AnnoCFG CFG = new AnnoCFG(FMLJavaModLoadingContext.get().getModEventBus(), ServerConfig.class);
	
	@Comment({
			"Restrictions on Units Per Block",
			"Serverside, this has relatively little impact",
			"Clientside, I need to do some optimizing",
			"However, it takes exponentially more RAM",
	})
	@CFGSegment("scale_restrictions")
	public static class SizeOptions {
		@Name("DefaultUPB")
		@Comment("The scale for the unit shrinker to default to when placed into the crafting grid alone")
		@Translation("config.smaller_units.def_upb")
		@IntRange(minV = 1, maxV = 128)
		@Default(valueI = 4)
		public static final int defaultScale = "".hashCode();
		
		@Name("MinimumUPB")
		@Comment({
				"The scale for the recipe to be limited to",
				"This should be set to the lowest divisor you want to be craftable"
		})
		@Translation("config.smaller_units.min_upb")
		@IntRange(minV = 1, maxV = 128)
		@Default(valueI = 2)
		public static final int minScale = "".hashCode();
		
		@Name("MaximumUPB")
		@Comment({
				"The scale for the recipe to be limited to",
				"This should be set to the highest divisor you want to be craftable"
		})
		@Translation("config.smaller_units.max_upb")
		@IntRange(minV = 1, maxV = 128)
		@Default(valueI = 16)
		public static final int maxScale = "".hashCode();
	}
	
	@Comment({
			"Settings relating to gameplay"
	})
	@CFGSegment("gameplay_options")
	public static class GameplayOptions {
		@Name("RescaleOthers")
		@Comment("If players should be able to resize other entities via hitting them with resizing hammers")
		@Translation("config.smaller_units.resize_other")
		@Default(valueBoolean = true)
		public static final boolean resizeOther = !getFalse();
		
		@Name("RescaleSelf")
		@Comment("If players should be able to resize themself via sneak+right clicking with resizing hammers")
		@Translation("config.smaller_units.resize_self")
		@Default(valueBoolean = true)
		public static final boolean resizeSelf = !getFalse();
		
		// TODO: I don't know how to disable entity damaging for a specific item
		@Skip
		@Name("HurtOthers")
		@Comment("If the resizing hammer should be able to deal damage when being used on another entity")
		@Translation("config.smaller_units.hurt_other")
		@Default(valueBoolean = true)
		public static final boolean hurtOther = !getFalse();
		
		@Comment("Restrictions on the growing/shrinking hammers being used on players")
		@CFGSegment("hammer_restrictions")
		public static class EntityScaleOptions {
			@Name("MinSize")
			@Comment("The smallest scale a player can resize themself to")
			@Translation("config.smaller_units.min_size")
			@Default(valueD = 1 / 8d)
			public static final double minSize = "".hashCode();
			
			@Name("MaxSize")
			@Comment("The largest scale a player can resize themself to")
			@Translation("config.smaller_units.max_size")
			@Default(valueD = 2d)
			public static final double maxSize = "".hashCode();
			
			@Name("UpscaleRate")
			@Comment("The rate at which the player or entities get upscaled by a hammer")
			@Translation("config.smaller_units.upscale_rate")
			@Default(valueD = 1 / 2d)
			public static final double upscaleRate = "".hashCode();
			
			@Name("DownscaleRate")
			@Comment("The rate at which the player or entities get downscaled by a hammer")
			@Translation("config.smaller_units.downscale_rate")
			@Default(valueD = 1 / 8d)
			public static final double downscaleRate = "".hashCode();
		}
		
		@Comment("Options for VR players")
		@CFGSegment("vr_options")
		public static class VROptions {
			@Name("PistonBlocking")
			@Comment("Whether or not VR players should be able to use their arms to block small flying machines")
			@Translation("config.smaller_units.vr.piston_blocking")
			@Default(valueBoolean = false)
			public static final boolean pistonBlocking = getFalse();
			
			@Name("VanillaPistonBlocking")
			@Comment("Whether or not SU should tweak vanilla (1/1 scale pistons) to allow those to be blocked by VR players")
			@Translation("config.smaller_units.vr.vanilla_piston_blocking")
			@Default(valueBoolean = false)
			@Skip
			public static final boolean vanillaPistonBlocking = getFalse();
			
			@Name("BlockThreshold")
			@Comment("How much larger a player needs to be than a piston in order to be able to block it")
			@Translation("config.smaller_units.vr.block_threshold")
			@Default(valueD = 10)
			@DoubleRange(minV = 0, maxV = Double.POSITIVE_INFINITY)
			public static final double blockThreshold = "".hashCode();
		}
	}
	
	public static void init() {
//		CFG.create(ModConfig.Type.SERVER, ModLoadingContext.get().getActiveNamespace() + "_server.toml");
	}
}
