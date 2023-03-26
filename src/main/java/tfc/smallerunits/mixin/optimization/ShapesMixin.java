package tfc.smallerunits.mixin.optimization;

import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.utils.selection.UnitShape;

@Mixin(Shapes.class)
public class ShapesMixin {
	@Inject(at = @At("HEAD"), method = "joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z", cancellable = true)
	private static void preJoin(VoxelShape pShape1, VoxelShape pShape2, BooleanOp pResultOperator, CallbackInfoReturnable<Boolean> cir) {
		// REASON: prevent crashes while still maintaining vanilla behavior
		if (pResultOperator == BooleanOp.AND) {
			if (pShape1 instanceof UnitShape) cir.setReturnValue(((UnitShape) pShape1).intersects(pShape2));
			else if (pShape2 instanceof UnitShape) cir.setReturnValue(((UnitShape) pShape2).intersects(pShape1));
		}
		// tbf, there's no real point in me having an instance of check for the ONLY_FIRST and ONLY_SECOND, 'cuz the result is the same... just faster
		else if (pResultOperator == BooleanOp.FIRST) {
			if (pShape1 instanceof UnitShape || pShape2 instanceof UnitShape) cir.setReturnValue(pShape1.isEmpty());
			
		} else if (pResultOperator == BooleanOp.SECOND) {
			if (pShape1 instanceof UnitShape || pShape2 instanceof UnitShape) cir.setReturnValue(pShape2.isEmpty());
		} else if (pResultOperator == BooleanOp.NOT_SAME) {
			if (pShape2 instanceof UnitShape) {
				VoxelShape f = pShape2;
				pShape2 = pShape1;
				pShape1 = f;
			}
			if (pShape1 instanceof UnitShape) {
				int s0 = pShape1.toAabbs().size();
				int s1 = pShape2.toAabbs().size();
				if (s0 != s1) cir.setReturnValue(false);
					// TODO: check that s0 is 1
					// TODO: handling for s0 != 1
				else cir.setReturnValue(pShape1.bounds().equals(pShape2.bounds()));
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "joinUnoptimized", cancellable = true)
	private static void preDJoin(VoxelShape pShape1, VoxelShape pShape2, BooleanOp pFunction, CallbackInfoReturnable<VoxelShape> cir) {
		if (pFunction == BooleanOp.OR) {
			if (pShape1 instanceof UnitShape) cir.setReturnValue(su_or((UnitShape) pShape1, pShape2));
			else if (pShape2 instanceof UnitShape) cir.setReturnValue(su_or((UnitShape) pShape2, pShape1));
		} else if (pFunction == BooleanOp.FIRST) {
			if (pShape1 instanceof UnitShape || pShape2 instanceof UnitShape)
				cir.setReturnValue(pShape1);
		} else if (pFunction == BooleanOp.SECOND) {
			if (pShape1 instanceof UnitShape || pShape2 instanceof UnitShape)
				cir.setReturnValue(pShape2);
		}
	}
	
	@Unique
	private static VoxelShape su_or(UnitShape first, VoxelShape other) {
//		UnitShape sp = new UnitShape(first.space, first.visual, first.collisionContext);
//		for (AABB toAabb : other.toAabbs()) {
//			if (toAabb instanceof UnitBox) sp.addBox((UnitBox) toAabb);
//			else
//				sp.addBox(new UnitBox(toAabb.minX, toAabb.minY, toAabb.minZ, toAabb.maxX, toAabb.maxY, toAabb.maxZ, new BlockPos(0, 0, 0)));
//		}
//		for (AABB toAabb : first.toAabbs()) sp.addBox((UnitBox) toAabb);
//		return sp;
		return first; // TODO
	}
	
	// Testing purposes only
//	@Inject(at = @At("HEAD"), method = "create(DDDDDD)Lnet/minecraft/world/phys/shapes/VoxelShape;", cancellable = true)
//	private static void preCreate(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, CallbackInfoReturnable<VoxelShape> cir) {
//		UnitShape u = new UnitShape();
//		u.addBox(new UnitBox(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ, new BlockPos(0, 0, 0)));
//		cir.setReturnValue(u);
//	}
}
