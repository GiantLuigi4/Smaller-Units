package tfc.smallerunits.mixin.data.access;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.client.tracking.CompiledChunkAccessor;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class RenderChunkMixin {
	@Inject(method = "getCompiledChunk", at = @At("RETURN"))
	public void postGetCompiledChunk(CallbackInfoReturnable<ChunkRenderDispatcher.CompiledChunk> cir) {
		((CompiledChunkAccessor) cir.getReturnValue()).SU$setRenderChunk((ChunkRenderDispatcher.RenderChunk) (Object) this);
	}
}
