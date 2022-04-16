package tfc.smallerunits.mixin.quality;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	public Level level;
	
	@Shadow
	public abstract boolean touchingUnloadedChunk();
	
	@Shadow
	public abstract boolean isPushedByFluid();
	
	@Shadow
	public abstract AABB getBoundingBox();
	
	// TODO
	@Inject(at = @At("HEAD"), method = "updateFluidHeightAndDoFluidPushing", cancellable = true)
	public void preCheckInFluid(Tag<Fluid> d1, double fluidstate, CallbackInfoReturnable<Boolean> cir) {
		if (touchingUnloadedChunk()) return;
		AABB aabb = this.getBoundingBox().deflate(0.001D);
		int i = Mth.floor(aabb.minX);
		int j = Mth.ceil(aabb.maxX);
		int k = Mth.floor(aabb.minY);
		int l = Mth.ceil(aabb.maxY);
		int i1 = Mth.floor(aabb.minZ);
		int j1 = Mth.ceil(aabb.maxZ);
		boolean flag = this.isPushedByFluid();
		Vec3 vec3 = Vec3.ZERO;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		
		for (int l1 = i; l1 < j; ++l1) {
			for (int i2 = k; i2 < l; ++i2) {
				for (int j2 = i1; j2 < j1; ++j2) {
					blockpos$mutableblockpos.set(l1, i2, j2);
					FluidState fluidState = this.level.getFluidState(blockpos$mutableblockpos);
					if (fluidState.is(d1)) {
					}
				}
			}
		}
	}
}
