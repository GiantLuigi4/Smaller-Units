package tfc.smallerunits.mixin.data;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SUChunkRender;

import java.lang.ref.WeakReference;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class CompiledChunkMixin implements SUCompiledChunkAttachments {
	@Unique
	private SUChunkRender compChunk;

	@Unique
	private WeakReference<SUCapableChunk> chnk;
	
	@Override
	public SUCapableChunk getSUCapable() {
		if (chnk == null) return null;
		return chnk.get();
	}
	
	@Override
	public void setSUCapable(int yCoord, SUCapableChunk chunk) {
		if (chunk instanceof EmptyLevelChunk) return;
		chnk = new WeakReference<>(chunk);
		compChunk = chunk.SU$getRenderer(yCoord);
	}

	boolean needsCull = true;

	@Override
	public void markForCull() {
		needsCull = true;
	}

	@Override
	public boolean needsCull() {
		return needsCull;
	}

	public void markCulled()  {
		needsCull = false;
	}

	@Override
	public SUChunkRender SU$getChunkRender() {
		return compChunk;
	}
}
