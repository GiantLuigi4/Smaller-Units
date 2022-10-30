package tfc.smallerunits.mixin.data.access;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.CompiledChunkAccessor;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class CompiledChunkMixin implements CompiledChunkAccessor {
	@Unique
	ChunkRenderDispatcher.RenderChunk mixinIShouldNotHaveToDoThis;
	
	@Override
	public void SU$setRenderChunk(ChunkRenderDispatcher.RenderChunk chunk) {
		mixinIShouldNotHaveToDoThis = chunk;
	}
	
	@Override
	public ChunkRenderDispatcher.RenderChunk SU$getRenderChunk() {
		return mixinIShouldNotHaveToDoThis;
	}
}
