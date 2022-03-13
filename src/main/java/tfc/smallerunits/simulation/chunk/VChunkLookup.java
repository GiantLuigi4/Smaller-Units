package tfc.smallerunits.simulation.chunk;

import net.minecraft.world.level.ChunkPos;
import tfc.smallerunits.simulation.world.TickerChunkCache;

public class VChunkLookup {
	final int myPos;
	final BasicVerticalChunk[] chunks;
	final TickerChunkCache tickerChunkCache;
	final ChunkPos ckPos;
	
	public VChunkLookup(TickerChunkCache tickerChunkCache, int myPos, BasicVerticalChunk[] chunks, ChunkPos ckPos) {
		this.tickerChunkCache = tickerChunkCache;
		this.myPos = myPos;
		this.chunks = chunks;
		this.ckPos = ckPos;
	}
	
	public BasicVerticalChunk apply(int i) {
		BasicVerticalChunk vc = chunks[i + myPos];
		if (vc == null) vc = tickerChunkCache.createChunk(i + myPos, ckPos);
		return vc;
	}
}
