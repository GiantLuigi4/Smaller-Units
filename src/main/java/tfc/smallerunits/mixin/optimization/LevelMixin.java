package tfc.smallerunits.mixin.optimization;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow
	public abstract boolean isClientSide();
	
	@Inject(at = @At("HEAD"), method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
	public void preSetBlockState(BlockPos levelchunk, BlockState block, int blockSnapshot, int old, CallbackInfoReturnable<Boolean> cir) {
		// TODO: test this, lol
		if (this instanceof RegionalAttachments) {
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						RegionPos pos = new RegionPos(new BlockPos(
								levelchunk.getX() - x * 15,
								levelchunk.getY() - y * 15,
								levelchunk.getZ() - z * 15
						));
//						Region r = ((RegionalAttachments) this).SU$getRegion(new RegionPos(levelchunk));
						Region r = ((RegionalAttachments) this).SU$getRegion(pos);
						if (r == null) return;
						r.updateWorlds(levelchunk);
					}
				}
			}
		}
	}
}
