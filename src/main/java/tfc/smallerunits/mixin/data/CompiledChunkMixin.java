package tfc.smallerunits.mixin.data;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;

import java.lang.ref.WeakReference;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class CompiledChunkMixin implements SUCompiledChunkAttachments {
	@Unique
	private WeakReference<SUCapableChunk> chnk;
	
	@Override
	public SUCapableChunk getSUCapable() {
		if (chnk == null) return null;
		return chnk.get();
	}
	
	@Override
	public void setSUCapable(SUCapableChunk chunk) {
		chnk = new WeakReference<>(chunk);
	}
}
