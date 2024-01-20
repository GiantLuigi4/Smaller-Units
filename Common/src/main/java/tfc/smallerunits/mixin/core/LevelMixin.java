package tfc.smallerunits.mixin.core;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public class LevelMixin {
    
//    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;unwrapKey()Ljava/util/Optional;"))
//    private java.util.Optional<net.minecraft.resources.ResourceKey<DimensionType>> patchDimType(Holder<DimensionType> instance) {
//        if (ThreadLocals.levelLocal.get() != null) {
//            return Optional.of(ThreadLocals.levelLocal.get().dimensionTypeId());
//        } else {
//            return instance.unwrapKey();
//        }
//    }
}
