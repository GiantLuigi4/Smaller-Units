package tfc.smallerunits.client.render.compat.sodium;

import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;

public class SodiumSUAttached implements SUCompiledChunkAttachments {
    LevelChunk chunk;

    public SodiumSUAttached(LevelChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public SUCapableChunk getSUCapable() {
        return (SUCapableChunk) chunk;
    }

    @Override
    public void setSUCapable(SUCapableChunk chunk) {

    }
}
