package tfc.smallerunits.mixin.optimization;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.capability.SUCapabilityManager;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
	@Inject(at = @At("RETURN"), method = "playerLoadedChunk")
	public void onLoad(ServerPlayer pPlaer, MutableObject<ClientboundLevelChunkWithLightPacket> pPacketCache, LevelChunk pChunk, CallbackInfo ci) {
		// so... the forge method doesn't actually provide the chunk itself
		// which is... EXTREMELY bad for performance
		// and I don't know why nobody has changed that
		SUCapabilityManager.onChunkWatch(pPlaer, pChunk);
	}
}
