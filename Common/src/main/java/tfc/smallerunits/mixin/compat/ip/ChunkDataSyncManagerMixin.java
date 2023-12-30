package tfc.smallerunits.mixin.compat.ip;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.ChunkDataSyncManager;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.ducks.IEThreadedAnvilChunkStorage;
import qouteall.imm_ptl.core.network.PacketRedirection;
import tfc.smallerunits.data.capability.SUCapabilityManager;

@Mixin(value = ChunkDataSyncManager.class, remap = false)
public class ChunkDataSyncManagerMixin {
	@Inject(at = @At("RETURN"), method = "sendChunkDataPacketNow")
	public void postSendPacket(ServerPlayer player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage, CallbackInfo ci) {
		// this is poor optimization but eh
		ChunkHolder chunkHolder = ieStorage.ip_getChunkHolder(chunkPos.getChunkPos().toLong());
		if (chunkHolder != null) {
			LevelChunk chunk = chunkHolder.getTickingChunk();
			if (chunk != null) {
				PacketRedirection.withForceRedirect(
						ieStorage.ip_getWorld(),
						() -> SUCapabilityManager.ip$onChunkWatch(chunk, player)
				);
			}
		}
	}
}
