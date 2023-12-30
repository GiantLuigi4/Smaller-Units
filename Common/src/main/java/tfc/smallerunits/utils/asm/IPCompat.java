package tfc.smallerunits.utils.asm;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.ducks.IECustomPayloadPacket;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.WrapperPacket;
import tfc.smallerunits.plat.net.NetworkDirection;

public class IPCompat {
	
	public static boolean runPacketModifications(Packet<?> p_129515_, ThreadLocal<Boolean> isSending, CallbackInfo ci) {
		if (p_129515_ instanceof IECustomPayloadPacket packet) {
			Packet<?> pkt = maybeWrap(packet.ip_getRedirectedPacket(), PacketFlow.CLIENTBOUND);
			if (pkt instanceof WrapperPacket) {
				packet.ip_setRedirectedPacket(
						(Packet<ClientGamePacketListener>) SUNetworkRegistry.NETWORK_INSTANCE.toVanillaPacket(
								(WrapperPacket) pkt,
								NetworkDirection.TO_CLIENT
						)
				);
			}
			isSending.remove();
			return true;
		}
		return false;
	}
	
	private static <E> E maybeWrap(E e, PacketFlow flow) {
		WrapperPacket pkt = new WrapperPacket(e, flow);
		if (pkt.additionalInfo != null)
			return (E) pkt;
		return e;
	}
}
