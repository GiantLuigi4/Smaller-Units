package tfc.smallerunits.mixin.core;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.WrapperPacket;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
	@Unique
	private final ThreadLocal<Boolean> isSending = ThreadLocal.withInitial(() -> false);
	@Shadow
	private PacketListener packetListener;
	
	@Shadow
	public abstract PacketFlow getDirection();
	
	@Inject(at = @At("HEAD"), method = "sendPacket", cancellable = true)
	public void preSend(Packet<?> p_129515_, PacketSendListener p_243316_, CallbackInfo ci) {
		try {
			if (((PacketListenerAccessor) this.packetListener).getPlayer() == null) return;
		} catch (Throwable ignored) {
			return;
		}
		if (!isSending.get()) {
			isSending.set(true);
			p_129515_ = maybeWrap(p_129515_);
			if (p_129515_ instanceof WrapperPacket) {
				if (this.getDirection().equals(PacketFlow.SERVERBOUND)) {
					SUNetworkRegistry.NETWORK_INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) ((PacketListenerAccessor) this.packetListener).getPlayer()), p_129515_);
				} else {
					SUNetworkRegistry.NETWORK_INSTANCE.sendToServer(p_129515_);
				}
				ci.cancel();
			}
			isSending.remove();
		}
	}
	
	public <E> E maybeWrap(E e) {
		WrapperPacket pkt = new WrapperPacket(e);
		if (pkt.additionalInfo != null)
			return (E) pkt;
		return e;
	}
}
