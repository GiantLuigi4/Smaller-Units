package tfc.smallerunits.mixin.data.regions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ChunkMap.class)
public class ChunkMapMixin implements RegionalAttachments {
	@Unique
	private final HashMap<RegionPos, Region> regionMap = new HashMap<>();
	
	// TODO: check that this works properly, if it does, swap back to it
//	@Inject(at = @At("HEAD"), method = "scheduleUnload")
//	public void preProcessUnloads(long pChunkPos, ChunkHolder pChunkHolder, CallbackInfo ci) {
//		ChunkPos pos = new ChunkPos(pChunkPos);
//		// this can be optimized
//		int min;
//		int max;
//		if (((ChunkHolderAccessor) pChunkHolder).getLevelHeightAccessor() instanceof ChunkAccess) {
//			min = ((ChunkHolderAccessor) pChunkHolder).getLevelHeightAccessor().getMinBuildHeight();
//			max = ((ChunkHolderAccessor) pChunkHolder).getLevelHeightAccessor().getMaxBuildHeight();
//		} else {
//			ChunkAccess acc = pChunkHolder.getChunkToSave().getNow(null);
//			if (acc == null) acc = pChunkHolder.getTickingChunk();
//			if (acc == null) {
//				min = ((ChunkHolderData) pChunkHolder).SU$getMinY();
//				max = ((ChunkHolderData) pChunkHolder).SU$getMaxY();
//			} else {
//				min = acc.getMinBuildHeight();
//				max = acc.getMaxBuildHeight();
//			}
//		}
//		for (int y = min; y < max; y += 16)
//			findChunk(y, pos, (rp, r) -> {
//				if (r.subtractRef(rp) <= 0)
//					regionMap.remove(rp);
//			});
//	}
	
	@Override
	public Map<RegionPos, Region> SU$getRegionMap() {
		return regionMap;
	}
	
	// TODO: fix this so it doesn't cause deadlocks
//	@Inject(at = @At("RETURN"), method = {"lambda$scheduleChunkLoad$14", "lambda$scheduleChunkLoad$16", "m_198890_", "m_203107_"})
//	public void preProcessLoads(ChunkPos flag, CallbackInfoReturnable<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> cir) {
//		Optional<ChunkAccess> potentialAccess = cir.getReturnValue().left();
//		if (!potentialAccess.isPresent()) return;
//		int min = potentialAccess.get().getMinBuildHeight();
//		int max = potentialAccess.get().getMaxBuildHeight();
//		for (int y = min; y < max; y += 16)
//			findChunk(y, flag, (rp, r) -> r.addRef(rp));
//	}
	
	@Override
	public void SU$findChunk(int y, ChunkPos flag, BiConsumer<RegionPos, Region> regionHandler) {
		findChunk(y, flag, regionHandler);
	}
	
	@Override
	public Region SU$getRegion(RegionPos pos) {
		return regionMap.getOrDefault(pos, null);
	}
	
	@Unique
	private void findChunk(int y, ChunkPos flag, BiConsumer<RegionPos, Region> regionHandler) {
		RegionPos pos = new RegionPos(new BlockPos(flag.getMinBlockX(), y, flag.getMinBlockZ()));
		Region r = regionMap.getOrDefault(pos, null);
		if (r == null) regionMap.put(pos, r = new Region(pos));
		regionHandler.accept(pos, r);
	}
}
