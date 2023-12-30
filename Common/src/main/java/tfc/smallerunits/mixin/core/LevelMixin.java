package tfc.smallerunits.mixin.core;

import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.smallerunits.utils.threading.ThreadLocals;

import java.util.Optional;

@Mixin(Level.class)
public class LevelMixin {
    
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;unwrapKey()Ljava/util/Optional;"))
    private java.util.Optional<net.minecraft.resources.ResourceKey<DimensionType>> patchDimType(Holder<DimensionType> instance) {
        if (ThreadLocals.levelLocal.get() != null) {
            return Optional.of(ThreadLocals.levelLocal.get().dimensionTypeId());
        } else {
            return instance.unwrapKey();
        }
    }
}
