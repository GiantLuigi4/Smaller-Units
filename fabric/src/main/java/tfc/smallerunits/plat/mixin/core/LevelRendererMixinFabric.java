package tfc.smallerunits.plat.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.render.TileRendererHelper;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.level.client.TickerClientLevel;

@Mixin(LevelRenderer.class)
public class LevelRendererMixinFabric {
	@Shadow @Final private RenderBuffers renderBuffers;
	
	@Shadow @Nullable public ClientLevel level;
	
	// ok that's a long injection target
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V"), method = "renderLevel")
	public void preRenderParticles(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		for (Region value : ((RegionalAttachments) level).SU$getRegionMap().values()) {
			// TODO: frustum checks
			for (Level valueLevel : value.getLevels()) {
				if (valueLevel != null) {
					if (valueLevel instanceof TickerClientLevel) {
						pPoseStack.pushPose();
						TileRendererHelper.drawParticles(pPoseStack, pPartialTick, pFinishNanoTime, pRenderBlockOutline, pCamera, pGameRenderer, pLightTexture, pProjectionMatrix, value, valueLevel, renderBuffers, ci);
						pPoseStack.popPose();
					}
				}
			}
		}
//		SphereBoxTesting.render(pPoseStack, pPartialTick, renderBuffers, level.getGameTime(), pProjectionMatrix);
	}
}
