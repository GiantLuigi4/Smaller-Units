package tfc.smallerunits.plat.mixin.core.network;

import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkAddon.class)
public class ServerPlayNetworkAddonMixin {
//	@Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isSameThread()Z"))
//	public boolean preCheckSameThread(MinecraftServer instance) {
//		if (NetworkingHacks.currentContext.get() != null) {
//			// actually, do run it on the main thread if it's an SU redirected packet
//			return false;
//		}
//		return instance.isSameThread();
//	}
}