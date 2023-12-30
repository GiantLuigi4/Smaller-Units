package tfc.smallerunits.plat.mixin.core.network;

import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.smallerunits.networking.hackery.NetworkingHacks;

@Mixin(ServerPlayNetworkAddon.class)
public class ServerPlayNetworkAddonMixin {
	@Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isSameThread()Z"))
	public boolean preCheckSameThread(MinecraftServer instance) {
		if (NetworkingHacks.currentContext.get() != null) {
			// actually, do run it on the main thread if it's an SU redirected packet
			return false;
		}
		return instance.isSameThread();
	}
}