package tfc.smallerunits.mixins.screens;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.AbstractRepairContainer;
import net.minecraft.util.IWorldPosCallable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.helpers.ContainerMixinHelper;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

@Mixin(AbstractRepairContainer.class)
public abstract class RepairContainerMixin {
	@Shadow
	@Final
	protected IWorldPosCallable field_234644_e_;
	
	@Shadow
	protected abstract boolean func_230302_a_(BlockState blockState);
	
	@Inject(at = @At("HEAD"), method = "canInteractWith", cancellable = true)
	public void preCheckUsability(PlayerEntity p_75145_1_, CallbackInfoReturnable<Boolean> cir) {
		field_234644_e_.consume((world, pos) -> {
			boolean isFakeWorld = false;
			if (world instanceof FakeServerWorld) isFakeWorld = true;
			else if (world.isRemote && ClientUtils.checkFakeClientWorld(world)) isFakeWorld = true;
			
			if (isFakeWorld) {
				Object o = ContainerMixinHelper.getOwner(world);
				if (o == null) {
					cir.setReturnValue(false);
					return;
				}
				if (!func_230302_a_(world.getBlockState(pos))) {  // is state supported
					cir.setReturnValue(false);
					return;
				}
				if (pos instanceof UnitPos) cir.setReturnValue(ContainerMixinHelper.checkReach(p_75145_1_, pos));
			}
		});
	}
}
