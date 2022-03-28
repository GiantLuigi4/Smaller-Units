package tfc.smallerunits.data.storage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.ServerLevelData;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.simulation.world.TickerServerWorld;

import java.util.ArrayList;
import java.util.List;

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
						new FlatLevelSource(
								new FlatLevelGeneratorSettings(
										new StructureSettings(false),
										RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY)
								).withLayers(
										List.of(new FlatLayerInfo(0, Blocks.AIR)),
										new StructureSettings(false)
								)
						),
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
						new FlatLevelSource(
								new FlatLevelGeneratorSettings(
										new StructureSettings(false),
										RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY)
								).withLayers(
										List.of(new FlatLayerInfo(0, Blocks.AIR)),
										new StructureSettings(false)
								)
						),
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
		
		return levels[upb];
	}
}