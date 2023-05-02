package tfc.smallerunits.mixin.core;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.networking.platform.PacketTarget;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.WrapperPacket;
import tfc.smallerunits.utils.asm.IPCompat;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
	@Unique
	private final ThreadLocal<Boolean> isSending = ThreadLocal.withInitial(() -> false);
	@Shadow
	private PacketListener packetListener;
	
	@Shadow @Final private PacketFlow receiving;
	
	@Inject(at = @At("HEAD"), method = "sendPacket", cancellable = true)
	public void preSend(Packet<?> p_129515_, PacketSendListener p_243316_, CallbackInfo ci) {
		try {
			if (((PacketListenerAccessor) this.packetListener).getPlayer() == null) return;
		} catch (Throwable ignored) {
			return;
		}
		if (!isSending.get()) {
			isSending.set(true);
			if (SmallerUnits.isImmersivePortalsPresent() && receiving.equals(PacketFlow.SERVERBOUND)) {
				if (IPCompat.runPacketModifications(p_129515_, isSending, ci)) {
					isSending.remove();
					return;
				}
			}
			p_129515_ = maybeWrap(p_129515_);
			if (p_129515_ instanceof WrapperPacket) {
				if (receiving.equals(PacketFlow.SERVERBOUND)) {
					SUNetworkRegistry.send(PacketTarget.player((ServerPlayer) ((PacketListenerAccessor) this.packetListener).getPlayer()), (WrapperPacket)p_129515_);
				} else {
					SUNetworkRegistry.send(PacketTarget.SERVER, (WrapperPacket)p_129515_);
				}
				ci.cancel();
			}
			isSending.remove();
		}
	}

//	@ModifyArg(
//			method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
//			index = 0,
//			at = @At(value = "INVOKE", target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z")
//	)
//	public <E> E modifyPacket0(E e) {
//		return maybeWrap(e);
//	}
//
//	@ModifyArg(
//			method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
//			index = 0,
//			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V")
//	)
//	public Packet<?> modifyPacket1(Packet<?> pInPacket) {
//		return maybeWrap(pInPacket);
//	}
	
	public <E> E maybeWrap(E e) {
		WrapperPacket pkt = new WrapperPacket(e);
		if (pkt.additionalInfo != null)
			return (E) pkt;
		return e;
	}
}
