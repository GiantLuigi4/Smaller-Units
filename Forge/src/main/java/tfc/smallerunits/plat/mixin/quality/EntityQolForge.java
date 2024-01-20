package tfc.smallerunits.plat.mixin.quality;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.plat.asm.PlatformQol;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityQolForge {
	
	@Shadow
	public Level level;
	
	@Shadow
	public abstract boolean touchingUnloadedChunk();
	
	@Shadow
	public abstract Vec3 getPosition(float f);
	
	@Shadow
	protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
	
	@Shadow
	public abstract float getEyeHeight();
	
	@Shadow
	public abstract AABB getBoundingBox();
	
	@Shadow
	@Final
	private Set<TagKey<Fluid>> fluidOnEyes;
	
	@Inject(at = @At("RETURN"), method = "updateFluidHeightAndDoFluidPushing", cancellable = true)
	public void postCheckInFluid(TagKey<Fluid> fluids, double something, CallbackInfoReturnable<Boolean> cir) {
		boolean wasInFluid = cir.getReturnValueZ();
		final boolean[] inFluid = {wasInFluid};
		SU$runPerWorld((level, regionPos) -> {
			inFluid[0] = inFluid[0] || PlatformQol.runSUFluidCheck((Entity) (Object) this, fluids, something, level, regionPos, fluidHeight);
		});
		if (inFluid[0] && !wasInFluid)
			cir.setReturnValue(true);
	}
	
	@Inject(at = @At("RETURN"), method = "updateFluidOnEyes")
	public void postCheckFluidOnEyes(CallbackInfo ci) {
		SU$runPerWorld((level, pos) -> {
//			if (!forgeFluidTypeOnEyes.isAir()) return;
			
			AABB aabb = HitboxScaling.getOffsetAndScaledBox(this.getBoundingBox().deflate(0.001D), this.getPosition(0), ((ITickerLevel) level).getUPB(), pos);
			
			double d0 = (this.getEyeHeight()/* - (double) 0.11111111F*/) * ((ITickerLevel) level).getUPB();
			BlockPos blockpos = new BlockPos(aabb.getCenter().x, aabb.minY + d0, aabb.getCenter().z);
			FluidState fluidstate = level.getFluidState(blockpos);
			double d1 = ((float) blockpos.getY() + fluidstate.getHeight(level, blockpos));
			if (d1 > d0) {
				if (!fluidstate.isEmpty()) {
					fluidstate.getTags().forEach((t) -> fluidOnEyes.add(t));
				}
			}
		});
	}
	
	@Unique
	private void SU$runPerWorld(BiConsumer<Level, RegionPos> action) {
		if (!(level instanceof RegionalAttachments)) return;
		if (touchingUnloadedChunk()) return;
		Vec3 position = getPosition(0);
		RegionPos regionPos = new RegionPos(new BlockPos(position));
		Region region = ((RegionalAttachments) level).SU$getRegionMap().get(regionPos);
		if (region != null) {
			for (Level regionLevel : region.getLevels()) {
				if (regionLevel != null) {
					action.accept(regionLevel, regionPos);
				}
			}
		}
	}
}
