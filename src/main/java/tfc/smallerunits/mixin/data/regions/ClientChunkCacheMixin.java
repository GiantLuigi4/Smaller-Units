package tfc.smallerunits.mixin.data.regions;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
	@Shadow
	@Final
	private ClientLevel level;
	
	// tracky breaks this
//	@Inject(at = @At("RETURN"), method = "replaceWithPacketData", cancellable = true)
//	public void postReplaceWithPacketData(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer, CallbackInfoReturnable<LevelChunk> cir) {
//		LevelChunk chnk = cir.getReturnValue();
//		if (chnk == null) return;
//		int min = chnk.getMinBuildHeight();
//		int max = chnk.getMaxBuildHeight();
//		for (int y = min; y < max; y += 16)
//			((RegionalAttachments) level).SU$findChunk(y, chnk.getPos(), (rp, r) -> {
//				r.addRef(rp);
//			});
//	}
}
