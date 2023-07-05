package tfc.smallerunits.mixin.debug;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.utils.platform.PlatformUtils;

@Mixin(Connection.class)
public class MojangCanYouJustPrintToError {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V"), method = "exceptionCaught")
	public void onExceptionCaught0(ChannelHandlerContext p_129533_, Throwable p_129534_, CallbackInfo ci) {
		if (PlatformUtils.isDevEnv()) p_129534_.printStackTrace();
		Loggers.SU_LOGGER.error("Packet handling failed", p_129534_);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"), method = "exceptionCaught")
	public void onExceptionCaught1(ChannelHandlerContext p_129533_, Throwable p_129534_, CallbackInfo ci) {
		if (PlatformUtils.isDevEnv()) p_129534_.printStackTrace();
		Loggers.SU_LOGGER.error("Packet handling failed (double)", p_129534_);
	}
}
