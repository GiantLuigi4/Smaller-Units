package tfc.smallerunits.plat.mixin.quality;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.plat.asm.PlatformQol;

import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityQolFabric {
	
	@Shadow public Level level;
	
	@Shadow public abstract boolean touchingUnloadedChunk();
	
	@Shadow public abstract Vec3 getPosition(float f);
	
	@Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
	
	
	@Inject(at = @At("RETURN"), method = "updateFluidHeightAndDoFluidPushing", cancellable = true)
	public void postCheckInFluid(TagKey<Fluid> fluids, double something, CallbackInfoReturnable<Boolean> cir) {
		boolean wasInFluid = cir.getReturnValueZ();
		final boolean[] inFluid = {wasInFluid};
		SU$runPerWorld((level, regionPos) -> {
			inFluid[0] = PlatformQol.runSUFluidCheck((Entity) (Object) this, fluids, something, level, regionPos, fluidHeight) || inFluid[0];
		});
		if (inFluid[0] && !wasInFluid)
			cir.setReturnValue(true);
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
