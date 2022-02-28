package tfc.smallerunits.mixins.breaking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.utils.accessor.IBlockBreaker;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Nullable
	public PlayerController playerController;
	
	boolean wasClicked = false;
	
	@Inject(at = @At("HEAD"), method = "sendClickBlockToController")
	public void preClick(boolean p_147115_1_, CallbackInfo ci) {
		if (wasClicked && !p_147115_1_) {
			assert this.playerController != null; // this should never be called while controller is null
			((IBlockBreaker) this.playerController).SmallerUnits_resetBreaking();
		}
		wasClicked = p_147115_1_;
	}
}
