package tfc.smallerunits.mixins;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.helpers.BufferCacheHelper;
import tfc.smallerunits.helpers.GameRendererHelper;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(at = @At("RETURN"), method = "updateCameraAndRender(FJZ)V")
	public void onRender(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo ci) {
		BufferCacheHelper.needsRefresh = true;
	}
	
	@Inject(at = @At("HEAD"), method = "resetProjectionMatrix")
	public void SmallerUnits_preSetProjectionMatrix(Matrix4f matrixIn, CallbackInfo ci) {
		GameRendererHelper.matrix = matrixIn;
	}
}
