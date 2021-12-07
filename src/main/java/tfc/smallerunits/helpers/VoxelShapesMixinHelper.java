package tfc.smallerunits.helpers;

import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.collision.AxisAlignedBBHelper;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.stream.Stream;

public class VoxelShapesMixinHelper {
	public static double calcAllowedOffset(VoxelShape shape, Direction.Axis movementAxis, AxisAlignedBB collisionBox, double desiredOffset) {
		return calcAllowedOffset(shape, AxisRotation.from(movementAxis, Direction.Axis.X), collisionBox, desiredOffset);
	}
	
	public static double calcAllowedOffset(VoxelShape shape, AxisRotation movementAxis, AxisAlignedBB collisionBox, double desiredOffset) {
		Direction.Axis direction$axis2 = movementAxis.rotate(Direction.Axis.Z);
		for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
			switch (direction$axis2) {
				case Y:
					desiredOffset = AxisAlignedBBHelper.calculateXOffset(axisAlignedBB, collisionBox, desiredOffset);
					break;
				case Z:
					desiredOffset = AxisAlignedBBHelper.calculateYOffset(axisAlignedBB, collisionBox, desiredOffset);
					break;
				case X:
					desiredOffset = AxisAlignedBBHelper.calculateZOffset(axisAlignedBB, collisionBox, desiredOffset);
					break;
			}
		}
		return desiredOffset;
	}
	
	public static void getOffset(AxisAlignedBB collisionBox, IWorldReader worldReader, double desiredOffset, ISelectionContext selectionContext, AxisRotation rotationAxis, Stream<VoxelShape> possibleHits, CallbackInfoReturnable<Double> cir) {
		if (Smallerunits.useCollisionReversion(worldReader)) return;
		
		if (!(worldReader instanceof World)) return;
		
		if (desiredOffset == 0) return;
		
		double initV = desiredOffset;
		
		Direction.Axis direction$axis2 = rotationAxis.rotate(Direction.Axis.Z);
		
		int dir = direction$axis2.getCoordinate(0, 1, 2);
		
		AxisAlignedBB aabb;
		if (dir == 0) aabb = collisionBox.grow(desiredOffset, 0, 0);
		else if (dir == 1) aabb = collisionBox.grow(0, desiredOffset, 0);
		else aabb = collisionBox.grow(0, 0, desiredOffset);

//		VoxelShape entityShape = VoxelShapes.create(aabb);
		int x1 = MathHelper.floor(aabb.minX) - 1;
		int x2 = MathHelper.ceil(aabb.maxX) + 1;
		int y1 = MathHelper.floor(aabb.minY) - 1;
		int y2 = MathHelper.ceil(aabb.maxY) + 1;
		int z1 = MathHelper.floor(aabb.minZ) - 1;
		int z2 = MathHelper.ceil(aabb.maxZ) + 1;
		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				for (int z = z1; z < z2; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock((World) worldReader, pos);
					if (tileEntity == null) continue;
					VoxelShape shape = tileEntity.getBlockState().getCollisionShape(worldReader, pos, ISelectionContext.dummy());
//					shape = shape.withOffset(x, y, z);
//					shape = VoxelShapes.combine(entityShape, shape, IBooleanFunction.AND);
					if (!shape.isEmpty()) {
						desiredOffset = VoxelShapesMixinHelper.calcAllowedOffset(
								shape,
								direction$axis2,
								collisionBox.offset(-x, -y, -z),
								desiredOffset
						);
//						desiredOffset =
//								shape
//										.getAllowedOffset(
//												direction$axis2,
//												collisionBox.offset(-x, -y, -z),
//												desiredOffset
//										)
//						;
					}
//					shapes.add(shape);
				}
			}
		}
		if (initV != desiredOffset) cir.setReturnValue(desiredOffset);
	}
}
