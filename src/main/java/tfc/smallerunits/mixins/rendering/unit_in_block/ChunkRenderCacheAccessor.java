package tfc.smallerunits.mixins.rendering.unit_in_block;

import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRenderCache.class)
public interface ChunkRenderCacheAccessor {
	@Accessor("world")
	World getWorld();
	
	@Accessor("chunks")
	Chunk[][] getChunks();
}
