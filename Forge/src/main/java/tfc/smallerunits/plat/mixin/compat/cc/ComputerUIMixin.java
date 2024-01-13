package tfc.smallerunits.plat.mixin.compat.cc;

import dan200.computercraft.shared.common.TileGeneric;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(value = TileGeneric.class, remap = false)
public class ComputerUIMixin {
	@Inject(at = @At("HEAD"), method = "isUsable", cancellable = true)
	public void preCheckValid(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (player.getLevel() instanceof ITickerLevel) {
			AttributeInstance instance = PlatformUtils.getReachAttrib(player);
			if (instance == null) return;
			AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);
			if (modifier == null) return;
			
			BlockEntity be = (BlockEntity) (Object) this;
			if (be.getLevel().getBlockEntity(be.worldPosition) != be) {
				cir.setReturnValue(false);
			} else {
				cir.setReturnValue(!(player.distanceToSqr((double)be.worldPosition.getX() + 0.5D, (double)be.worldPosition.getY() + 0.5D, (double)be.worldPosition.getZ() + 0.5D) > (64.0D * modifier.getAmount())));
			}
		}
	}
}
