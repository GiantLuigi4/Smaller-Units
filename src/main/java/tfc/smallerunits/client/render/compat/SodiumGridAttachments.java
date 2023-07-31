package tfc.smallerunits.client.render.compat;

import net.minecraft.world.level.ChunkPos;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;

import java.util.HashMap;

public interface SodiumGridAttachments {
	HashMap<ChunkPos, SUCompiledChunkAttachments> getRenderChunks();
}
