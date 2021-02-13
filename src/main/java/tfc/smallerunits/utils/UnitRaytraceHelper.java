package tfc.smallerunits.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import tfc.smallerunits.block.UnitTileEntity;

import java.util.ArrayList;
import java.util.Optional;

public class UnitRaytraceHelper {
	public static UnitRaytraceContext raytraceBlock(UnitTileEntity tileEntity, Entity entity, boolean includeGround, BlockPos pos, Optional<ISelectionContext> contextOptional) {
		VoxelShape shape = null;
		
		Vector3d start = entity.getEyePosition(0);
		double reach = 8;
		
		if (entity instanceof PlayerEntity)
			reach = ((LivingEntity) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d look = entity.getLookVec().scale(reach);
		Vector3d end = entity.getEyePosition(0).add(look);
		
		double bestDist = Double.POSITIVE_INFINITY;
		
		BlockPos hitPos = null;
		Vector3d hitVec = null;
		
		for (SmallUnit unit : tileEntity.world.blockMap.values()) {
			VoxelShape shape1;
			if (contextOptional.isPresent())
				shape1 = unit.state.getShape(tileEntity.world, unit.pos, contextOptional.get());
			else shape1 = unit.state.getShape(tileEntity.world, unit.pos);
			ArrayList<AxisAlignedBB> aabbs = shrink(shape1, tileEntity.unitsPerBlock);
			
			for (AxisAlignedBB axisAlignedBB : aabbs) {
				axisAlignedBB = axisAlignedBB.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, unit.pos.getY() / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
				axisAlignedBB = axisAlignedBB.offset(pos.getX(), pos.getY(), pos.getZ());
				
				Optional<Vector3d> intercept = axisAlignedBB.rayTrace(start, end);
				if (!intercept.isPresent()) continue;
				
				double dist = intercept.get().distanceTo(start);
				if (dist > bestDist) continue;
				
				bestDist = dist;
				VoxelShape theShape = VoxelShapes.empty();
				
				for (AxisAlignedBB axisAlignedBB1 : aabbs) {
					axisAlignedBB1 = axisAlignedBB1.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, unit.pos.getY() / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
					theShape = VoxelShapes.or(theShape, VoxelShapes.create(axisAlignedBB1));
				}
				
				shape = theShape;
				hitPos = unit.pos;
				hitVec = intercept.get();
			}
		}
		
		if (shape == null || hitPos == null)
			return new UnitRaytraceContext(VoxelShapes.empty(), new BlockPos(-100, -100, -100), new Vector3d(-100, -100, -100));
		
		return new UnitRaytraceContext(shape, hitPos, hitVec);
	}
	
	public static ArrayList<AxisAlignedBB> shrink(VoxelShape shape, int scale) {
		ArrayList<AxisAlignedBB> newBoundingBoxes = new ArrayList<>();
		
		for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
			axisAlignedBB = new AxisAlignedBB(
					axisAlignedBB.minX / scale, axisAlignedBB.minY / scale, axisAlignedBB.minZ / scale,
					axisAlignedBB.maxX / scale, axisAlignedBB.maxY / scale, axisAlignedBB.maxZ / scale
			);
			newBoundingBoxes.add(axisAlignedBB);
		}
		
		return newBoundingBoxes;
	}
}
