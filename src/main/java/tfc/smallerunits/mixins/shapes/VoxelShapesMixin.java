package tfc.smallerunits.mixins.shapes;

import net.minecraft.util.AxisRotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.helpers.VoxelShapesMixinHelper;

import java.util.stream.Stream;

@Mixin(VoxelShapes.class)
public class VoxelShapesMixin {
	@Inject(at = @At("RETURN"), method = "getAllowedOffset(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/world/IWorldReader;DLnet/minecraft/util/math/shapes/ISelectionContext;Lnet/minecraft/util/AxisRotation;Ljava/util/stream/Stream;)D", cancellable = true)
	private static void postGetOffset(AxisAlignedBB collisionBox, IWorldReader worldReader, double desiredOffset, ISelectionContext selectionContext, AxisRotation rotationAxis, Stream<VoxelShape> possibleHits, CallbackInfoReturnable<Double> cir) {
		VoxelShapesMixinHelper.getOffset(collisionBox, worldReader, desiredOffset, selectionContext, rotationAxis, possibleHits, cir);
	}
}
