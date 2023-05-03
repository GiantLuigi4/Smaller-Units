package tfc.smallerunits.mixin.core;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.util.thread.BlockableEventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.networking.hackery.PacketUtilMess;

@Mixin(PacketUtils.class)
public class PacketUtilsMixin {
	@Inject(at = @At("HEAD"), method = "ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", cancellable = true)
	private static <T extends PacketListener> void preEnsureRunningOnSameThread(Packet<T> packet, T pProcessor, BlockableEventLoop<?> pExecutor, CallbackInfo ci) {
		if (packet instanceof ClientboundCustomPayloadPacket) {
			if (((ClientboundCustomPayloadPacket) packet).getIdentifier().toString().equals("smaller_units:main")) {
				ci.cancel();
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = {"lambda$ensureRunningOnSameThread$0(Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/protocol/Packet;)V", "method_11072"}, require = 0)
	private static <T extends PacketListener> void preHandlePacket(PacketListener listener, Packet packet, CallbackInfo ci) {
		PacketUtilMess.preHandlePacket(listener, packet);
	}
	
	@Inject(at = @At("RETURN"), method = {"lambda$ensureRunningOnSameThread$0(Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/protocol/Packet;)V", "method_11072"}, require = 0)
	private static <T extends PacketListener> void postHandlePacket(PacketListener listener, Packet packet, CallbackInfo ci) {
		PacketUtilMess.postHandlePacket(listener, packet);
	}
}
