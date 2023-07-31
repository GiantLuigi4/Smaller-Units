package tfc.smallerunits.mixin.compat.sodium;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.client.render.compat.SodiumRenderMode;
import tfc.smallerunits.utils.platform.PlatformUtils;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class SodiumDebugOverlayMixin {
	@Unique
	private static String getSodiumName() {
		if (PlatformUtils.isLoaded("sodium")) return "Sodium";
		else if (PlatformUtils.isLoaded("rubidium")) return "Rubidium";
		else if (PlatformUtils.isLoaded("magnesium")) return "Magnesium";
		return null;
	}
	
	@Unique
	private static boolean isSodiumPresent() {
		if (PlatformUtils.isLoaded("sodium")) return true;
		else if (PlatformUtils.isLoaded("rubidium")) return true;
		else if (PlatformUtils.isLoaded("magnesium")) return true;
		return false;
	}
	
	@Inject(at = @At("TAIL"), method = "getSystemInformation")
	public void addText(CallbackInfoReturnable<List<String>> cir) {
		if (isSodiumPresent()) {
			String name = getSodiumName();
			if (name == null) throw new RuntimeException("???");
			
			List<String> strings = cir.getReturnValue();
			boolean foundSodium = false;
			int index = -1;
			
			for (int i = 0; i < strings.size(); i++) {
				String string = strings.get(i);
				if (foundSodium && string.isEmpty()) {
					index = i;
					break;
				} else if (!foundSodium && string.startsWith(name)) foundSodium = true;
			}
			
			if (index == -1) index = strings.size();
			
			if (foundSodium) {
				strings.add(index, "SU Renderer: " +
//						ClientCompatConfig.RenderCompatOptions.sodiumRenderMode.formatting +
//						ClientCompatConfig.RenderCompatOptions.sodiumRenderMode.f3Text
								SodiumRenderMode.VANILLA.formatting +
								SodiumRenderMode.VANILLA.f3Text
				);
			}
		}
	}
}
