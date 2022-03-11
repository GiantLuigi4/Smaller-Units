package tfc.smallerunits.mixin.optimization;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.utils.selection.UnitShape;

@Mixin(Shapes.class)
public class ShapesMixin {
	@Inject(at = @At("HEAD"), method = "joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z", cancellable = true)
	private static void preJoin(VoxelShape pShape1, VoxelShape pShape2, BooleanOp pResultOperator, CallbackInfoReturnable<Boolean> cir) {
		// REASON: prevent crash while still maintaining vanilla behavior
		if (pResultOperator == BooleanOp.AND) {
			if (pShape1 instanceof UnitShape) cir.setReturnValue(((UnitShape) pShape1).intersects(pShape2));
			else if (pShape2 instanceof UnitShape) cir.setReturnValue(((UnitShape) pShape2).intersects(pShape1));
		}
	}
}
