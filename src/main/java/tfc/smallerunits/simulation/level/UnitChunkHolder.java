package tfc.smallerunits.simulation.level;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph;
import qouteall.imm_ptl.core.network.PacketRedirection;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.api.PositionUtils;
import tfc.smallerunits.data.access.ChunkHolderAccessor;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.networking.hackery.WrapperPacket;
import tfc.smallerunits.networking.platform.NetworkDirection;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class UnitChunkHolder extends ChunkHolder {
	LevelChunk chunk;
	int yPos;
	
	public UnitChunkHolder(ChunkPos p_142986_, int p_142987_, LevelHeightAccessor p_142988_, LevelLightEngine p_142989_, LevelChangeListener p_142990_, PlayerProvider p_142991_, LevelChunk chunk, int yPos) {
		super(p_142986_, p_142987_, p_142988_, p_142989_, p_142990_, p_142991_);
		this.chunk = chunk;
		this.yPos = yPos;
	}
	
	@Override
	public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus p_140048_) {
		return getFutureIfPresent(null);
	}
	
	@Override
	public CompletableFuture<Either<ChunkAccess, ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus p_140081_) {
		return new CompletableFuture<>() {
			@Override
			public boolean isDone() {
				return true;
			}
			
			@Override
			public Either<ChunkAccess, ChunkLoadingFailure> get() {
				return Either.left(chunk);
			}
		};
	}
	
	@Override
	public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getTickingChunkFuture() {
		return getFullChunkFuture();
	}
	
	@Override
	public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getEntityTickingChunkFuture() {
		return getFullChunkFuture();
	}
	
	@Override
	public CompletableFuture<Either<LevelChunk, ChunkLoadingFailure>> getFullChunkFuture() {
		return new CompletableFuture<>() {
			@Override
			public boolean isDone() {
				return true;
			}
			
			@Override
			public Either<LevelChunk, ChunkLoadingFailure> get() {
				return Either.left(chunk);
			}
		};
	}
	
	@Nullable
	@Override
	public LevelChunk getTickingChunk() {
		return chunk;
	}
	
	@Nullable
	@Override
	public LevelChunk getFullChunk() {
		return chunk;
	}
	
	public void setBlockDirty(BlockPos pos) {
		blockChanged(pos);
	}
	
	@Override
	public void broadcastChanges(LevelChunk pChunk) {
		if (this.hasChangedSections) {
			Level level = pChunk.getLevel();
			
			for (int l = 0; l < this.changedBlocksPerSection.length; ++l) {
				ShortSet shortset = this.changedBlocksPerSection[l];
				if (shortset != null) {
					int k = this.levelHeightAccessor.getSectionYFromSectionIndex(yPos);
					SectionPos sectionpos = SectionPos.of(pChunk.getPos(), k);
					if (shortset.size() == 1) {
						BlockPos blockpos = sectionpos.relativeToBlockPos(shortset.iterator().nextShort());
						BlockState blockstate = level.getBlockState(blockpos);
						compatBroadcast(new ClientboundBlockUpdatePacket(blockpos, blockstate), false);
						this.broadcastBlockEntityIfNeeded(level, blockpos, blockstate);
					} else {
						LevelChunkSection levelchunksection = pChunk.getSection(yPos);
						ClientboundSectionBlocksUpdatePacket clientboundsectionblocksupdatepacket = new ClientboundSectionBlocksUpdatePacket(sectionpos, shortset, levelchunksection, false);
						compatBroadcast(clientboundsectionblocksupdatepacket, false);
						clientboundsectionblocksupdatepacket.runUpdates((p_140078_, p_140079_) -> {
							this.broadcastBlockEntityIfNeeded(level, p_140078_, p_140079_);
						});
					}
					
					this.changedBlocksPerSection[l] = null;
				}
			}
			
			this.hasChangedSections = false;
		}
	}
	
	protected void compatBroadcast(Packet<?> packet, boolean bl) {
		if (SmallerUnits.isImmersivePortalsPresent()) {
			if (packet == null) return;
			
			ChunkPos pos = getPos();
			
			BlockPos bp = PositionUtils.getParentPos(pos.getWorldPosition(), (ITickerLevel) chunk.level);
			pos = new ChunkPos(bp);
			
			ResourceKey<Level> dimension = chunk.level.dimension();
			Packet<?> wrappedPacket = SUNetworkRegistry.NETWORK_INSTANCE.toVanillaPacket(new WrapperPacket(packet), NetworkDirection.TO_CLIENT);
			Consumer<ServerPlayer> func = player -> PacketRedirection.sendRedirectedMessage(player, dimension, wrappedPacket);
			Stream<ServerPlayer> players;
			if (bl) players = NewChunkTrackingGraph.getFarWatchers(dimension, pos.x, pos.z);
			else players = NewChunkTrackingGraph.getPlayersViewingChunk(dimension, pos.x, pos.z);
			
			NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
			NetworkingHacks.unitPos.set(descriptor.parent());
			players.forEach(func);
			NetworkingHacks.unitPos.set(descriptor);
		} else {
			if (packet == null) {
				Loggers.SU_LOGGER.warn("what");
				return;
			}
			((ChunkHolderAccessor) this).SU$call_broadcast(packet, bl);
		}
	}
}
