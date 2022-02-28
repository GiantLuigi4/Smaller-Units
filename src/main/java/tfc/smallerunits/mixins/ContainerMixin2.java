package tfc.smallerunits.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.helpers.ContainerMixinHelper;
import tfc.smallerunits.utils.accessor.IAmContainer;

@Mixin({
		PlayerContainer.class,
		ChestContainer.class,
		AbstractFurnaceContainer.class,
		EnchantmentContainer.class,
		HopperContainer.class,
		LoomContainer.class,
		WorkbenchContainer.class,
		AbstractRepairContainer.class,
		CartographyContainer.class,
		GrindstoneContainer.class,
		StonecutterContainer.class,
		DispenserContainer.class,
		LecternContainer.class,
		BrewingStandContainer.class
})
public class ContainerMixin2 {
	@Inject(at = @At("HEAD"), method = "canInteractWith", cancellable = true)
	public void canInteract(PlayerEntity playerIn, CallbackInfoReturnable<Boolean> cir) {
		// this uh... this kinda passes all checks above... 100% of the time...
		if (!ContainerMixinHelper.isVanilla((Container) (Object) this)) {
			if (this instanceof IAmContainer) {
				if (!((IAmContainer) this).SmallerUnits_canCloseNaturally()) {
					cir.setReturnValue(true);
				}
			}
		}
	}
}
