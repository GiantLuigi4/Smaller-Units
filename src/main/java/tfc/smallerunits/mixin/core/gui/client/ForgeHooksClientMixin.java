package tfc.smallerunits.mixin.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
	@Inject(at = @At("HEAD"), method = "drawScreenInternal")
	private static void preDrawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "drawScreenInternal")
	private static void postDrawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenPotionSize")
	private static void preScreenPotionSize(Screen screen, CallbackInfoReturnable<Event.Result> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenPotionSize")
	private static void postScreenPotionSize(Screen screen, CallbackInfoReturnable<Event.Result> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	// TODO: I have no idea how I'm gonna port the below to fabric
	@Inject(at = @At("HEAD"), method = "onScreenMouseClickedPre")
	private static void prePrePressMouse(Screen guiScreen, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseClickedPre")
	private static void postPrePressMouse(Screen guiScreen, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseClickedPost")
	private static void prePostPressMouse(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseClickedPost")
	private static void postPostPressMouse(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseClickedPre")
	private static void prePreClickMouse(Screen guiScreen, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseClickedPre")
	private static void postPreClickMouse(Screen guiScreen, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseClickedPost")
	private static void prePostClickMouse(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseClickedPost")
	private static void postPostClickMouse(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseReleasedPre")
	private static void prePreReleaseMouse(Screen guiScreen, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseReleasedPre")
	private static void postPreReleaseMouse(Screen guiScreen, double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseReleasedPost")
	private static void prePostReleaseMouse(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseReleasedPost")
	private static void postPostReleaseMouse(Screen guiScreen, double mouseX, double mouseY, int button, boolean handled, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseScrollPre")
	private static void prePreScrollMouse(MouseHandler mouseHelper, Screen guiScreen, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("TAIL"), method = "onScreenMouseScrollPre")
	private static void postPreScrollMouse(MouseHandler mouseHelper, Screen guiScreen, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseScrollPost")
	private static void prePostScrollMouse(MouseHandler mouseHelper, Screen guiScreen, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseScrollPost")
	private static void postPostScrollMouse(MouseHandler mouseHelper, Screen guiScreen, double scrollDelta, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseDragPre")
	private static void prePreDragMouse(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseDragPre")
	private static void postPreDragMouse(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenMouseDragPost")
	private static void prePostDragMouse(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenMouseDragPost")
	private static void postPostDragMouse(Screen guiScreen, double mouseX, double mouseY, int mouseButton, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenKeyPressedPre")
	private static void prePrePressKey(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenKeyPressedPre")
	private static void postPrePressKey(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenKeyPressedPost")
	private static void prePostPressKey(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenKeyPressedPost")
	private static void postPostPressKey(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenCharTypedPre")
	private static void prePreTypeChar(Screen guiScreen, char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenCharTypedPre")
	private static void postPreTypeChar(Screen guiScreen, char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenCharTypedPost")
	private static void prePostTypeChar(Screen guiScreen, char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenCharTypedPost")
	private static void postTypeChar(Screen guiScreen, char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenKeyReleasedPre")
	private static void prePreTypeChar(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenKeyReleasedPre")
	private static void postPreTypeChar(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenKeyReleasedPost")
	private static void prePostTypeChar(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At("RETURN"), method = "onScreenKeyReleasedPost")
	private static void postPostTypeChar(Screen guiScreen, int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) guiScreen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset(Minecraft.getInstance().player);
			}
		}
	}
}
