package tfc.smallerunits.client.render.compat.sodium;

import net.minecraft.world.level.chunk.ChunkAccess;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SUChunkRender;

public class SodiumSUAttached implements SUCompiledChunkAttachments {
    ChunkAccess chunk;

    public SodiumSUAttached(ChunkAccess chunk) {
        this.chunk = chunk;
    }

    @Override
    public SUCapableChunk getSUCapable() {
        return (SUCapableChunk) chunk;
    }

    @Override
    public void setSUCapable(SUCapableChunk chunk) {

    }

    @Override
    public void markForCull() {
        throw new RuntimeException("TODO");
    }

    @Override
    public boolean needsCull() {
        return true;
    }

    public void markCulled()  {
        throw new RuntimeException("TODO");
    }

    @Override
    public SUChunkRender SU$getChunkRender() {
        throw new RuntimeException("TODO");
    }
}
