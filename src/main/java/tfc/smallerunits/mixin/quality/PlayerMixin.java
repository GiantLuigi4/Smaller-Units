package tfc.smallerunits.mixin.quality;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(Player.class)
public abstract class PlayerMixin {
//	@Inject(at = @At("TAIL"), method = "getDestroySpeed", remap = false, cancellable = true)
//	public void afflictMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
//		//noinspection ConstantConditions
//		if (((Player) (Object) this).level instanceof ITickerLevel tickerWorld) {
//			cir.setReturnValue(cir.getReturnValue() * tickerWorld.getUPB());
//		}
//	}
}
