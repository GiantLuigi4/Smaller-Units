package tfc.smallerunits.simulation.world.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.mojangpls.NoPath;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.world.ITickerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class UnitChunkMap extends ChunkMap {
	ServerLevel lvl;
	
	public UnitChunkMap(ServerLevel p_143040_, LevelStorageSource.LevelStorageAccess p_143041_, DataFixer p_143042_, StructureManager p_143043_, Executor p_143044_, BlockableEventLoop<Runnable> p_143045_, LightChunkGetter p_143046_, ChunkGenerator p_143047_, ChunkProgressListener p_143048_, ChunkStatusUpdateListener p_143049_, Supplier<DimensionDataStorage> p_143050_, int p_143051_, boolean p_143052_) {
		super(p_143040_, p_143041_, p_143042_, p_143043_, p_143044_, p_143045_, p_143046_, p_143047_, p_143048_, p_143049_, p_143050_, p_143051_, p_143052_);
		this.distanceManager = new ButcheredDistMap(p_143044_, p_143045_);
		this.poiManager = new BlankPOIManager(new NoPath(), p_143042_, p_143052_ /* I don't know */, p_143040_);
		lvl = p_143040_;
	}
	
	@Override
	public List<ServerPlayer> getPlayers(ChunkPos pPos, boolean pBoundaryOnly) {
		// TODO: filter for chunk
		// TODO: figure out what "filter for chunk" means
		ITickerWorld tickerWorld = (ITickerWorld) lvl;
		Level parent = tickerWorld.getParent();
		Region region = tickerWorld.getRegion();
		RegionPos pos = region.pos;
		int upb = tickerWorld.getUPB();
		
		float minX = pPos.getMinBlockX() / (float) upb + pos.toBlockPos().getX();
		float minZ = pPos.getMinBlockZ() / (float) upb + pos.toBlockPos().getZ();
		float maxX = pPos.getMaxBlockX() / (float) upb + pos.toBlockPos().getX();
		float maxZ = pPos.getMaxBlockZ() / (float) upb + pos.toBlockPos().getZ();
		
		float avgX = (minX + maxX) / 2;
		float avgZ = (minZ + maxZ) / 2;
		
		// TODO: figure out what exactly this is doing?
		ArrayList<ServerPlayer> out = new ArrayList<>();
		for (Player player : parent.players()) {
			if (player instanceof ServerPlayer) {
				if (
//						player.getX() > minX && player.getX() < maxX &&
//							player.getZ() > minZ && player.getZ() < maxZ
						Math.sqrt(
								Math.pow(player.getX() - avgX, 2) +
										Math.pow(player.getZ() - avgZ, 2)
						) < 128
				) out.add((ServerPlayer) player);
			}
		}
		return out;
//		return (List<ServerPlayer>) ((ITickerWorld)lvl).getParent().players();
//		return super.getPlayers(pPos, pBoundaryOnly);
	}
	
	@Override
	protected Iterable<ChunkHolder> getChunks() {
		return ((TickerChunkCache) lvl.getChunkSource()).getChunks();
	}
	
	@Override
	protected void updateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad) {
		super.updateChunkTracking(pPlayer, pChunkPos, pPacketCache, pWasLoaded, pLoad);
	}

//	public void onUpdateChunkTracking(ServerPlayer pPlayer, ChunkPos pChunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, boolean pWasLoaded, boolean pLoad) {
//		updateChunkTracking(pPlayer, pChunkPos, pPacketCache, pWasLoaded, pLoad);
//	}
	
	public class ButcheredDistMap extends ChunkMap.DistanceManager {
		public ButcheredDistMap(Executor p_140459_, Executor p_140460_) {
			super(p_140459_, p_140460_);
		}
		
		@Override
		public boolean inBlockTickingRange(long p_183917_) {
			// TODO: check if it's in the parent's ticking range
			return true;
		}
		
		@Override
		public boolean inEntityTickingRange(long p_183914_) {
			// TODO: check if it's in the parent's ticking range
			return true;
		}
	}
	
	@Nullable
	@Override
	protected ChunkHolder getVisibleChunkIfPresent(long p_140328_) {
		ChunkPos pos = new ChunkPos(p_140328_);
		ChunkAccess access = lvl.getChunkSource().getChunk(pos.x, pos.z, false);
		if (access instanceof BasicVerticalChunk) return ((BasicVerticalChunk) access).holder;
		else return null;
	}
	
	@Override
	public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos p_183889_) {
		return List.of();
	}
}
