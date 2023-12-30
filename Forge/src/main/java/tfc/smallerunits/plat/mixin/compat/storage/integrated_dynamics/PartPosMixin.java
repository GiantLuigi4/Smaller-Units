package tfc.smallerunits.plat.mixin.compat.storage.integrated_dynamics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.ref.WeakReference;

@Mixin(value = DimPos.class, remap = false)
public class PartPosMixin {
    @Inject(at = @At("RETURN"), method = "of(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lorg/cyclops/cyclopscore/datastructure/DimPos;")
    private static void posOf(Level world, BlockPos blockPos, CallbackInfoReturnable<DimPos> cir) {
        cir.getReturnValue().setWorldReference(new WeakReference<>(world));
    }
}
