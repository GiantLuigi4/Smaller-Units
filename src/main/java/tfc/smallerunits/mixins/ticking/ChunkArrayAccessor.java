package tfc.smallerunits.mixins.ticking;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;

@Mixin(ClientChunkProvider.ChunkArray.class)
public interface ChunkArrayAccessor {
	@Accessor("chunks")
	AtomicReferenceArray<Chunk> getChunks();
}