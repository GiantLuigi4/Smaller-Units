package tfc.smallerunits.mixin.compat.sodium;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class SodiumDebugOverlayMixin {
	@Inject(at = @At("TAIL"), method = "getSystemInformation")
	public void addText(CallbackInfoReturnable<List<String>> cir) {
		List<String> strings = cir.getReturnValue();
		boolean foundSodium = false;
		int index = 0;
		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			if (foundSodium && string.isEmpty()) {
				index = i;
				break;
			} else if (!foundSodium && string.startsWith("Sodium")) foundSodium = true;
		}
		if (foundSodium)
			strings.add(index, "SU Renderer: " + ChatFormatting.RED + "Vanilla Style (slow)");
	}
}
