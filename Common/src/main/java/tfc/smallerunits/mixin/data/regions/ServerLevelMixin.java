package tfc.smallerunits.mixin.data.regions;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements RegionalAttachments {
	@Shadow
	public ServerChunkCache chunkSource;
	
	@Override
	public Region SU$getRegion(RegionPos pos) {
		return ((RegionalAttachments) chunkSource.chunkMap).SU$getRegion(pos);
	}
	
	@Override
	public void SU$findChunk(int y, ChunkPos flag, BiConsumer<RegionPos, Region> regionHandler) {
		((RegionalAttachments) chunkSource.chunkMap).SU$findChunk(y, flag, regionHandler);
	}
	
	@Override
	public Map<RegionPos, Region> SU$getRegionMap() {
		return ((RegionalAttachments) chunkSource.chunkMap).SU$getRegionMap();
	}
}
