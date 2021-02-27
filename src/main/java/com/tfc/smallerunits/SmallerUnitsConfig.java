package com.tfc.smallerunits;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SmallerUnitsConfig {
	public static final SmallerUnitsConfig SERVER;
	static final ForgeConfigSpec serverSpec;
	
	static {
		final Pair<SmallerUnitsConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SmallerUnitsConfig::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}
	
	public final ForgeConfigSpec.IntValue minUPB;
	public final ForgeConfigSpec.IntValue maxUPB;
	
	public SmallerUnitsConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Server Settings").push("Server");
		
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
		builder.pop();
	}
}
