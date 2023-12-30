package tfc.smallerunits.mixin.quality;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {
	@Inject(at = @At("TAIL"), method = "isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z", remap = false)
	public void preCheckHarvestLevel(ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		// TODO, maybe
		// awaiting poll to age a bit
	}
}
