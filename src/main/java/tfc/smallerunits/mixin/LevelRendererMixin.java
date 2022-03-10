package tfc.smallerunits.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.render.SURenderManager;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.client.tracking.SUCompiledChunkAttachments;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	@Nullable
	private ClientLevel level;
	@Unique
	public ChunkRenderDispatcher.RenderChunk renderChunk;
	
	// TODO: move off of redirect
	// even js coremods are better than a redirect imo
	// granted those aren't exactly able to be ported to fabric
	// and if I'm not gonna be the one porting SU to fabric, I don't wanna force someone else to port js coremods to fabric
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexBuffer;"))
	public VertexBuffer preRenderLayer(ChunkRenderDispatcher.RenderChunk instance, RenderType p_112808_) {
		return (renderChunk = instance).getBuffer(p_112808_);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawChunkLayer()V"), method = "renderChunkLayer")
	public void preDrawLayer(RenderType j, PoseStack d0, double d1, double d2, double i, Matrix4f k, CallbackInfo ci) {
		BlockPos origin = renderChunk.getOrigin();
		ChunkRenderDispatcher.CompiledChunk chunk = renderChunk.compiled.get();
		SUCapableChunk capable = ((SUCompiledChunkAttachments) chunk).getSUCapable();
		if (capable == null)
			((SUCompiledChunkAttachments) chunk).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		SURenderManager.drawChunk(((LevelChunk) capable), level, renderChunk, j);
	}
}
