package tfc.smallerunits.mixin.core;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tfc.smallerunits.networking.hackery.WrapperPacket;

@Mixin(Connection.class)
public class ConnectionMixin {
	@ModifyArg(
			method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
			index = 0,
			at = @At(value = "INVOKE", target = "Ljava/util/Queue;add(Ljava/lang/Object;)Z")
	)
	public <E> E modifyPacket0(E e) {
		WrapperPacket pkt = new WrapperPacket(e);
		if (pkt.additionalInfo != null) {
//			super.write(ctx, pkt, promise);
//			return;
			return (E) pkt;
		}
		return e;
	}
	
	@ModifyArg(
			method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
			index = 0,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V")
	)
	public Packet<?> modifyPacket1(Packet<?> pInPacket) {
		WrapperPacket pkt = new WrapperPacket(pInPacket);
		if (pkt.additionalInfo != null) {
//			super.write(ctx, pkt, promise);
//			return;
			return pkt;
		}
		return pInPacket;
	}
}
