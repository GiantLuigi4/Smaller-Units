package tfc.smallerunits.mixin.quality;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.world.ITickerWorld;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Shadow
	public abstract void increaseScore(int pScore);
	
	@Inject(at = @At("TAIL"), method = "getDigSpeed", remap = false, cancellable = true)
	// odd, not sure why this doesn't have mappings
	public void afflictMiningSpeed(BlockState pState, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (!((Object) this instanceof FakePlayer)) {
			if (((Player) (Object) this).level instanceof ITickerWorld tickerWorld) {
				cir.setReturnValue(cir.getReturnValue() * tickerWorld.getUPB());
			}
		}
	}
}
