package tfc.smallerunits.data.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.storage.ServerLevelData;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.simulation.world.server.LevelSourceProviderProvider;
import tfc.smallerunits.simulation.world.server.TickerServerWorld;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class Region {
	public final RegionPos pos;
	int chunksLoaded = 0;
	Level[] levels;
	
	public Region(RegionPos pos) {
		this.pos = pos;
		// TODO: config
		this.levels = new Level[255];
	}
	
	public int subtractRef(RegionPos regionPos) {
		chunksLoaded--;
		return chunksLoaded;
	}
	
	public void addRef(RegionPos regionPos) {
		chunksLoaded++;
	}
	
	public TickerServerWorld getServerWorld(MinecraftServer srv, ServerLevel parent, int upb) {
		if (levels[upb] == null) {
			try {
				levels[upb] = new TickerServerWorld(
						srv,
						// TODO: wrap level data
						(ServerLevelData) parent.getLevelData(),
						// TODO:
						Level.OVERWORLD,
						srv.getLevel(Level.OVERWORLD).dimensionType(),
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
		
		return (TickerServerWorld) levels[upb];
	}
	
	public Level getClientWorld(ClientLevel parent, int upb) {
		if (levels[upb] == null) {
			try {
				levels[upb] = new TickerServerWorld(
						Minecraft.getInstance().getSingleplayerServer(),
						// TODO: wrap level data
						(ServerLevelData) Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD).getLevelData(),
						// TODO:
						Level.OVERWORLD,
						Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD).dimensionType(),
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
						LevelSourceProviderProvider.createGenerator(Minecraft.getInstance().getLaunchedVersion(), parent),
						false, 0, new ArrayList<>(), false,
						parent, upb, this
				);
				levels[upb].isClientSide = true;
				TickerServerWorld lvl = ((TickerServerWorld) levels[upb]);
				lvl.lookup = pos -> {
					BlockPos bp = lvl.region.pos.toBlockPos().offset(
							// TODO: double check this
							Math.floor(pos.getX() / (double) upb),
							Math.floor(pos.getY() / (double) upb),
							Math.floor(pos.getZ() / (double) upb)
					);
					Map<BlockPos, BlockState> cache = lvl.cache;
					if (cache.containsKey(bp)) return cache.get(bp);
					BlockState state;
//					if (!parent.isLoaded(bp)) return Blocks.VOID_AIR.defaultBlockState();
					ChunkPos cp = new ChunkPos(bp);
					if (parent.getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null)
						return Blocks.VOID_AIR.defaultBlockState();
					cache.put(bp, state = parent.getBlockState(bp));
					return state;
				};
			} catch (Throwable e) {
				RuntimeException ex = new RuntimeException(e.getMessage(), e);
				ex.setStackTrace(e.getStackTrace());
				Loggers.UNITSPACE_LOGGER.error("", e);
				throw ex;
			}
		}
		
		return levels[upb];
	}
	
	public void updateWorlds() {
		for (Level level : levels) {
			if (level != null) {
				if (level.isClientSide) {
					if (level instanceof TickerServerWorld) {
						((TickerServerWorld) level).invalidateCache();
					}
				} else {
					((TickerServerWorld) level).invalidateCache();
				}
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
			}
		}
	}
	
	public void forEachLevel(Consumer<Level> func) {
		for (Level level : levels) {
			if (level == null) continue;
			func.accept(level);
		}
	}
}
