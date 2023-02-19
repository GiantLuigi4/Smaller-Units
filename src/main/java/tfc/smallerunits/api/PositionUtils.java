package tfc.smallerunits.api;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;
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
	
	public static BlockPos getParentPos(BlockPos pPos, BasicVerticalChunk verticalChunk, BlockPos.MutableBlockPos output) {
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
		
		output.set(rPos.getX() + xo, rPos.getY() + yo, rPos.getZ() + zo);
		return output;
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
	
	protected static BlockPos onPos(Level lvl, BlockPos pos, Entity entity) {
		if (lvl.isEmptyBlock(pos)) {
			BlockPos blockpos1 = pos.below();
			BlockState blockstate = lvl.getBlockState(blockpos1);
			if (blockstate.collisionExtendsVertically(lvl, blockpos1, entity)) {
				return blockpos1;
			}
		}
		
		return pos;
	}
	
	public static Pair<Level, BlockPos> getOnPos(Entity entity, int upb) {
		Level lvl = entity.getLevel();
		Vec3 position = entity.getPosition(0);
		int i = Mth.floor(position.x);
		int j = Mth.floor(position.y);
		int k = Mth.floor(position.z);
		
		BlockPos blockpos = new BlockPos(i, j, k);
		RegionPos regionPos = new RegionPos(blockpos);
		Region r = ((RegionalAttachments) lvl).SU$getRegionMap().get(regionPos);
		if (r == null) return null;
		
		// TODO: adjust entity
		Level smallWorld = r.getLevels()[upb];
		if (smallWorld == null) return null;
		
		double x = HitboxScaling.scaleX((ITickerLevel) smallWorld, position.x);
		double y = HitboxScaling.scaleY((ITickerLevel) smallWorld, position.y);
		double z = HitboxScaling.scaleZ((ITickerLevel) smallWorld, position.z);
		BlockPos scaledPos = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
		return Pair.of(smallWorld, onPos(smallWorld, scaledPos, entity));
	}
	
	public static Vec3 getParentVec(BlockPos pPos, ITickerLevel tickerWorld) {
		BlockPos rPos = tickerWorld.getRegion().pos.toBlockPos();
		int j = pPos.getX();
		int k = pPos.getY();
		int l = pPos.getZ();
		int xo = Math1D.getChunkOffset(j, tickerWorld.getUPB());
		int yo = Math1D.getChunkOffset(k, tickerWorld.getUPB());
		int zo = Math1D.getChunkOffset(l, tickerWorld.getUPB());
		
		int x0 = pPos.getX() - (xo * tickerWorld.getUPB());
		int y0 = pPos.getY() - (yo * tickerWorld.getUPB());
		int z0 = pPos.getZ() - (zo * tickerWorld.getUPB());
		
		return new Vec3(
				(xo) + rPos.getX() + (x0 + 0.5) / (double) tickerWorld.getUPB(),
				(yo) + rPos.getY() + (y0 + 0.5) / (double) tickerWorld.getUPB(),
				(zo) + rPos.getZ() + (z0 + 0.5) / (double) tickerWorld.getUPB()
		);
	}
}
