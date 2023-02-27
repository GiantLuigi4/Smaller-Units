package tfc.smallerunits.mixin.data.regions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.logging.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements RegionalAttachments {
	@Unique
	private final HashMap<RegionPos, Region> regionMap = new HashMap<>();
	
	@Inject(at = @At("HEAD"), method = "unload")
	public void preUnloadChunk(LevelChunk pChunk, CallbackInfo ci) {
		ChunkPos pos = pChunk.getPos();
		int min = pChunk.getMinBuildHeight();
		int max = pChunk.getMaxBuildHeight();
		Loggers.SU_LOGGER.info("A chunk has been unloaded: " + pChunk.getPos());
		for (int y = min; y < max; y += 16)
			findChunk(y, pos, (rp, r) -> {
				if (r.subtractRef(rp) <= 0) {
					Region region = regionMap.remove(rp);
					if (region != null) region.close();
				}
			});
	}
	
	// TODO: maybe get a better way of doing this?
	@Inject(at = @At("HEAD"), method = "onChunkLoaded")
	public void onLoadChunk(ChunkPos pChunkPos, CallbackInfo ci) {
		int min = ((Level) (Object) this).getMinBuildHeight();
		int max = ((Level) (Object) this).getMaxBuildHeight();
		Loggers.SU_LOGGER.info("A chunk has been loaded: " + pChunkPos);
		for (int y = min; y < max; y += 16)
			findChunk(y, pChunkPos, (rp, r) -> {
				r.addRef(rp);
			});
	}
	
	@Override
	public Region SU$getRegion(RegionPos pos) {
		return regionMap.getOrDefault(pos, null);
	}
	
	@Override
	public Map<RegionPos, Region> SU$getRegionMap() {
		return regionMap;
	}
	
	@Override
	public void SU$findChunk(int y, ChunkPos flag, BiConsumer<RegionPos, Region> regionHandler) {
		findChunk(y, flag, regionHandler);
	}
	
	@Unique
	private void findChunk(int y, ChunkPos flag, BiConsumer<RegionPos, Region> regionHandler) {
		RegionPos pos = new RegionPos(flag.getMinBlockX() >> 9, y >> 9, flag.getMinBlockZ() >> 9);
		Region r = regionMap.getOrDefault(pos, null);
		if (r == null) regionMap.put(pos, r = new Region(pos));
		regionHandler.accept(pos, r);
	}
}
