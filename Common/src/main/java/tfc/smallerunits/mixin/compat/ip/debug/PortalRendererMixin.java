package tfc.smallerunits.mixin.compat.ip.debug;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import qouteall.imm_ptl.core.render.PortalRenderer;

@Mixin(value = PortalRenderer.class, remap = false)
public class PortalRendererMixin {
	@Shadow @Final public static Minecraft client;
	
	@Redirect(method = "getPortalsToRender", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/Validate;isTrue(Z)V"))
	protected void betterReporting(boolean expression) {
		if (!expression) {
			assert client.cameraEntity != null;
			throw new IllegalArgumentException(client.level + " is not the same level as " + client.cameraEntity.level);
		}
	}
}
