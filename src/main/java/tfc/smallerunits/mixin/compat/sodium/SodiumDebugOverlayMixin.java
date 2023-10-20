package tfc.smallerunits.mixin.compat.sodium;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.utils.config.compat.ClientCompatConfig;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class SodiumDebugOverlayMixin {
	@Unique
	private static String getSodiumName() {
		if (ModList.get().isLoaded("sodium")) return "Sodium";
		else if (ModList.get().isLoaded("rubidium")) return "Rubidium";
		else if (ModList.get().isLoaded("magnesium")) return "Magnesium";
		else if (ModList.get().isLoaded("embeddium")) return "Embeddium";
		return null;
	}
	
	@Unique
	private static boolean isSodiumPresent() {
		if (ModList.get().isLoaded("sodium")) return true;
		else if (ModList.get().isLoaded("rubidium")) return true;
		else if (ModList.get().isLoaded("magnesium")) return true;
		else if (ModList.get().isLoaded("embeddium")) return true;
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
						ClientCompatConfig.RenderCompatOptions.sodiumRenderMode.formatting +
						ClientCompatConfig.RenderCompatOptions.sodiumRenderMode.f3Text
				);
			}
		}
	}
}
