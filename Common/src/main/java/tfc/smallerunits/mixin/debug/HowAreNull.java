package tfc.smallerunits.mixin.debug;

import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.class)
public class HowAreNull {
	@Inject(at = @At("HEAD"), method = "isWithinBounds(Lnet/minecraft/world/phys/AABB;)Z", cancellable = true)
	public void idkHowBoxIsNull(AABB pBox, CallbackInfoReturnable<Boolean> cir) {
		if (pBox == null) {
			System.out.println("???");
			cir.setReturnValue(true);
		}
	}
}
