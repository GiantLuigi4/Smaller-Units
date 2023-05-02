//#if FABRIC
package tfc.smallerunits.mixin.core.gui.common;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import tfc.smallerunits.networking.PacketTarget;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.networking.hackery.WrapperPacket;

@Mixin(value = ServerPlayNetworking.class, remap = false)
public class ServerPlayNetworkingMixin {
	// TODO: test
	private static ServerPlayNetworking.PlayChannelHandler botchReciever(ServerPlayNetworking.PlayChannelHandler handler) {
		return (a, b, c, d, e) -> {
			if (NetworkingHacks.unitPos.get() != null) {
				handler.receive(a, b, c, d, new PacketSender() {
					@Override
					public Packet<?> createPacket(ResourceLocation channelName, FriendlyByteBuf buf) {
						return ServerPlayNetworking.createS2CPacket(channelName, buf);
					}

					Packet<?> maybeWrap(Packet<?> packet) {
						WrapperPacket pkt = new WrapperPacket(packet);
						if (pkt.additionalInfo != null)
							return pkt;
						return packet;
					}

					@Override
					public void sendPacket(Packet<?> packet) {
						packet = maybeWrap(packet);
						if (packet instanceof WrapperPacket)
							SUNetworkRegistry.send(PacketTarget.player(b), (WrapperPacket) packet);
						e.sendPacket(packet);
					}

					@Override
					public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) {
						sendPacket(packet); // TODO
					}

					@Override
					public void sendPacket(Packet<?> packet, @Nullable PacketSendListener callback) {
						sendPacket(packet); // TODO
						callback.onSuccess();
					}
				});
			} else {
				handler.receive(a, b, c, d, e);
			}
		};
	}

	@ModifyVariable(method = "registerGlobalReceiver", at = @At("HEAD"), index = 1, argsOnly = true)
	private static ServerPlayNetworking.PlayChannelHandler preRegisterGReciever(ServerPlayNetworking.PlayChannelHandler value) {
		return botchReciever(value);
	}

	@ModifyVariable(method = "registerReceiver", at = @At("HEAD"), index = 2, argsOnly = true)
	private static ServerPlayNetworking.PlayChannelHandler preRegisterReciever(ServerPlayNetworking.PlayChannelHandler value) {
		return botchReciever(value);
	}
}
//#endif
