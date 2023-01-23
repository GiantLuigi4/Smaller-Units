package tfc.smallerunits.mixin.core.gui.server;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z"), method = "tick")
	public void preCheckContainer(CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) ((Player) (Object) this).containerMenu);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.scalePlayerReach(((Player) (Object) this), screenAttachments.getUpb());
				info.adjust((Player) (Object) this, screenAttachments.getTarget(), screenAttachments.getUpb(), screenAttachments.regionPos());
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z", shift = At.Shift.AFTER), method = "tick")
	public void postCheckContainer(CallbackInfo ci) {
		if (Minecraft.getInstance().player != null) {
			SUScreenAttachments screenAttachments = ((SUScreenAttachments) ((Player) (Object) this).containerMenu);
			PositionalInfo info = screenAttachments.getPositionalInfo();
			if (info != null) {
				info.reset((Player) (Object) this);
			}
		}
	}
}
