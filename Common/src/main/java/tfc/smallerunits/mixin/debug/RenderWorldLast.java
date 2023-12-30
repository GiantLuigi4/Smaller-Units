package tfc.smallerunits.mixin.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.render.debug.PlayerOffsetRenderHelper;

@Mixin(LevelRenderer.class)
public abstract class RenderWorldLast {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lnet/minecraft/client/Camera;)V"), method = "renderLevel")
	public void postRenderWorld(PoseStack stack, float pct, long tick, boolean idk, Camera camera, GameRenderer renderer, LightTexture lightTexture, Matrix4f matrix, CallbackInfo ci) {
		PlayerOffsetRenderHelper.render(stack, pct, tick, idk, camera, renderer, lightTexture, matrix, ci);
	}
}
