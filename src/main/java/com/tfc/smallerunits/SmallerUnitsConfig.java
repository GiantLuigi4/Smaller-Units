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
	public final ForgeConfigSpec.BooleanValue usePacketHandlerHacks;
	
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
		
		builder.comment("Potentially Broken Stuff, mostly invasive stuff").push("Compatibility Hacks");
		
		builder.pop();
		
		usePacketHandlerHacks = builder
				.comment("Whether or not Smaller Units should use a hacky method of tying packets sent by small blocks to the tile entity of the small world which holds the block\nMay be very broken with some mods")
				.translation("config.smaller_units.packet_handler_hacks")
				.define("PacketHandlerHacks", true);
		
		builder.pop();
	}
	
	public static class ClientConfig {
		public final ForgeConfigSpec.BooleanValue useExperimentalRenderer;
		public final ForgeConfigSpec.BooleanValue useExperimentalRendererPt2;
		public final ForgeConfigSpec.BooleanValue useVBOS;
		public final ForgeConfigSpec.BooleanValue useExperimentalSelection;
		public final ForgeConfigSpec.IntValue lightingUpdatesPerFrame;
		
		public ClientConfig(ForgeConfigSpec.Builder builder) {
			builder.comment("Defaults are the \"ideal\" settings").push("Rendering Settings");
			
			useExperimentalRenderer = builder
					.comment("Potentially buggy, but may give slightly better performance (may lead to jvm crashes)")
					.translation("config.smaller_units.experimental_renderer")
					.define("ExperimentalRenderer", true);
			useExperimentalRendererPt2 = builder
					.comment("Most likely buggy, but potentially better performance (incompatible with fabulous graphics and maybe also shaders)")
					.translation("config.smaller_units.experimental_renderer_pt2")
					.define("ExperimentalRendererPt2", false);
			useVBOS = builder
					.comment("Buggy, higher GPU usage, better performance (will destroy your game if there are too many unit tile entities or you reload textures too much)")
					.translation("config.smaller_units.use_vbos")
					.define("UseVOBS", false);
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
