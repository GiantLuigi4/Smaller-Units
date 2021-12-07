package tfc.smallerunits.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
	
	public final ForgeConfigSpec.BooleanValue slightlyAsyncCollision;
	public final ForgeConfigSpec.IntValue asyncThreshold;
	public final ForgeConfigSpec.IntValue maxThreads;
	
	public CommonConfig(ForgeConfigSpec.Builder builder) {
		builder.comment("Eventually this'll be moved to client and server separately instead of being common\nThese options mean nothing if collision reversion is not present").push("Collision Settings");
		
		slightlyAsyncCollision = builder
				.comment("If smaller units should be able to use slightly async collision\nThe way I have async collision setup can cause a lot of overhead, especially with smaller thread counts")
				.translation("config.smaller_units.allow_async")
				.define("AllowAsync", false);
		
		asyncThreshold = builder
				.comment("How many blocks must be in a unit before SU will switch over to using async collision retrieval")
				.translation("config.smaller_units.async_threshold")
				.defineInRange("AsyncThreshold", 80, 0, 2048);
		
		maxThreads = builder
				.comment("The max number of threads to use for shape retrieval\nThere is no way to accurately measure the \"ideal\" value for this setting other than to use trial and error and see what performs best")
				.translation("config.smaller_units.thread_limit")
				.defineInRange("ThreadLimit", 4, 1, 16);
		
		builder.pop();
	}
}
