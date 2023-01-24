package tfc.smallerunits.mixin.core.gui.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	
	@Unique
	private static final ThreadLocal<Screen> currentScreen = new ThreadLocal<>();
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"), method = "keyPress")
	public void preKeyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
		currentScreen.set(minecraft.screen);
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) minecraft.screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "keyPress")
	public void postKeyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) currentScreen.get());
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
				NetworkingHacks.unitPos.remove();
				
				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
				}
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "charTyped")
	public void preTypeChar(long pWindowPointer, int pCodePoint, int pModifiers, CallbackInfo ci) {
		currentScreen.set(minecraft.screen);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"), method = "charTyped")
	public void preTypeChar$(long pWindowPointer, int pCodePoint, int pModifiers, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) minecraft.screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "charTyped")
	public void postTypeChar$(long pWindowPointer, int pCodePoint, int pModifiers, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) currentScreen.get());
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
				NetworkingHacks.unitPos.remove();
				
				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
				}
			}
		}
	}
}
