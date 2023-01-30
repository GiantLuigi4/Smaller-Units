package tfc.smallerunits.mixin.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
	@Unique
	private static final ThreadLocal<Screen> currentScreen = new ThreadLocal<>();
	
	@Inject(at = @At("HEAD"), method = "drawScreenInternal")
	private static void preDrawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		currentScreen.set(screen);
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				screenAttachments.update(Minecraft.getInstance().player);
				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
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
				NetworkingHacks.unitPos.remove();
				
				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
				}
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "onScreenPotionSize")
	private static void preScreenPotionSize(Screen screen, CallbackInfoReturnable<Event.Result> cir) {
		currentScreen.set(screen);
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
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
				NetworkingHacks.unitPos.remove();
				
				if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen != currentScreen.get()) {
					SUScreenAttachments attachments = (SUScreenAttachments) Minecraft.getInstance().screen;
					attachments.setup(info, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
				}
			}
		}
	}
}
