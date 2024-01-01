package tfc.smallerunits.utils.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Math3d {
	public static <T> T traverseBlocks(
			Vec3 start, Vec3 end,
			Level level,
			BiFunction<BlockPos, BlockState, T> fullMapper,
			Function<BlockPos, T> emptyMapper,
			Supplier<T> fallback
	) {
		if (start.equals(end)) {
			return fallback.get();
		} else {
			double x0 = Mth.lerp(-1.0E-7, end.x, start.x);
			double x1 = Mth.lerp(-1.0E-7, start.x, end.x);
			double y0 = Mth.lerp(-1.0E-7, end.y, start.y);
			double y1 = Mth.lerp(-1.0E-7, start.y, end.y);
			double z0 = Mth.lerp(-1.0E-7, end.z, start.z);
			double z1 = Mth.lerp(-1.0E-7, start.z, end.z);
			
			int blockX = Mth.floor(x1);
			int blockY = Mth.floor(y1);
			int blockZ = Mth.floor(z1);
			BlockPos.MutableBlockPos mutableblockpos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
			
			int cx = SectionPos.blockToSectionCoord(blockX);
			int cz = SectionPos.blockToSectionCoord(blockZ);
			LevelChunk chunk = (LevelChunk) level.getChunk(cx, cz, ChunkStatus.FULL, false);
			
			T res = null;
			if (chunk != null && !chunk.isEmpty()) {
				res = fullMapper.apply(
						mutableblockpos,
						chunk.getBlockState(mutableblockpos)
				);
			}
			
			if (res != null) {
				return res;
			} else {
				res = emptyMapper.apply(mutableblockpos);
				if (res != null) return res;
				
				double d6 = x0 - x1;
				double d7 = y0 - y1;
				double d8 = z0 - z1;
				int xStep = Mth.sign(d6);
				int yStep = Mth.sign(d7);
				int zStep = Mth.sign(d8);
				double d9 = xStep == 0 ? Double.MAX_VALUE : (double) xStep / d6;
				double d10 = yStep == 0 ? Double.MAX_VALUE : (double) yStep / d7;
				double d11 = zStep == 0 ? Double.MAX_VALUE : (double) zStep / d8;
				double d12 = d9 * (xStep > 0 ? 1.0 - Mth.frac(x1) : Mth.frac(x1));
				double d13 = d10 * (yStep > 0 ? 1.0 - Mth.frac(y1) : Mth.frac(y1));
				double d14 = d11 * (zStep > 0 ? 1.0 - Mth.frac(z1) : Mth.frac(z1));
				
				while (d12 <= 1.0 || d13 <= 1.0 || d14 <= 1.0) {
					if (d12 < d13) {
						if (d12 < d14) {
							blockX += xStep;
							d12 += d9;
							
							int s = SectionPos.blockToSectionCoord(blockX);
							if (s != cx) chunk = (LevelChunk) level.getChunk(cx = s, cz, ChunkStatus.FULL, false);
						} else {
							blockZ += zStep;
							d14 += d11;
							
							int s = SectionPos.blockToSectionCoord(blockZ);
							if (s != cz) chunk = (LevelChunk) level.getChunk(cx, cz = s, ChunkStatus.FULL, false);
						}
					} else if (d13 < d14) {
						blockY += yStep;
						d13 += d10;
					} else {
						blockZ += zStep;
						d14 += d11;
						
						int s = SectionPos.blockToSectionCoord(blockZ);
						if (s != cz) chunk = (LevelChunk) level.getChunk(cx, cz = s, ChunkStatus.FULL, false);
					}
					
					mutableblockpos.set(blockX, blockY, blockZ);
					
					if (chunk == null || chunk.isEmpty()) {
						res = emptyMapper.apply(mutableblockpos);
						if (res != null) return res;
						continue;
					}
					
					LevelChunkSection section = ((BasicVerticalChunk) chunk).getSectionNullable(SectionPos.blockToSectionCoord(blockY));
					if (section == null || section.hasOnlyAir()) {
						res = emptyMapper.apply(mutableblockpos);
						if (res != null) return res;
						continue;
					}
					
					res = fullMapper.apply(
							mutableblockpos,
							chunk.getBlockState(mutableblockpos)
					);
					if (res != null) return res;
					
					res = emptyMapper.apply(mutableblockpos);
					if (res != null) return res;
				}
				
				return fallback.get();
			}
		}
	}
	
	public static Direction getUp(Direction src) {
		return switch (src) {
			case NORTH, SOUTH, EAST, WEST -> Direction.UP;
			case UP, DOWN -> Direction.NORTH;
		};
	}
	
	public static Direction getRight(Direction src) {
		return switch (src) {
			case NORTH -> Direction.EAST;
			case EAST -> Direction.SOUTH;
			case SOUTH -> Direction.EAST;
			case WEST -> Direction.SOUTH;
			case UP, DOWN -> Direction.EAST;
		};
	}
}
