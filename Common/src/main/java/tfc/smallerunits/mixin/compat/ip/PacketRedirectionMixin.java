package tfc.smallerunits.mixin.compat.ip;

import org.spongepowered.asm.mixin.Mixin;
import qouteall.imm_ptl.core.network.PacketRedirection;

@Mixin(value = PacketRedirection.class, remap = false)
public class PacketRedirectionMixin {
//	@ModifyVariable(
//			method = {"createRedirectedMessage"},
//			at = @At("HEAD"),
//			argsOnly = true,
//			index = 1
//	)
//	private static Packet swapMessgae(Packet value) {
//		Packet src = value;
//		value = maybeWrap(value);
//		if (value instanceof WrapperPacket) {
//			return SUNetworkRegistry.NETWORK_INSTANCE.toVanillaPacket(
//					value,
//					NetworkDirection.PLAY_TO_CLIENT
//			);
//		}
//		return src;
//	}
//
//	private static <E> E maybeWrap(E e) {
//		WrapperPacket pkt = new WrapperPacket(e);
//		if (pkt.additionalInfo != null)
//			return (E) pkt;
//		return e;
//	}
}
