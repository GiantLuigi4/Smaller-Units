package tfc.smallerunits.plat.mixin.compat.cc;

import dan200.computercraft.shared.network.NetworkHandler;
import dan200.computercraft.shared.network.NetworkMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.server.AbstractTickerServerLevel;

import java.util.function.Consumer;

@Mixin(value = NetworkHandler.class, remap = false)
public abstract class NetworkHandlerMixin {
	@Shadow
	private static FriendlyByteBuf encode(NetworkMessage par1) {
		return null;
	}
	
	@Shadow
	@Final
	private static ResourceLocation ID;
	
	@Inject(at = @At("HEAD"), method = "sendToAllTracking", cancellable = true)
	private static void preSend(NetworkMessage packet, LevelChunk chunk, CallbackInfo ci) {
		if (chunk instanceof BasicVerticalChunk) {
			NetworkingHacks.LevelDescriptor desc = NetworkingHacks.unitPos.get();
			NetworkingHacks.setPos(((ITickerLevel) chunk.getLevel()).getDescriptor());
			
			Consumer<ServerPlayer> consumer = var1x -> var1x.connection.send(new ClientboundCustomPayloadPacket(ID, encode(packet)));
			((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(consumer);
			
			NetworkingHacks.setPos(desc);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "sendToAllAround", cancellable = true)
	private static void preSend(NetworkMessage packet, Level world, Vec3 pos, double range, CallbackInfo ci) {
		if (world instanceof ITickerLevel) {
			NetworkingHacks.LevelDescriptor desc = NetworkingHacks.unitPos.get();
			NetworkingHacks.setPos(((ITickerLevel) world).getDescriptor());
			
			((AbstractTickerServerLevel) world)
					.broadcastTo(null, pos.x, pos.y, pos.z, range, world.dimension(), new ClientboundCustomPayloadPacket(ID, encode(packet)));
			
			NetworkingHacks.setPos(desc);
			ci.cancel();
		}
	}
}
