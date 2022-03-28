package tfc.smallerunits.mixin.data.regions;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.tracking.RegionalAttachments;

import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
	@Shadow
	@Final
	private ClientLevel level;
	
	@Inject(at = @At("RETURN"), method = "replaceWithPacketData", cancellable = true)
	public void postReplaceWithPacketData(int pX, int pZ, FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> pConsumer, CallbackInfoReturnable<LevelChunk> cir) {
		LevelChunk chnk = cir.getReturnValue();
		if (chnk == null) return;
		int min = chnk.getMinBuildHeight();
		int max = chnk.getMaxBuildHeight();
		for (int y = min; y < max; y += 16)
			((RegionalAttachments) level).SU$findChunk(y, chnk.getPos(), (rp, r) -> {
				r.addRef(rp);
			});
	}
}
