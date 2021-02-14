package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerContainer.class, ChestContainer.class, FurnaceContainer.class, AbstractFurnaceContainer.class, EnchantmentContainer.class, HopperContainer.class, LoomContainer.class, WorkbenchContainer.class, SmithingTableContainer.class, RepairContainer.class, DispenserContainer.class})
public class ContainerMixin2 {
	@Inject(at = @At("HEAD"), method = "canInteractWith(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
	public void canInteract(PlayerEntity playerIn, CallbackInfoReturnable<Boolean> cir) {
		if (!ContainerMixinHelper.getNaturallyClosable((Container) (Object) this)) {
			cir.setReturnValue(true);
		}
	}
}
