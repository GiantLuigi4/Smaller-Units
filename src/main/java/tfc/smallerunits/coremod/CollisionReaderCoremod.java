package tfc.smallerunits.coremod;

import net.minecraft.entity.Entity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.HashSet;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class CollisionReaderCoremod {
	public static ReuseableStream<VoxelShape> append(ReuseableStream<VoxelShape> shapeStream, Entity entity, AxisAlignedBB aabb) {
		return new ReuseableStream<>(append(shapeStream.createStream(), entity, aabb));
	}
	
	public static Stream<VoxelShape> append(Stream<VoxelShape> shapeStream, Entity entity, AxisAlignedBB aabb) {
		if (entity == null) return shapeStream;
		World world = entity.getEntityWorld();
		HashSet<VoxelShape> shapes = new HashSet<>();
		shapeStream.forEach(shapes::add);
		int x1 = MathHelper.floor(aabb.minX) - 1;
		int x2 = MathHelper.ceil(aabb.maxX) + 1;
		int y1 = MathHelper.floor(aabb.minY) - 1;
		int y2 = MathHelper.ceil(aabb.maxY) + 1;
		int z1 = MathHelper.floor(aabb.minZ) - 1;
		int z2 = MathHelper.ceil(aabb.maxZ) + 1;
		VoxelShape entityShape = VoxelShapes.create(aabb);
		for (int x = x1; x < x2; x++) {
			for (int y = y1; y < y2; y++) {
				for (int z = z1; z < z2; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(world, pos);
					if (tileEntity == null) continue;
					VoxelShape shape = tileEntity.getBlockState().getCollisionShape(world, pos, ISelectionContext.forEntity(entity));
					shape = shape.withOffset(x, y, z);
					shape = VoxelShapes.combine(entityShape, shape, IBooleanFunction.AND);
					if (!shape.isEmpty())
						shapes.add(shape);
//					shapes.add(shape);
				}
			}
		}
		return Stream.of(shapes.toArray(new VoxelShape[0]));
	}

//	public static Vector3d collideBoundingBoxHeuristically(@Nullable Entity entity, Vector3d vec, AxisAlignedBB collisionBox, World world, ISelectionContext context, ReuseableStream<VoxelShape> potentialHits) {
//		potentialHits = append(potentialHits, entity, collisionBox);
//		boolean flag = vec.x == 0.0D;
//		boolean flag1 = vec.y == 0.0D;
//		boolean flag2 = vec.z == 0.0D;
//		if ((!flag || !flag1) && (!flag || !flag2) && (!flag1 || !flag2)) {
//			ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(potentialHits.createStream(), world.getCollisionShapes(entity, collisionBox.expand(vec))));
//			return collideBoundingBox(vec, collisionBox, reuseablestream);
//		} else {
//			return getAllowedMovement(vec, collisionBox, world, context, potentialHits);
//		}
//	}
}
