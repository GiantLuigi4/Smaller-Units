package tfc.smallerunits.plat.mixin.core.network;

import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.smallerunits.networking.hackery.NetworkingHacks;

@Mixin(ClientPlayNetworkAddon.class)
public class ClientPlayNetworkingAddonMixin {
	@Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isSameThread()Z"))
	public boolean preCheckSameThread(Minecraft instance) {
		if (NetworkingHacks.currentContext.get() != null) {
			// actually, do run it on the main thread if it's an SU redirected packet
			return false;
		}
		return instance.isSameThread();
	}
}
