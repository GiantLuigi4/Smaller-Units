package tfc.smallerunits.mixin.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(value = GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final private Minecraft minecraft;
	@Unique
	private static final ThreadLocal<Screen> currentScreen = new ThreadLocal<>();
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"), method = "render")
	private void preDrawScreen(float f, long l, boolean bl, CallbackInfo ci) {
		currentScreen.set(minecraft.screen);
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) minecraft.screen);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				screenAttachments.update(Minecraft.getInstance().player);
				NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(((ITickerLevel) screenAttachments.getTarget()).getRegion().pos, screenAttachments.getUpb()));
				info.adjust(Minecraft.getInstance().player, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos(), false);
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V", shift = At.Shift.AFTER), method = "render")
	private void postDrawScreen(float f, long l, boolean bl, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) minecraft.screen);
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
