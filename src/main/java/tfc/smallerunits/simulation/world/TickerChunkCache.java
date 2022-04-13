package tfc.smallerunits.simulation.world;

import com.mojang.datafixers.DataFixer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.chunk.VChunkLookup;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TickerChunkCache extends ServerChunkCache {
	final BasicVerticalChunk[][] columns;
	int upb;
	
	public TickerChunkCache(ServerLevel p_184009_, LevelStorageSource.LevelStorageAccess p_184010_, DataFixer p_184011_, StructureManager p_184012_, Executor p_184013_, ChunkGenerator p_184014_, int p_184015_, int p_184016_, boolean p_184017_, ChunkProgressListener p_184018_, ChunkStatusUpdateListener p_184019_, Supplier<DimensionDataStorage> p_184020_, int upb) {
		super(p_184009_, p_184010_, p_184011_, p_184012_, p_184013_, p_184014_, p_184015_, p_184016_, p_184017_, p_184018_, p_184019_, p_184020_);
		this.chunkMap = new UnitChunkMap(p_184009_, p_184010_, p_184011_, p_184012_, p_184013_, this.mainThreadProcessor, this, p_184014_, p_184018_, p_184019_, p_184020_, p_184015_, p_184017_);
		this.upb = upb;
		columns = new BasicVerticalChunk[33 * 33 * upb * upb][];
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
	
	public ParentLookup getLookup() {
		return ((TickerServerWorld) level).lookup;
	}
	
	public ChunkAccess getChunk(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		if (pChunkX >= (upb * 32) || pChunkZ >= (upb * 32) || pChunkZ < 0 || pChunkX < 0 || pChunkY < 0 || pChunkY > upb) {
			Region r = ((TickerServerWorld) level).region;
			RegionPos pos = r.pos;
			
			int x = pChunkX < 0 ? -1 : ((pChunkX > upb) ? 1 : 0);
			int y = pChunkY < 0 ? -1 : ((pChunkY > upb) ? 1 : 0);
			int z = pChunkZ < 0 ? -1 : ((pChunkZ > upb) ? 1 : 0);
			pos = new RegionPos(
					pos.x + x,
					pos.y + y,
					pos.z + z
			);
			
			pChunkX = ((pChunkX < 0) ? pChunkX + upb : ((pChunkX > upb) ? (pChunkX - (upb * 32)) : pChunkX));
			pChunkY = ((pChunkY < 0) ? pChunkY + upb : ((pChunkY > upb) ? (pChunkX - (upb * 32)) : pChunkY));
			pChunkZ = ((pChunkZ < 0) ? pChunkZ + upb : ((pChunkZ > upb) ? (pChunkX - (upb * 32)) : pChunkZ));
			
			Level parent = ((TickerServerWorld) level).parent;
			Region otherRegion = null;
			Level level = null;
			if (!parent.isClientSide && parent instanceof ServerLevel) {
				otherRegion = ((RegionalAttachments) ((ServerChunkCache) parent.getChunkSource()).chunkMap).SU$getRegion(pos);
				level = otherRegion.getServerWorld(this.level.getServer(), (ServerLevel) parent, upb);
			} else {
				if (parent.isClientSide) {
					otherRegion = ((RegionalAttachments) ((TickerServerWorld) this.level).parent).SU$getRegion(pos);
					level = otherRegion.getClientWorld((ClientLevel) parent, upb);
				}
			}
			return level.getChunk(pChunkX, pChunkZ);
//			return new EmptyLevelChunk(level, new ChunkPos(pChunkX, pChunkZ));
		}
		if (!pLoad) {
			BasicVerticalChunk[] chunks = columns[pChunkX * (33 * upb) + pChunkZ];
			if (chunks == null) return null;
			return chunks[pChunkY];
		} else {
			BasicVerticalChunk[] ck = columns[pChunkX * (33 * upb) + pChunkZ];
			if (ck == null) ck = columns[pChunkX * (33 * upb) + pChunkZ] = new BasicVerticalChunk[33 * upb];
			if (ck[pChunkY] == null) ck[pChunkY] = new BasicVerticalChunk(
					level, new ChunkPos(pChunkX, pChunkZ), pChunkY,
					new VChunkLookup(
							this, pChunkY, ck,
							new ChunkPos(pChunkX, pChunkZ)
					), getLookup(), upb
			);
			return ck[pChunkY];
		}
	}
	
	public BasicVerticalChunk createChunk(int i, ChunkPos ckPos) {
		int pChunkX = ckPos.x;
		int pChunkZ = ckPos.z;
		BasicVerticalChunk[] ck = columns[pChunkX * (33 * upb) + pChunkZ];
		return ck[i] = new BasicVerticalChunk(
				level, new ChunkPos(pChunkX, pChunkZ), i,
				new VChunkLookup(
						this, i, ck,
						new ChunkPos(pChunkX, pChunkZ)
				), getLookup(), upb
		);
	}
}
