package tfc.smallerunits.client.render.compat.sodium;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SUChunkRender;

public class SodiumSUAttached implements SUCompiledChunkAttachments {
    ChunkAccess chunk;
    SUChunkRender[] render;

    public SodiumSUAttached(ChunkAccess chunk) {
        this.chunk = chunk;
        render = new SUChunkRender[chunk.getMaxSection() - chunk.getMinSection()];
        for (int i = chunk.getMinSection(); i < chunk.getMaxSection(); i++) {
            render[i - chunk.getMinSection()] = new SUChunkRender((LevelChunk) chunk);
        }
    }

    @Override
    public SUCapableChunk getSUCapable() {
        return (SUCapableChunk) chunk;
    }

    @Override
    public void setSUCapable(int yCoord, SUCapableChunk chunk) {
        throw new RuntimeException("unsupported");
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

    public SUChunkRender SU$getChunkRender(int section) {
        return render[section - chunk.getMinSection()];
    }
}
