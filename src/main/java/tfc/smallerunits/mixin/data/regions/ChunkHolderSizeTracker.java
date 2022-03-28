package tfc.smallerunits.mixin.data.regions;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.tracking.ChunkHolderData;

@Mixin(ChunkHolder.class)
public class ChunkHolderSizeTracker implements ChunkHolderData {
	@Unique
	int minY, maxY;
	
	@Override
	public int SU$getMaxY() {
		return maxY;
	}
	
	@Override
	public int SU$getMinY() {
		return minY;
	}
	
	@Inject(at = @At("HEAD"), method = "replaceProtoChunk")
	public void preUpdateChunkSave(ImposterProtoChunk completablefuture, CallbackInfo ci) {
		minY = completablefuture.getMinBuildHeight();
		maxY = completablefuture.getMaxBuildHeight();
	}
	
	@Inject(at = @At("HEAD"), method = "broadcastChanges")
	public void preUpdateChunkSave(LevelChunk blockpos, CallbackInfo ci) {
		minY = blockpos.getMinBuildHeight();
		maxY = blockpos.getMaxBuildHeight();
	}
}
