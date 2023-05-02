package tfc.smallerunits.data.storage;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.storage.ServerLevelData;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.client.FakeClientLevel;
import tfc.smallerunits.simulation.level.server.LevelSourceProviderProvider;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.platform.PlatformUtils;
import tfc.smallerunits.utils.threading.ThreadLocals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Region {
	public final RegionPos pos;
	int chunksLoaded = 0;
	Level[] levels;
	
	public Region(RegionPos pos) {
		this.pos = pos;
		// TODO: config
		this.levels = new Level[256];
	}
	
	public int subtractRef(RegionPos regionPos) {
		chunksLoaded--;
		return chunksLoaded;
	}
	
	public void addRef(RegionPos regionPos) {
		chunksLoaded++;
	}
	
	public TickerServerLevel getServerWorld(MinecraftServer srv, ServerLevel parent, int upb) {
		if (levels[upb] == null) {
			try {
				ThreadLocals.levelLocal.set(parent);
				levels[upb] = new TickerServerLevel(
						srv,
						// TODO: wrap level data
						(ServerLevelData) parent.getLevelData(),
						// TODO:
						parent.dimension(), parent.dimensionType(),
						new ChunkProgressListener() {
							@Override
							public void updateSpawnPos(ChunkPos pCenter) {
							}
							
							@Override
							public void onStatusChange(ChunkPos pChunkPosition, ChunkStatus pNewStatus) {
							}
							
							@Override
							public void start() {
							}
							
							@Override
							public void stop() {
							}
						},
						LevelSourceProviderProvider.createGenerator(srv.getServerVersion(), parent),
						false, 0, new ArrayList<>(), false,
						parent, upb, this
				);
			} catch (Throwable e) {
				RuntimeException ex = new RuntimeException(e.getMessage(), e);
				ex.setStackTrace(e.getStackTrace());
				Loggers.UNITSPACE_LOGGER.error("", e);
				throw ex;
			}
		}
		
		return (TickerServerLevel) levels[upb];
	}
	
	public Level getClientWorld(Level parent, int upb) {
//		if (!(parent instanceof ClientLevel)) return null;
		if (levels[upb] == null) {
			try {
				ThreadLocals.levelLocal.set(parent);
				levels[upb] = new FakeClientLevel(
						(ClientLevel) parent,
						null, ((ClientLevel) parent).getLevelData(),
						parent.dimension(), Holder.direct(parent.dimensionType()),
						0, 0, parent.getProfilerSupplier(),
						null, true, 0,
						upb, this
				);
				levels[upb].isClientSide = true;
//				TickerServerWorld lvl = ((TickerServerWorld) levels[upb]);
//				lvl.lookup = pos -> {
//					BlockPos bp = lvl.region.pos.toBlockPos().offset(
//							// TODO: double check this
//							Math.floor(pos.getX() / (double) upb),
//							Math.floor(pos.getY() / (double) upb),
//							Math.floor(pos.getZ() / (double) upb)
//					);
//					Map<BlockPos, BlockState> cache = lvl.cache;
//					if (cache.containsKey(bp)) return cache.get(bp);
//					BlockState state;
////					if (!parent.isLoaded(bp)) return Blocks.VOID_AIR.defaultBlockState();
//					ChunkPos cp = new ChunkPos(bp);
//					if (parent.getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null)
//						return Blocks.VOID_AIR.defaultBlockState();
//					cache.put(bp, state = parent.getBlockState(bp));
//					return state;
//				};
			} catch (Throwable e) {
				RuntimeException ex = new RuntimeException(e.getMessage(), e);
				ex.setStackTrace(e.getStackTrace());
				Loggers.UNITSPACE_LOGGER.error("", e);
				throw ex;
			}
		}
		
		return levels[upb];
	}
	
	public void updateWorlds(BlockPos pos) {
		for (Level level : levels) {
			if (level != null) {
				((ITickerLevel) level).invalidateCache(pos);
			}
		}
	}
	
	public void tickWorlds() {
		for (Level level : levels) {
			if (level == null) continue;
			if (!level.isClientSide) {
				if (level instanceof ServerLevel) {
					((ServerLevel) level).tick(() -> true);
				}
			} else {
				if (IHateTheDistCleaner.isClientLevel(level)) {
					IHateTheDistCleaner.tickLevel(level);
				}
			}
		}
	}
	
	public void forEachLevel(Consumer<Level> func) {
		for (Level level : levels) {
			if (level == null) continue;
			func.accept(level);
		}
	}
	
	public Level getLevel(PacketListener listener, Player player, int upb) {
		if (listener instanceof ServerPacketListener) {
			return getServerWorld(player.level.getServer(), (ServerLevel) player.level, upb);
		} else {
			return getClientWorld(player.level, upb);
		}
	}
	
	public Level[] getLevels() {
		return levels;
	}
	
	public void close() {
		for (Level level : levels) {
			try {
				if (level != null) {
					if (!level.isClientSide) PlatformUtils.postUnload(level);
					else IHateTheDistCleaner.postUnload(level);
					level.close();
					if (level instanceof TickerServerLevel) {
						((TickerServerLevel) level).saveWorld.saveLevel();
						((TickerServerLevel) level).saveWorld.saveAllChunks();
					}
					
					//#if FABRIC
					if (level instanceof ServerLevel serverLevel)
					net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents.UNLOAD.invoker().onWorldUnload(serverLevel.getServer(), serverLevel);
					//#else
					//net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
					//		new net.minecraftforge.event.level.LevelEvent.Unload(level));
					//#endif
					level.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				// TODO: probably should handle this
			}
		}
	}
}
