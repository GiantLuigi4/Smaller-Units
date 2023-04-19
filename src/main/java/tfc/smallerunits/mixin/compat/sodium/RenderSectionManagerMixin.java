package tfc.smallerunits.mixin.compat.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.client.access.tracking.sodium.RenderSectionManagerAccessor;

@Mixin(value = RenderSectionManager.class, remap = false)
public class RenderSectionManagerMixin implements RenderSectionManagerAccessor {
	@Shadow
	@Final
	private ChunkRenderList chunkRenderList;
	
	@Override
	public ChunkRenderList SU$getChunkRenderList() {
		return chunkRenderList;
	}
}
