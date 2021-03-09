package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.helpers.BufferCacheHelper;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(at = @At("RETURN"), method = "updateCameraAndRender(FJZ)V")
	public void onRender(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo ci) {
		BufferCacheHelper.needsRefresh = true;
	}
}
