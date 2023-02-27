package tfc.smallerunits.mixin.core.gui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
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

import java.nio.file.Path;
import java.util.List;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
//	@Shadow
//	@Final
//	private Minecraft minecraft;
//
//	@Unique
//	private static final ThreadLocal<Screen> currentScreen = new ThreadLocal<>();
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"), method = "onMove")
//	public void preMouseMove(long pWindowPointer, double pXpos, double pYpos, CallbackInfo ci) {
//		currentScreen.set(minecraft.screen);
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
//				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "onMove")
//	public void postMouseMove(long pWindowPointer, double pXpos, double pYpos, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) currentScreen.get());
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				info.reset(Minecraft.getInstance().player);
//				NetworkingHacks.unitPos.remove();
//
//				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
//					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
//					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
//				}
//			}
//		}
//	}
//
//	@Inject(at = @At("HEAD"), method = "onScroll")
//	public void preStartScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci) {
//		currentScreen.set(minecraft.screen);
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;afterMouseAction()V"), method = "onScroll")
//	public void preScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
//				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onScreenMouseScrollPre(Lnet/minecraft/client/MouseHandler;Lnet/minecraft/client/gui/screens/Screen;D)Z", shift = At.Shift.AFTER), method = "onScroll")
//	public void postPreScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				info.reset(Minecraft.getInstance().player);
//				NetworkingHacks.unitPos.remove();
//
//				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
//					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
//					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
//				}
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onScreenMouseScrollPost(Lnet/minecraft/client/MouseHandler;Lnet/minecraft/client/gui/screens/Screen;D)Z"), method = "onScroll")
//	public void prePostScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
//				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/ForgeHooksClient;onScreenMouseScrollPost(Lnet/minecraft/client/MouseHandler;Lnet/minecraft/client/gui/screens/Screen;D)Z", shift = At.Shift.AFTER), method = "onScroll")
//	public void postPostScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				info.reset(Minecraft.getInstance().player);
//				NetworkingHacks.unitPos.remove();
//
//				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
//					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
//					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
//				}
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V"), method = "onPress")
//	public void prePress(long pWindowPointer, int pButton, int pAction, int pModifiers, CallbackInfo ci) {
//		currentScreen.set(minecraft.screen);
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
//				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.AFTER), method = "onPress")
//	public void postPress(long pWindowPointer, int pButton, int pAction, int pModifiers, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				info.reset(Minecraft.getInstance().player);
//				NetworkingHacks.unitPos.remove();
//
//				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
//					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
//					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
//				}
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;afterMouseAction()V"), method = "onPress")
//	public void preAfterPress(long pWindowPointer, int pButton, int pAction, int pModifiers, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
//				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;afterMouseAction()V", shift = At.Shift.AFTER), method = "onPress")
//	public void postAfterPress(long pWindowPointer, int pButton, int pAction, int pModifiers, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				info.reset(Minecraft.getInstance().player);
//				NetworkingHacks.unitPos.remove();
//
//				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
//					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
//					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
//				}
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "HEAD"), method = "onDrop")
//	public void preDrop(long pWindow, List<Path> pPaths, CallbackInfo ci) {
//		currentScreen.set(minecraft.screen);
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
//				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
//			}
//		}
//	}
//
//	@Inject(at = @At(value = "RETURN"), method = "onDrop")
//	public void postDrop(long pWindow, List<Path> pPaths, CallbackInfo ci) {
//		Screen screen = currentScreen.get();
//		if (Minecraft.getInstance().player != null && screen != null) {
//			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
//			PositionalInfo info = screenAttachments.getPositionalInfo();
//			if (info != null) {
//				info.reset(Minecraft.getInstance().player);
//				NetworkingHacks.unitPos.remove();
//
//				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
//					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
//					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
//				}
//			}
//		}
//	}
}
