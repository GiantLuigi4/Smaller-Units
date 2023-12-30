package tfc.smallerunits.plat.mixin.quality;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.plat.asm.PlatformQol;

import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityQolForge {
	
	@Shadow
	protected Object2DoubleMap<FluidType> forgeFluidTypeHeight;
	@Shadow public Level level;
	
	@Shadow public abstract boolean touchingUnloadedChunk();
	
	@Shadow public abstract Vec3 getPosition(float f);
	
	@Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;
	
	// forge, casually rewriting the entirety of fluid logic
	@Inject(at = @At("RETURN"), method = "updateFluidHeightAndDoFluidPushing()V", remap = false)
	public void postCheckInFluid(CallbackInfo ci) {
		SU$runPerWorld((level, regionPos) -> {
			// TODO: optimize?
			PlatformQol.runSUFluidCheck((Entity) (Object) this, level, regionPos, fluidHeight, forgeFluidTypeHeight);
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
