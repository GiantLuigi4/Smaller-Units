package tfc.smallerunits.mixin.core.gui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.utils.PositionalInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public Screen screen;
	
	@Shadow
	@Nullable
	public LocalPlayer player;
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.NONE), method = "tick")
	public void preTickScreen(CallbackInfo ci) {
		if (Minecraft.getInstance().player != null && screen != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) this.screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "tick")
	public void postTickScreen(CallbackInfo ci) {
		if (Minecraft.getInstance().player != null && screen != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) this.screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
}
