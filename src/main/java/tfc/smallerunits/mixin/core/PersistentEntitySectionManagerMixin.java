package tfc.smallerunits.mixin.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.entity.Visibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.world.ITickerWorld;

@Mixin(PersistentEntitySectionManager.class)
public class PersistentEntitySectionManagerMixin {
	@Inject(at = @At("HEAD"), method = "getEffectiveStatus", cancellable = true)
	private static <T extends EntityAccess> void preGetEffectiveStatus(T pEntity, Visibility pVisibility, CallbackInfoReturnable<Visibility> cir) {
		if (pEntity instanceof Entity) {
			if (((Entity) pEntity).getLevel() instanceof ITickerWorld) {
				cir.setReturnValue(Visibility.TICKING);
			}
		}
	}
}
