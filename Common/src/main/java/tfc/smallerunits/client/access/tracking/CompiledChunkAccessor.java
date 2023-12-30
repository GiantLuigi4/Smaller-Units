package tfc.smallerunits.client.access.tracking;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface CompiledChunkAccessor {
	void SU$setRenderChunk(ChunkRenderDispatcher.RenderChunk chunk);
	
	ChunkRenderDispatcher.RenderChunk SU$getRenderChunk();
}
