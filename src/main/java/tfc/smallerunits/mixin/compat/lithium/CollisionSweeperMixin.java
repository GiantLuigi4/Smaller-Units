package tfc.smallerunits.mixin.compat.lithium;

import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
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
@Mixin(value = ChunkAwareBlockCollisionSweeper.class, remap = false)
public class CollisionSweeperMixin {
	@Shadow
	@Final
	private CollisionGetter view;
	
	@Unique
	boolean isSmallWorld = false;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(CollisionGetter view, Entity entity, AABB box, CallbackInfo ci) {
		isSmallWorld = view instanceof ITickerLevel;
	}
	
	@Inject(at = @At("HEAD"), method = "nextSection", cancellable = true)
	public void preNextSection(CallbackInfoReturnable<Boolean> cir) {
		if (isSmallWorld) cir.setReturnValue(false);
		// TODO
	}
}
