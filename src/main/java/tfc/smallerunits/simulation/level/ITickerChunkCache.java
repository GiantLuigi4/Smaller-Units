package tfc.smallerunits.simulation.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;

public interface ITickerChunkCache {
	BasicVerticalChunk createChunk(int i, ChunkPos ckPos);
	
	ChunkAccess getChunk(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad);
	
	EmptyLevelChunk getEmpty();
	
	ITickerLevel tickerLevel();
}
