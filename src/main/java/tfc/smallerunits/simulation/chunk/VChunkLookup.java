package tfc.smallerunits.simulation.chunk;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import tfc.smallerunits.simulation.level.ITickerChunkCache;

public class VChunkLookup {
	final int myPos;
	final BasicVerticalChunk[] chunks;
	final ITickerChunkCache tickerChunkCache;
	final ChunkPos ckPos;
	
	final int maxPos;
	
	public VChunkLookup(ITickerChunkCache tickerChunkCache, int myPos, BasicVerticalChunk[] chunks, ChunkPos ckPos, int maxPos) {
		this.tickerChunkCache = tickerChunkCache;
		this.myPos = myPos;
		this.chunks = chunks; // TODO: should have a padding of 2 to both sides
		this.ckPos = ckPos;
		this.maxPos = maxPos;
	}
	
	public BasicVerticalChunk apply(int i) {
		return applyAbs(i + myPos);
	}
	
	public BasicVerticalChunk applyAbs(int i) {
		if (i < 0 || i >= maxPos) {
			ChunkAccess chunk = tickerChunkCache.getChunk(ckPos.x, i, ckPos.z, ChunkStatus.FULL, true); // TODO: make it so this doesn't have to be true?;
			if (chunk instanceof BasicVerticalChunk vc) return vc;
			return null;
		}
		BasicVerticalChunk vc = chunks[i];
		if (vc == null) vc = chunks[i] = tickerChunkCache.createChunk(i, ckPos);
		return vc;
	}
}
