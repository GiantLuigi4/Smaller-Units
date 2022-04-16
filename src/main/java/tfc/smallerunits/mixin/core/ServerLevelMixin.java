package tfc.smallerunits.mixin.core;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Shadow
	public ServerChunkCache chunkSource;
	
	@Inject(at = @At("TAIL"), method = "tick")
	public void postTick(BooleanSupplier k, CallbackInfo ci) {
		for (Region value : ((RegionalAttachments) chunkSource.chunkMap).SU$getRegionMap().values().toArray(new Region[0])) {
			if (value == null) continue;
			value.tickWorlds();
		}
	}
}
