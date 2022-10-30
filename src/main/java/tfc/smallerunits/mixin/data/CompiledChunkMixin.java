package tfc.smallerunits.mixin.data;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class CompiledChunkMixin implements SUCompiledChunkAttachments {
	@Unique
	private SUCapableChunk chnk;
	
	@Override
	public SUCapableChunk getSUCapable() {
		return chnk;
	}
	
	@Override
	public void setSUCapable(SUCapableChunk chunk) {
		chnk = chunk;
	}
}
