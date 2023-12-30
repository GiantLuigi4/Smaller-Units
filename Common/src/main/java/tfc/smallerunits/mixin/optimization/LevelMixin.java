package tfc.smallerunits.mixin.optimization;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow
	public abstract boolean isClientSide();
	
	@Inject(at = @At("HEAD"), method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
	public void preSetBlockState(BlockPos levelchunk, BlockState block, int blockSnapshot, int old, CallbackInfoReturnable<Boolean> cir) {
		// TODO: test this, lol
		if (this instanceof RegionalAttachments) {
			ITickerLevel.update((Level) (Object) this, levelchunk);
		}
	}
}
