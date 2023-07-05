package tfc.smallerunits.mixin.compat.fabric.networking;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
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

@Mixin(value = ClientPlayNetworking.class, remap = false)
public class ClientPlayNetworkingMixin {
	// TODO: test
	private static ClientPlayNetworking.PlayChannelHandler botchReceiver(ClientPlayNetworking.PlayChannelHandler handler) {
		return (a, b, c, d) -> {
			if (NetworkingHacks.unitPos.get() != null) {
				handler.receive(a, b, c, new PacketSender() {
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
							SUNetworkRegistry.send(PacketTarget.SERVER, (WrapperPacket) packet);
						d.sendPacket(packet);
					}
					
					@Override
					public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) {
						sendPacket(packet); // TODO
					}
				});
			} else {
				handler.receive(a, b, c, d);
			}
		};
	}
	
	@ModifyVariable(method = "registerGlobalReceiver", at = @At("HEAD"), index = 1, argsOnly = true)
	private static ClientPlayNetworking.PlayChannelHandler preRegisterGReceiver(ClientPlayNetworking.PlayChannelHandler value) {
		return botchReceiver(value);
	}
	
	@ModifyVariable(method = "registerReceiver", at = @At("HEAD"), index = 1, argsOnly = true)
	private static ClientPlayNetworking.PlayChannelHandler preRegisterReceiver(ClientPlayNetworking.PlayChannelHandler value) {
		return botchReceiver(value);
	}
}