package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.mojangpls.NoPath;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class UnitChunkMap extends ChunkMap {
	ServerLevel lvl;

	public UnitChunkMap(ServerLevel p_214836_, LevelStorageSource.LevelStorageAccess p_214837_, DataFixer p_214838_, StructureTemplateManager p_214839_, Executor p_214840_, BlockableEventLoop<Runnable> p_214841_, LightChunkGetter p_214842_, ChunkGenerator p_214843_, ChunkProgressListener p_214844_, ChunkStatusUpdateListener p_214845_, Supplier<DimensionDataStorage> p_214846_, int p_214847_, boolean p_214848_) {
		super(p_214836_, p_214837_, p_214838_, p_214839_, p_214840_, p_214841_, p_214842_, p_214843_, p_214844_, p_214845_, p_214846_, p_214847_, p_214848_);
		this.distanceManager = new ButcheredDistMap(p_214840_, p_214841_);
		this.poiManager = new BlankPOIManager(new NoPath(), p_214838_, p_214848_ /* I don't know */, p_214836_.registryAccess(), p_214836_);
		lvl = p_214836_;
	}

	@Override
	public List<ServerPlayer> getPlayers(ChunkPos pPos, boolean pBoundaryOnly) {
		// TODO: filter for chunk
		// TODO: figure out what "filter for chunk" means
		ITickerLevel tickerWorld = (ITickerLevel) lvl;
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
		
		@Override
		public boolean shouldForceTicks(long chunkPos) {
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
	
	@Override
	public void write(ChunkPos pChunkPos, CompoundTag pChunkData) {
	}
	
	@Override
	public CompletableFuture<Optional<CompoundTag>> read(ChunkPos pChunkPos) {
		return null;
	}
	
	@Override
	public void move(ServerPlayer pPlayer) {
		for(ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
			if (chunkmap$trackedentity.entity == pPlayer) {
				chunkmap$trackedentity.updatePlayers(lvl.players());
			} else {
				chunkmap$trackedentity.updatePlayer(pPlayer);
			}
		}
	}
	
	@Override
	public void tick() {
		super.tick();
	}
}
