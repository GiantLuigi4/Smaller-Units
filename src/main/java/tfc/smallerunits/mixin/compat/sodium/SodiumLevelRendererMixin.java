package tfc.smallerunits.mixin.compat.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.abstraction.SodiumFrustum;
import tfc.smallerunits.client.compat.SodiumRenderer;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class SodiumLevelRendererMixin {
	@Shadow
	private RenderSectionManager renderSectionManager;
	
	@Shadow
	private ClientLevel world;
	
	@Shadow
	@Final
	private Minecraft client;
	
	@Unique
	SodiumFrustum frustum = new SodiumFrustum();
	
	@Inject(at = @At("TAIL"), method = "updateChunks")
	public void postUpdateChunks(Camera camera, me.jellysquid.mods.sodium.client.util.frustum.Frustum frustum, int frame, boolean spectator, CallbackInfo ci) {
		this.frustum.set(frustum);
	}
	
	@Inject(at = @At("TAIL"), method = "drawChunkLayer")
	public void preRenderSomething(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci) {
		SodiumRenderer.render(
				renderLayer, matrixStack, x, y, z, ci,
				frustum, client, world, renderSectionManager
		);
	}
}
