package tfc.smallerunits.simulation.level.server;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.plat.CapabilityWrapper;
import tfc.smallerunits.simulation.level.ITickerLevel;

import java.util.List;

public abstract class AbstractTickerServerLevel extends ServerLevel implements ITickerLevel {
	public AbstractTickerServerLevel(MinecraftServer server, ServerLevelData data, ResourceKey<Level> p_8575_, DimensionType dimType, ChunkProgressListener progressListener, ChunkGenerator generator, boolean p_8579_, long p_8580_, List<CustomSpawner> spawners, boolean p_8582_, Level parent, int upb, Region region) {
		super(
				server,
				Util.backgroundExecutor(),
				null,
				data,
				p_8575_,
				Holder.direct(dimType),
				progressListener,
				generator,
				p_8579_,
				p_8580_,
				spawners,
				p_8582_
		);
	}
	
	public abstract CapabilityWrapper getCaps();
}
