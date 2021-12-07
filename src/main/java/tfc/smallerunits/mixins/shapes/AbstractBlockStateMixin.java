package tfc.smallerunits.mixins.shapes;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.data.SUCapabilityManager;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
	@Unique
	ThreadLocal<Boolean> isGettingCollisionShape = ThreadLocal.withInitial(() -> false);
	
	@Shadow
	public abstract Block getBlock();
	
	@Inject(at = @At("TAIL"), method = "getShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;", cancellable = true)
	public void postGetShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (
			// mixin is annoying and checks that the classes being referenced actually exist
				Smallerunits.useSelectionReversion(worldIn)
		) return;
		if (this.getBlock() instanceof SmallerUnitBlock) return;
		if (isGettingCollisionShape.get()) return;
		if (worldIn instanceof World) {
			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock((World) worldIn, pos);
			if (tileEntity == null) return;
			
			BlockState state = tileEntity.getBlockState();
			VoxelShape unitShape = state.getBlock().getShape(state, worldIn, pos, context);
			if (unitShape.isEmpty()) return;
			else {
				cir.setReturnValue(VoxelShapes.combine(
						cir.getReturnValue(), unitShape, IBooleanFunction.OR
				));
			}
		}
		isGettingCollisionShape.remove();
	}

//	@Inject(at = @At("TAIL"), method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;", cancellable = true)
//	public void postGetCollisionShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
//		if (worldIn instanceof World) {
//			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock((World) worldIn, pos);
//			if (tileEntity == null) return;
//
//			BlockState state = tileEntity.getBlockState();
//			VoxelShape unitShape = state.getBlock().getCollisionShape(state, worldIn, pos, context);
//			if (unitShape.isEmpty()) return;
//			else {
//				cir.setReturnValue(VoxelShapes.combine(
//						cir.getReturnValue(), unitShape, IBooleanFunction.OR
//				));
//			}
//		}
//	}
	
	@Inject(at = @At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;")
	public void preGetCollisionShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		isGettingCollisionShape.set(true);
	}
	
	@Inject(at = @At("TAIL"), method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;")
	public void postGetCollisionShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context, CallbackInfoReturnable<VoxelShape> cir) {
		isGettingCollisionShape.remove();
	}

//	@Inject(at = @At("TAIL"), method = "getCollisionShape(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/shapes/VoxelShape;", cancellable = true)
//	public void postGetCollisionShape(IBlockReader worldIn, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
//		isGettingCollisionShape = true;
//		if (worldIn instanceof World) {
//			UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock((World) worldIn, pos);
//			if (tileEntity == null) {
//				isGettingCollisionShape = false;
//				return;
//			}
//
//			BlockState state = tileEntity.getBlockState();
//			VoxelShape unitShape = state.getBlock().getShape(state, worldIn, pos, ISelectionContext.dummy());
//			if (unitShape.isEmpty()) {
//				isGettingCollisionShape = false;
//				return;
//			}
//			else {
//				cir.setReturnValue(VoxelShapes.combine(
//						cir.getReturnValue(), unitShape, IBooleanFunction.OR
//				));
//			}
//		}
//		isGettingCollisionShape = false;
//	}
}
