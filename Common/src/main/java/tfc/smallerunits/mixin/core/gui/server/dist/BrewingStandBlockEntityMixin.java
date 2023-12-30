package tfc.smallerunits.mixin.core.gui.server.dist;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	@Inject(at = @At("HEAD"), method = "stillValid", cancellable = true)
	public void scale(Player $$0, CallbackInfoReturnable<Boolean> cir) {
		if ($$0.getLevel() instanceof ITickerLevel) {
			AttributeInstance instance = PlatformUtils.getReachAttrib($$0);
			if (instance == null) return;
			AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);
			if (modifier == null) return;
			
			BlockEntity be = (BlockEntity) (Object) this;
			if (be.getLevel().getBlockEntity(be.worldPosition) != be) {
				cir.setReturnValue(false);
			} else {
				cir.setReturnValue(!($$0.distanceToSqr((double)be.worldPosition.getX() + 0.5D, (double)be.worldPosition.getY() + 0.5D, (double)be.worldPosition.getZ() + 0.5D) > (64.0D * modifier.getAmount())));
			}
		}
	}
}
