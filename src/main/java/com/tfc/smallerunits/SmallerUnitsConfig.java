package com.tfc.smallerunits;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SmallerUnitsConfig {
	public static final ClientConfig CLIENT;
	static final ForgeConfigSpec clientSpec;
	public static final SmallerUnitsConfig SERVER;
	static final ForgeConfigSpec serverSpec;
	
	static {
		{
			final Pair<SmallerUnitsConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SmallerUnitsConfig::new);
			serverSpec = specPair.getRight();
			SERVER = specPair.getLeft();
		}
		
		{
			final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
			clientSpec = specPair.getRight();
			CLIENT = specPair.getLeft();
		}
	}
	
	public final ForgeConfigSpec.IntValue minUPB;
	public final ForgeConfigSpec.IntValue maxUPB;
	public final ForgeConfigSpec.IntValue lightingUpdatesPerTick;
	
	public SmallerUnitsConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Crafting related stuff").push("Recipe Settings");
		
		minUPB = builder
				.comment("Minimum Units Per Block")
				.comment("Default: 2")
				.comment("Minimum: 1")
				.comment("Maximum: 16")
				.translation("config.smaller_units.min_upb")
				.defineInRange("MinUPB", 2, 1, 16);
		
		maxUPB = builder
				.comment("Maximum Units Per Block")
				.comment("The more units in one block, the laggier it is on client")
				.comment("Serverside, it's relatively unimpactful")
				.comment("")
				.comment("Default: 16")
				.comment("Minimum: 2")
				.comment("Maximum: 256")
				.translation("config.smaller_units.max_upb")
				.defineInRange("MaxUPB", 16, 2, 256);
		
		builder.pop();
		
		builder.comment("Performance And Quality related stuff").push("Performance Settings");
		
		lightingUpdatesPerTick = builder
				.comment("How many lighting updates should occur on each small unit block per tick, lower values might cause plants to grow in dark spots but cause less lag, higher values cause more lag but work better")
				.comment("Default: 10")
				.comment("Minimum: 1")
				.comment("Maximum: 200")
				.translation("config.smaller_units.lighting_updates_per_tick")
				.defineInRange("LightingUpdatesPerTick", 10, 1, 200);
		
		builder.pop();
	}
	
	public static class ClientConfig {
		public final ForgeConfigSpec.BooleanValue useExperimentalRenderer;
		public final ForgeConfigSpec.BooleanValue useVBOS;
		public final ForgeConfigSpec.BooleanValue useExperimentalSelection;
		public final ForgeConfigSpec.IntValue lightingUpdatesPerFrame;
		
		public ClientConfig(ForgeConfigSpec.Builder builder) {
			builder.comment("Defaults are the \"ideal\" settings").push("Rendering Settings");
			
			useExperimentalRenderer = builder
					.comment("Potentially buggy, but better performance")
					.translation("config.smaller_units.experimental_renderer")
					.define("ExperimentalRenderer", false);
			useVBOS = builder
					.comment("Buggy, higher GPU usage, better performance")
					.translation("config.smaller_units.experimental_renderer")
					.define("ExperimentalRenderer", false);
			useExperimentalSelection = builder
					.comment("Less likely to select the block behind the selected unit block, but work in progress")
					.translation("config.smaller_units.experimental_selection")
					.define("ExperimentalSelection", false);
			lightingUpdatesPerFrame = builder
					.comment("How many lighting updates should occur on each small unit block per frame, lower values look worse but cause less lag, higher values cause more lag but look better")
					.comment("Default: 10")
					.comment("Minimum: 1")
					.comment("Maximum: 200")
					.translation("config.smaller_units.lighting_updates_per_frame")
					.defineInRange("LightingUpdatesPerFrame", 10, 1, 200);
			
			builder.pop();
		}
	}
}
