package tfc.smallerunits.mixin.core.gui.server.dist;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@Inject(at = @At("HEAD"), method = "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z", cancellable = true)
	private static void scale(ContainerLevelAccess $$0, Player $$1, Block $$2, CallbackInfoReturnable<Boolean> cir) {
		if ($$1.getLevel() instanceof ITickerLevel) {
			AttributeInstance instance = PlatformUtils.getReachAttrib($$1);
			if (instance == null) return;
			AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);
			if (modifier == null) return;
			
			cir.setReturnValue($$0.evaluate(
					(var2x, var3) -> !var2x.getBlockState(var3).is($$2)
							? false
							: $$1.distanceToSqr((double)var3.getX() + 0.5, (double)var3.getY() + 0.5, (double)var3.getZ() + 0.5) <= (64.0 * modifier.getAmount()),
					true
			));
		}
	}
}
