package tfc.smallerunits.plat.mixin.core.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
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
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.WrapperPacket;
import tfc.smallerunits.plat.net.PacketTarget;
import tfc.smallerunits.utils.asm.IPCompat;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
	@Unique
	private final ThreadLocal<Boolean> isSending = ThreadLocal.withInitial(() -> false);
	@Shadow
	private PacketListener packetListener;
	
	@Shadow
	public abstract PacketFlow getDirection();
	
	@Shadow @Final private PacketFlow receiving;
	
	@Inject(at = @At("HEAD"), method = "sendPacket", cancellable = true)
	public void preSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> p_129522_, CallbackInfo ci) {
		try {
			if (((PacketListenerAccessor) this.packetListener).getPlayer() == null) return;
		} catch (Throwable ignored) {
			return;
		}

//		if (SmallerUnits.isImmersivePortalsPresent() && this.getDirection().equals(PacketFlow.SERVERBOUND)) {
//			// for some reason, IP does not redirect packets being sent to the server
//			return;
//		}
		
		if (!isSending.get()) {
			isSending.set(true);
			if (SmallerUnits.isImmersivePortalsPresent() && this.getDirection().equals(PacketFlow.SERVERBOUND)) {
				if (IPCompat.runPacketModifications(packet, isSending, ci))
					return;
			}
			packet = maybeWrap(packet, this.getDirection().getOpposite());
			if (packet instanceof WrapperPacket) {
				if (this.getDirection().equals(PacketFlow.SERVERBOUND)) {
					SUNetworkRegistry.NETWORK_INSTANCE.send(
							PacketTarget.player((ServerPlayer) ((PacketListenerAccessor) this.packetListener).getPlayer()),
							(WrapperPacket) packet
					);
				} else {
					SUNetworkRegistry.NETWORK_INSTANCE.send(
							PacketTarget.SERVER,
							(WrapperPacket) packet
					);
				}
				ci.cancel();
			}
			isSending.remove();
		}
	}
	
	public <E> E maybeWrap(E e, PacketFlow flow) {
		WrapperPacket pkt = new WrapperPacket(e, flow);
		if (pkt.additionalInfo != null)
			return (E) pkt;
		return e;
	}
}
