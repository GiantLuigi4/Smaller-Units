package com.tfc.smallerunits.utils;

import com.tfc.smallerunits.block.UnitTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

import java.util.ArrayList;
import java.util.Optional;

public class UnitRaytraceHelper {
	public static UnitRaytraceContext raytraceBlock(UnitTileEntity tileEntity, Entity entity, boolean includeGround, BlockPos pos, Optional<ISelectionContext> contextOptional) {
		VoxelShape shape = null;
		
		Vector3d start1 = entity.getEyePosition(0);
		double reach = 8;
		
		if (entity instanceof PlayerEntity)
			reach = ((LivingEntity) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d look = entity.getLookVec().scale(reach);
		Vector3d end = entity.getEyePosition(0).add(look);
		
		double bestDist = Double.POSITIVE_INFINITY;
		
		BlockPos hitPos = null;
		Vector3d hitVec = null;
		
		Optional<Direction> hitFace = Optional.empty();
		
		for (SmallUnit unit : tileEntity.getBlockMap().values()) {
			VoxelShape shape1;
			if (contextOptional.isPresent())
				shape1 = unit.state.getShape(tileEntity.getFakeWorld(), unit.pos, contextOptional.get());
			else shape1 = unit.state.getShape(tileEntity.getFakeWorld(), unit.pos, ISelectionContext.dummy());
			ArrayList<AxisAlignedBB> aabbs = shrink(shape1, tileEntity.unitsPerBlock);
			
			for (AxisAlignedBB axisAlignedBB : aabbs) {
				axisAlignedBB = axisAlignedBB.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, (unit.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
				axisAlignedBB = axisAlignedBB.offset(pos.getX(), pos.getY(), pos.getZ());
				
				Vector3d start = start1;
				if (axisAlignedBB.contains(start))
					start = start1.add(look.scale(-1));
				Optional<Vector3d> intercept = axisAlignedBB.rayTrace(start, end);
				if (!intercept.isPresent()) continue;
				
				double dist = intercept.get().distanceTo(start);
				if (axisAlignedBB.contains(start1))
					dist = Double.MIN_VALUE;
				if (dist > bestDist) continue;
				
				bestDist = dist;
				VoxelShape theShape = VoxelShapes.empty();
				
				for (AxisAlignedBB axisAlignedBB1 : aabbs) {
					axisAlignedBB1 = axisAlignedBB1.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, (unit.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
					theShape = VoxelShapes.combine(theShape, VoxelShapes.create(axisAlignedBB1), IBooleanFunction.OR);
				}
				
				shape = theShape;
				hitPos = unit.pos;
				hitVec = intercept.get();
				Vector3d vector3d = hitVec.subtract(start);
				hitFace = Optional.of(Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z).getOpposite());
				for (Direction value : Direction.values()) {
					if (value.getXOffset() < 0) {
						if (axisAlignedBB.minX == hitVec.x) {
							hitFace = Optional.of(value);
						}
					} else if (value.getXOffset() > 0) {
						if (axisAlignedBB.maxX == hitVec.x) {
							hitFace = Optional.of(value);
						}
					} else if (value.getYOffset() < 0) {
						if (axisAlignedBB.minY == hitVec.y) {
							hitFace = Optional.of(value);
						}
					} else if (value.getYOffset() > 0) {
						if (axisAlignedBB.maxY == hitVec.y) {
							hitFace = Optional.of(value);
						}
					} else if (value.getZOffset() < 0) {
						if (axisAlignedBB.minZ == hitVec.z) {
							hitFace = Optional.of(value);
						}
					} else if (value.getZOffset() > 0) {
						if (axisAlignedBB.maxZ == hitVec.z) {
							hitFace = Optional.of(value);
						}
					}
				}
			}
		}
		
		UnitRaytraceContext context = new UnitRaytraceContext(VoxelShapes.empty(), new BlockPos(-100, -100, -100), new Vector3d(-100, -100, -100));
		context.hitFace = hitFace;
		if (shape == null || hitPos == null)
			return context;
		
		context = new UnitRaytraceContext(shape, hitPos, hitVec);
		context.hitFace = hitFace;
		return context;
	}
	
	public static UnitRaytraceContext raytraceBlockWithoutShape(UnitTileEntity tileEntity, Entity entity, boolean includeGround, BlockPos pos, Optional<ISelectionContext> contextOptional) {
		Vector3d start = entity.getEyePosition(0);
		double reach = 8;
		
		if (entity instanceof PlayerEntity)
			reach = ((LivingEntity) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d look = entity.getLookVec().scale(reach);
		Vector3d end = entity.getEyePosition(0).add(look);
		
		double bestDist = Double.POSITIVE_INFINITY;
		
		BlockPos hitPos = null;
		Vector3d hitVec = null;
		
		Optional<Direction> hitFace = Optional.empty();
		
		for (SmallUnit unit : tileEntity.getBlockMap().values()) {
			VoxelShape shape1;
			if (contextOptional.isPresent())
				shape1 = unit.state.getShape(tileEntity.getFakeWorld(), unit.pos, contextOptional.get());
			else shape1 = unit.state.getShape(tileEntity.getFakeWorld(), unit.pos, ISelectionContext.dummy());
			ArrayList<AxisAlignedBB> aabbs = shrink(shape1, tileEntity.unitsPerBlock);
			
			for (AxisAlignedBB axisAlignedBB : aabbs) {
				axisAlignedBB = axisAlignedBB.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, (unit.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
				axisAlignedBB = axisAlignedBB.offset(pos.getX(), pos.getY(), pos.getZ());
				
				Optional<Vector3d> intercept = axisAlignedBB.rayTrace(start, end);
				if (!intercept.isPresent()) continue;
				
				double dist = intercept.get().distanceTo(start);
				if (dist > bestDist) continue;
				
				bestDist = dist;
				hitPos = unit.pos;
				hitVec = intercept.get();
				Vector3d vector3d = hitVec.subtract(start);
				hitFace = Optional.of(Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z).getOpposite());
				for (Direction value : Direction.values()) {
					if (value.getXOffset() < 0) {
						if (axisAlignedBB.minX == hitVec.x) {
							hitFace = Optional.of(value);
						}
					} else if (value.getXOffset() > 0) {
						if (axisAlignedBB.maxX == hitVec.x) {
							hitFace = Optional.of(value);
						}
					} else if (value.getYOffset() < 0) {
						if (axisAlignedBB.minY == hitVec.y) {
							hitFace = Optional.of(value);
						}
					} else if (value.getYOffset() > 0) {
						if (axisAlignedBB.maxY == hitVec.y) {
							hitFace = Optional.of(value);
						}
					} else if (value.getZOffset() < 0) {
						if (axisAlignedBB.minZ == hitVec.z) {
							hitFace = Optional.of(value);
						}
					} else if (value.getZOffset() > 0) {
						if (axisAlignedBB.maxZ == hitVec.z) {
							hitFace = Optional.of(value);
						}
					}
				}
			}
		}
		
		UnitRaytraceContext context = new UnitRaytraceContext(VoxelShapes.empty(), new BlockPos(-100, -100, -100), new Vector3d(-100, -100, -100));
		context.hitFace = hitFace;
		context.posHit = hitPos;
		return context;
	}
	
	public static ArrayList<AxisAlignedBB> shrink(VoxelShape shape, int scale) {
		ArrayList<AxisAlignedBB> newBoundingBoxes = new ArrayList<>();
		
		for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
			axisAlignedBB = new AxisAlignedBB(
					axisAlignedBB.minX / scale, axisAlignedBB.minY / scale, axisAlignedBB.minZ / scale,
					axisAlignedBB.maxX / scale, axisAlignedBB.maxY / scale, axisAlignedBB.maxZ / scale
			);
//			float padding = 0.0025f/scale;
//			axisAlignedBB = axisAlignedBB
//					.expand(padding, padding, padding)
//					.offset(-padding/2, -padding/2, -padding/2);
			newBoundingBoxes.add(axisAlignedBB);
		}
		
		return newBoundingBoxes;
	}
	
	public static UnitRaytraceContext raytraceFluid(UnitTileEntity tileEntity, Entity entity, boolean includeGround, BlockPos pos, Optional<ISelectionContext> contextOptional) {
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
		
		Optional<Direction> hitFace = Optional.empty();
		
		for (SmallUnit unit : tileEntity.getBlockMap().values()) {
//			VoxelShape shape1;
//			if (contextOptional.isPresent())
//				shape1 = unit.state.getShape(tileEntity.world, unit.pos, contextOptional.get());
//			else shape1 = unit.state.getShape(tileEntity.world, unit.pos);
			if (unit.state.getFluidState().isEmpty() || !unit.state.getFluidState().isSource()) continue;
			VoxelShape shape1 = VoxelShapes.create(0, 0, 0, 1, unit.state.getBlockState().getFluidState().getHeight(), 1);
			ArrayList<AxisAlignedBB> aabbs = shrink(shape1, tileEntity.unitsPerBlock);
			
			for (AxisAlignedBB axisAlignedBB : aabbs) {
				axisAlignedBB = axisAlignedBB.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, (unit.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
				axisAlignedBB = axisAlignedBB.offset(pos.getX(), pos.getY(), pos.getZ());
				
				Optional<Vector3d> intercept = axisAlignedBB.rayTrace(start, end);
				if (!intercept.isPresent()) continue;
				
				double dist = intercept.get().distanceTo(start);
				if (dist > bestDist) continue;
				
				bestDist = dist;
				VoxelShape theShape = VoxelShapes.empty();
				
				for (AxisAlignedBB axisAlignedBB1 : aabbs) {
					axisAlignedBB1 = axisAlignedBB1.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, (unit.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
					theShape = VoxelShapes.or(theShape, VoxelShapes.create(axisAlignedBB1));
				}
				
				shape = theShape;
				hitPos = unit.pos;
				hitVec = intercept.get();
				Vector3d vector3d = end.subtract(start);
				hitFace = Optional.of(Direction.getFacingFromVector(vector3d.x, vector3d.y, vector3d.z).getOpposite());
			}
		}
		
		UnitRaytraceContext result1 = raytraceBlockWithoutShape(tileEntity, entity, true, pos, contextOptional);
		
		UnitRaytraceContext context = new UnitRaytraceContext(VoxelShapes.empty(), new BlockPos(-100, -100, -100), new Vector3d(-100, -100, -100));
		context.hitFace = hitFace;
		double distBlocks = 0;
		if (result1.posHit != null)
			distBlocks = Math.sqrt(result1.posHit.distanceSq(start.subtract(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ()).mul(tileEntity.unitsPerBlock, tileEntity.unitsPerBlock, tileEntity.unitsPerBlock).add(0, 64, 0), true)) / tileEntity.unitsPerBlock;
		if (shape == null || hitPos == null || (result1.posHit != null && distBlocks < bestDist))
			return context;
		
		context = new UnitRaytraceContext(shape, hitPos, hitVec);
		context.hitFace = hitFace;
		return context;
	}
}
