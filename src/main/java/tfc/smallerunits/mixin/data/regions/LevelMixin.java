package tfc.smallerunits.mixin.data.regions;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;

@Mixin(Level.class)
public class LevelMixin {
	@Inject(at = @At("HEAD"), method = "close")
	public void preClose(CallbackInfo ci) {
		if (this instanceof RegionalAttachments attachments) {
			for (Region value : attachments.SU$getRegionMap().values()) {
				value.close();
			}
		}
	}
}
