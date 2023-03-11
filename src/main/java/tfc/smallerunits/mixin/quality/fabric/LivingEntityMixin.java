package tfc.smallerunits.mixin.quality.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.utils.platform.hooks.ILadderBlock;

import java.util.Optional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Shadow
	private Optional<BlockPos> lastClimbablePos;
	
	@Inject(at = @At("TAIL"), method = "onClimbable", cancellable = true)
	public void postCheckClimable(CallbackInfoReturnable<Boolean> cir) {
		LivingEntity entity = ((LivingEntity) (Object) this);
		BlockPos pos = entity.blockPosition();
		BlockState state = entity.getLevel().getBlockState(pos);
		if (state.getBlock() instanceof ILadderBlock ladderBlock) {
			if (ladderBlock.isLadder(
					state, entity.getLevel(),
					pos, entity
			)) {
				lastClimbablePos = Optional.of(pos);
				cir.setReturnValue(true);
			}
		}
	}
}
