package tfc.smallerunits.mixin.core;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;

import java.util.function.BooleanSupplier;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(at = @At("TAIL"), method = "tick")
	public void postTick(BooleanSupplier k, CallbackInfo ci) {
		for (Region value : ((RegionalAttachments) this).SU$getRegionMap().values().toArray(new Region[0])) {
			if (value == null) continue;
			value.tickWorlds();
		}
	}
}
