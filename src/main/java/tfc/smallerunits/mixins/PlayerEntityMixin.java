package tfc.smallerunits.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.smallerunits.helpers.ContainerMixinHelper;
import tfc.smallerunits.utils.accessor.IAmContainer;

@Mixin({PlayerEntity.class, ServerPlayerEntity.class})
public class PlayerEntityMixin {
	//thanks to Upcraft in MMD, I now have this which makes for near perfect mod compatibility for containers
	//
	//I say "near" because *gestures at refined storage*
	//
	//
	//
	//https://discord.com/channels/176780432371744769/750505199415590942/812385811583729754
	//https://discord.mcmoddev.com/
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/container/Container;canInteractWith(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
	public boolean smaller_units_canInteractWith(Container container, PlayerEntity playerIn) {
//		return (!ContainerMixinHelper.getNaturallyClosable(container)) || container.canInteractWith(playerIn);
		if (container instanceof IAmContainer) {
			if (!((IAmContainer) container).SmallerUnits_canCloseNaturally()) {
				if (ContainerMixinHelper.isVanilla(container)) return true;
//				return container.canInteractWith(playerIn);
				return true;
			}
		}
		return container.canInteractWith(playerIn);
	}
}
