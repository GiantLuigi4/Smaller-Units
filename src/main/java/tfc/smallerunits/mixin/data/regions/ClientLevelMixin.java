package tfc.smallerunits.mixin.data.regions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;

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
		for (int y = min; y < max; y += 16)
			findChunk(y, pos, (rp, r) -> {
				if (r.subtractRef(rp) <= 0)
					regionMap.remove(rp);
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
