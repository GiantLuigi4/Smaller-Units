package tfc.smallerunits.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.Math1D;

public class PositionUtils {
	// TODO: test this
	public static double getDistance(Level level, BlockPos smallWorldPos, BlockPos actualPos) {
		if (level instanceof ITickerLevel tickerLevel) {
			BlockPos parentPos = getParentPos(smallWorldPos, tickerLevel);
			double scl = 1d / tickerLevel.getUPB();
			BlockPos rPos = tickerLevel.getRegion().pos.toBlockPos();
			double x = (smallWorldPos.getX() & 15) * scl + rPos.getX();
			double y = (smallWorldPos.getX() & 15) * scl + rPos.getY();
			double z = (smallWorldPos.getX() & 15) * scl + rPos.getZ();
			return actualPos.distToCenterSqr(x, y, z);
		}
		return actualPos.distToCenterSqr(smallWorldPos.getX(), smallWorldPos.getY(), smallWorldPos.getZ());
	}
	
	public static BlockPos getParentPos(BlockPos pPos, ITickerLevel tickerWorld) {
		BlockPos rPos = tickerWorld.getRegion().pos.toBlockPos();
		int j = pPos.getX();
		int k = pPos.getY();
		int l = pPos.getZ();
		int xo = Math1D.getChunkOffset(j, tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset(k, tickerWorld.getUPB());
		int zo = Math1D.getChunkOffset(l, tickerWorld.getUPB());
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
//		int xo = ((j + (chunkPos.getMinBlockX())) / tickerWorld.getUPB());
//		int yo = Math1D.getChunkOffset((k + yPos * 16), tickerWorld.getUPB());
//		int zo = ((l + (chunkPos.getMinBlockZ())) / tickerWorld.getUPB());
		int xo = Math1D.getChunkOffset(j + chunkPos.getMinBlockX(), tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset(k + yPos * 16, tickerWorld.getUPB());
		int zo = Math1D.getChunkOffset(l + chunkPos.getMinBlockZ(), tickerWorld.getUPB());
		return rPos.offset(new BlockPos(xo, yo, zo));
	}
	
	public static BlockPos getParentPos(BlockPos pPos, ChunkPos pos, int y, ITickerLevel tickerWorld) {
		BlockPos rPos = tickerWorld.getRegion().pos.toBlockPos();
		int j = pPos.getX() & 15;
		int k = pPos.getY();
		int l = pPos.getZ() & 15;
		ChunkPos chunkPos = pos;
		int yPos = y;
//		int xo = ((j + (chunkPos.getMinBlockX())) / tickerWorld.getUPB());
//		int yo = Math1D.getChunkOffset((k + yPos * 16), tickerWorld.getUPB());
//		int zo = ((l + (chunkPos.getMinBlockZ())) / tickerWorld.getUPB());
		int xo = Math1D.getChunkOffset(j + chunkPos.getMinBlockX(), tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset(k + yPos * 16, tickerWorld.getUPB());
		int zo = Math1D.getChunkOffset(l + chunkPos.getMinBlockZ(), tickerWorld.getUPB());
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
		int xo = Math1D.getChunkOffset(j + chunkPos.getMinBlockX(), tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset(k + yPos * 16, tickerWorld.getUPB());
		int zo = Math1D.getChunkOffset(l + chunkPos.getMinBlockZ(), tickerWorld.getUPB());
		return rPos.offset(new BlockPos(xo, yo, zo));
	}
	
	/**
	 * Gets the scale factor of blocks in a given level
	 *
	 * @param lvl the level to check
	 * @return the scale factor
	 */
	public static double getWorldScale(Level lvl) {
		if (lvl instanceof ITickerLevel tickerLevel) return 1d / tickerLevel.getUPB();
		return 1;
	}
	
	/**
	 * Gets the amount of blocks that can fit into a real world block in one axis for a given level
	 *
	 * @param lvl the level to check
	 * @return the amount of blocks
	 */
	public static int getWorldUpb(Level lvl) {
		if (lvl instanceof ITickerLevel tickerLevel) return tickerLevel.getUPB();
		return 1;
	}
}
