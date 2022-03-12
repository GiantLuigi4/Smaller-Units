package tfc.smallerunits.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
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
	
	@Unique
	double pCamX, pCamY, pCamZ;
	
	@Inject(at = @At("HEAD"), method = "renderChunkLayer")
	public void preStartDraw(RenderType j, PoseStack d0, double d1, double d2, double i, Matrix4f k, CallbackInfo ci) {
		pCamX = d1;
		pCamY = d2;
		pCamZ = i;
	}
	
	// TODO: move off of redirect
	// even js coremods are better than a redirect imo
	// granted those aren't exactly able to be ported to fabric
	// and if I'm not gonna be the one porting SU to fabric, I don't wanna force someone else to port js coremods to fabric
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getCompiledChunk()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;"))
	public ChunkRenderDispatcher.CompiledChunk preRenderLayer(ChunkRenderDispatcher.RenderChunk instance) {
		return (renderChunk = instance).getCompiledChunk();
	}
	
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;isEmpty(Lnet/minecraft/client/renderer/RenderType;)Z"))
	public boolean preDrawLayer(ChunkRenderDispatcher.CompiledChunk instance, RenderType pRenderType) {
		ShaderInstance shaderinstance = RenderSystem.getShader();
		Uniform uniform = shaderinstance.CHUNK_OFFSET;
		
		BlockPos origin = renderChunk.getOrigin();
		ChunkRenderDispatcher.CompiledChunk chunk = renderChunk.compiled.get();
		SUCapableChunk capable = ((SUCompiledChunkAttachments) chunk).getSUCapable();
		if (capable == null)
			((SUCompiledChunkAttachments) chunk).setSUCapable(capable = ((SUCapableChunk) level.getChunk(origin)));
		
		if (uniform != null) {
			uniform.set((float) ((double) origin.getX() - pCamX), (float) ((double) origin.getY() - pCamY), (float) ((double) origin.getZ() - pCamZ));
			uniform.upload();
		}
		
		SURenderManager.drawChunk(((LevelChunk) capable), level, renderChunk, pRenderType);
		return instance.isEmpty(pRenderType);
	}
}
