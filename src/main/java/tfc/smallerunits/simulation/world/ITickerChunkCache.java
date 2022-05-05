package tfc.smallerunits.simulation.world;

import net.minecraft.world.level.ChunkPos;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;

public interface ITickerChunkCache {
	BasicVerticalChunk createChunk(int i, ChunkPos ckPos);
}
