package tfc.smallerunits.simulation.world;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.chunk.VChunkLookup;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TickerChunkCache extends ServerChunkCache {
	BasicVerticalChunk[][] columns = new BasicVerticalChunk[32 * 32][];
	int upb;
	
	public TickerChunkCache(ServerLevel p_184009_, LevelStorageSource.LevelStorageAccess p_184010_, DataFixer p_184011_, StructureManager p_184012_, Executor p_184013_, ChunkGenerator p_184014_, int p_184015_, int p_184016_, boolean p_184017_, ChunkProgressListener p_184018_, ChunkStatusUpdateListener p_184019_, Supplier<DimensionDataStorage> p_184020_) {
		super(p_184009_, p_184010_, p_184011_, p_184012_, p_184013_, p_184014_, p_184015_, p_184016_, p_184017_, p_184018_, p_184019_, p_184020_);
	}
	
	@Nullable
	@Override
	public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		return getChunk(pChunkX, 0, pChunkZ, pRequiredStatus, pLoad);
	}
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft) {
		for (BasicVerticalChunk[] column : columns) {
			if (column == null) continue;
			for (BasicVerticalChunk basicVerticalChunk : column) {
				if (basicVerticalChunk == null) continue;
//				level.tickChunk(basicVerticalChunk, 100);
				basicVerticalChunk.randomTick();
			}
		}
	}
	
	public ChunkAccess getChunk(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		if (!pLoad) {
			BasicVerticalChunk[] chunks = columns[pChunkX * 32 + pChunkZ];
			if (chunks == null) return null;
			return chunks[pChunkY];
		} else {
			BasicVerticalChunk[] ck = columns[pChunkX * 32 + pChunkZ];
			if (ck == null) ck = columns[pChunkX * 32 + pChunkZ] = new BasicVerticalChunk[32];
			if (ck[pChunkY] == null) ck[pChunkY] = new BasicVerticalChunk(
					level, new ChunkPos(pChunkX, pChunkZ), pChunkY,
					new VChunkLookup(
							this, pChunkY, ck,
							new ChunkPos(pChunkX, pChunkZ)
					)
			);
			return ck[pChunkY];
		}
	}
	
	public BasicVerticalChunk createChunk(int i, ChunkPos ckPos) {
		int pChunkX = ckPos.x;
		int pChunkZ = ckPos.z;
		BasicVerticalChunk[] ck = columns[pChunkX * 32 + pChunkZ];
		return ck[i] = new BasicVerticalChunk(
				level, new ChunkPos(pChunkX, pChunkZ), i,
				new VChunkLookup(
						this, i, ck,
						new ChunkPos(pChunkX, pChunkZ)
				)
		);
	}
}
