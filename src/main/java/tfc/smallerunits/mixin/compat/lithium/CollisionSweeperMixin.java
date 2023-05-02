package tfc.smallerunits.mixin.compat.lithium;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;

// TODO
//#if FABRIC==1
@Mixin(value = me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper.class, remap = false)
//#else
//$$@Mixin(value = com.abdelaziz.canary.common.entity.movement.ChunkAwareBlockCollisionSweeper.class, remap = false)
//#endif
public class CollisionSweeperMixin {
	@Unique
	boolean isSmallWorld = false;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(Level world, Entity entity, AABB box, CallbackInfo ci) {
		isSmallWorld = world instanceof ITickerLevel;
	}
	
	@Inject(at = @At("HEAD"), method = "nextSection", cancellable = true)
	public void preNextSection(CallbackInfoReturnable<Boolean> cir) {
		if (isSmallWorld) cir.setReturnValue(false);
		// TODO
	}
}
