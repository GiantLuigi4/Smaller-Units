package tfc.smallerunits.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.Math1D;

public class GeneralUtils {
//	public static BlockPos getParentOffset(Level level, BlockPos pos) {
//		// TODO:
//	}
	
	public static BlockPos getParentPos(BlockPos pPos, ITickerLevel tickerWorld) {
		BlockPos rPos = tickerWorld.getRegion().pos.toBlockPos();
		int j = pPos.getX();
		int k = pPos.getY();
		int l = pPos.getZ();
		int xo = (j / tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset(k, tickerWorld.getUPB());
		int zo = (l / tickerWorld.getUPB());
		return rPos.offset(new BlockPos(xo, yo, zo));
	}
	
	public static BlockPos getParentPos(BlockPos pPos, BasicVerticalChunk verticalChunk) {
		ITickerLevel tickerWorld = (ITickerLevel) verticalChunk.getLevel();
		BlockPos rPos = tickerWorld.getRegion().pos.toBlockPos();
		int j = pPos.getX() & 15;
		int k = pPos.getY();
		int l = pPos.getZ() & 15;
		ChunkPos chunkPos = verticalChunk.getPos();
		int yPos = verticalChunk.yPos;
		int xo = ((j + (chunkPos.getMinBlockX())) / tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset((k + yPos * 16), tickerWorld.getUPB());
		int zo = ((l + (chunkPos.getMinBlockZ())) / tickerWorld.getUPB());
		return rPos.offset(new BlockPos(xo, yo, zo));
	}
	
	/**
	 * Gets the block pos of the unit space relative to the given chunk
	 * This method assumes that the x, y, and z positions are all able to be clamped to a value from 0-15
	 * In order to use this method, you should get the exact sub chunk
	 *
	 * @param pPos          the small world position
	 * @param verticalChunk the chunk
	 * @return the parent position
	 */
	public static BlockPos getParentPosPrecise(BlockPos pPos, BasicVerticalChunk verticalChunk) {
		ITickerLevel tickerWorld = (ITickerLevel) verticalChunk.getLevel();
		BlockPos rPos = tickerWorld.getRegion().pos.toBlockPos();
		int j = pPos.getX() & 15;
		int k = pPos.getY() & 15;
		int l = pPos.getZ() & 15;
		ChunkPos chunkPos = verticalChunk.getPos();
		int yPos = verticalChunk.yPos;
		int xo = ((j + (chunkPos.getMinBlockX())) / tickerWorld.getUPB());
		int yo = ((k + yPos * 16) / tickerWorld.getUPB()); // TODO: fix this, maybe
		int zo = ((l + (chunkPos.getMinBlockZ())) / tickerWorld.getUPB());
		return rPos.offset(new BlockPos(xo, yo, zo));
	}
}
