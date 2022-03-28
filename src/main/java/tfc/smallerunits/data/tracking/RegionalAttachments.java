package tfc.smallerunits.data.tracking;

import net.minecraft.world.level.ChunkPos;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;

import java.util.Map;
import java.util.function.BiConsumer;

public interface RegionalAttachments {
	Region SU$getRegion(RegionPos pos);
	
	void SU$findChunk(int y, ChunkPos flag, BiConsumer<RegionPos, Region> regionHandler);
	
	Map<RegionPos, Region> SU$getRegionMap();
}
