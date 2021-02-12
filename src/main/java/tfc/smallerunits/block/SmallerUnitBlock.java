package tfc.smallerunits.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import tfc.smallerunits.utils.Unit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class SmallerUnitBlock extends Block {
	public SmallerUnitBlock() {
		super(Properties.from(Blocks.STONE).setOpaque((a, b, c) -> false).notSolid());
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new UnitTileEntity();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//		if (!(worldIn.getTileEntity(pos) instanceof UnitTileEntity))
//			return super.getShape(state, worldIn, pos, context);
//
//		UnitTileEntity tileEntity = (UnitTileEntity) worldIn.getTileEntity(pos);
//
//		if (tileEntity == null) return super.getShape(state, worldIn, pos, context);
//
//		final VoxelShape[] shape = {VoxelShapes.empty()};
//		tileEntity.world.blockMap.forEach((pos1, unit) -> {
//			VoxelShape shape1 = unit.state.getShape(tileEntity.world, pos1);
//			if (!shape1.isEmpty() && !unit.state.isAir()) {
//				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
//					axisAlignedBB = axisAlignedBB.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, unit.pos.getY() / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
//					shape[0] = VoxelShapes.or(shape[0], VoxelShapes.create(axisAlignedBB));
//				}
//			}
//		});
//
//		if (shape[0].isEmpty()) return super.getShape(state, worldIn, pos, context);
//
//		return shape[0];
		return getRayTraceShape(state,worldIn,pos,context);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos worldPos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		TileEntity tileEntityUncasted = worldIn.getTileEntity(worldPos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return super.onBlockActivated(state, worldIn, worldPos, player, handIn, hit);
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		Vector3d pos;
		{
			RayTraceResult result = hit;
			Vector3d pos1 = result.getHitVec().subtract(new Vector3d(worldPos.getX(), worldPos.getY(), worldPos.getZ())).scale(tileEntity.unitsPerBlock);
			pos = new Vector3d(pos1.x, pos1.y, pos1.z);
			pos = invRound(pos);
		}
		
		BlockPos hitPos = new BlockPos(
				Math.floor(pos.x),
				Math.floor(pos.y),
				Math.floor(pos.z)
		);
		if (
				(
						hit.getFace() == Direction.DOWN ||
								hit.getFace() == Direction.NORTH ||
								hit.getFace() == Direction.WEST ||
								pos.x < 0 ||
								pos.y < 0 ||
								pos.z < 0
				) && (
						Math.abs(pos.x) % 1 <= 0.001 ||
								Math.abs(pos.y) % 1 <= 0.001 ||
								Math.abs(pos.z) % 1 <= 0.001 ||
								Math.abs(pos.x) % 1 >= 0.999 ||
								Math.abs(pos.y) % 1 >= 0.999 ||
								Math.abs(pos.z) % 1 >= 0.999
						)
		) {
			hitPos = hitPos.offset(hit.getFace());
		}
		
		System.out.println(pos.getY());
		
		ItemStack stack = player.getHeldItem(handIn);
		
		ActionResultType resultType = tileEntity.world.getBlockState(hitPos.offset(hit.getFace().getOpposite())).onBlockActivated(tileEntity.world,player,handIn,
				new BlockRayTraceResult(
						hit.getHitVec().subtract(worldPos.getX(),worldPos.getY(),worldPos.getZ()).scale(tileEntity.unitsPerBlock),
						hit.getFace(), hitPos.offset(hit.getFace().getOpposite()), hit.isInside()
				)
		);

		if (resultType.isSuccessOrConsume()) return resultType;
		
		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			tileEntity.world.setBlockState(hitPos,
					item.getBlock().getStateForPlacement(
							new BlockItemUseContext(
									new ItemUseContext(
											player, handIn,
											new BlockRayTraceResult(
													hit.getHitVec().subtract(worldPos.getX(),worldPos.getY(),worldPos.getZ()).scale(tileEntity.unitsPerBlock),
													hit.getFace(), hitPos, hit.isInside()
											)
									)
							)));
			tileEntity.markDirty();
		}
		
		return ActionResultType.SUCCESS;
//		return super.onBlockActivated(state, worldIn, worldPos, player, handIn, hit);
	}
	
	@Override
	public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape shape = VoxelShapes.empty();
		
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		
		if (context.getEntity() == null || !(tileEntityUncasted instanceof UnitTileEntity)) return super.getShape(state,reader,pos,context);
		
		Vector3d start = context.getEntity().getEyePosition(0);
		double reach = 8;
		
		if (context.getEntity() instanceof PlayerEntity)
			reach = ((LivingEntity) context.getEntity()).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d look = context.getEntity().getLookVec().scale(reach);
		Vector3d end = context.getEntity().getEyePosition(0).add(look);
		
		double bestDist = Double.POSITIVE_INFINITY;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		for (Unit unit : tileEntity.world.blockMap.values()) {
			VoxelShape shape1 = unit.state.getShape(tileEntity.world, unit.pos, context);
			ArrayList<AxisAlignedBB> aabbs = shrink(shape1, tileEntity.unitsPerBlock);
			
			for (AxisAlignedBB axisAlignedBB : aabbs) {
				axisAlignedBB = axisAlignedBB.offset(unit.pos.getX() / (float) tileEntity.unitsPerBlock, unit.pos.getY() / (float) tileEntity.unitsPerBlock, unit.pos.getZ() / (float) tileEntity.unitsPerBlock);
				axisAlignedBB = axisAlignedBB.offset(pos.getX(),pos.getY(),pos.getZ());
				
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
			}
		}
		
		if (!shape.isEmpty()) return shape;
		
		for (Direction dir : Direction.values()) {
			BlockPos pos1 = pos.offset(dir);
			BlockState state1 = reader.getBlockState(pos1);
			VoxelShape raytraceShape = state1.getRaytraceShape(reader, pos, context);
			boolean isHit = false;
			
			Vector3d hit = null;
			for (AxisAlignedBB axisAlignedBB : raytraceShape.toBoundingBoxList()) {
				axisAlignedBB = axisAlignedBB.offset(pos1.getX(), pos1.getY(), pos1.getZ());
				
				Optional<Vector3d> intercept = axisAlignedBB.rayTrace(start, end);
				if (!intercept.isPresent()) continue;
				isHit = true;
				hit = intercept.get();
			}
			
			if (isHit) {
				VoxelShape shape1 = raytraceShape.withOffset(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
				BlockPos pos2 = getHit(new BlockRayTraceResult(hit,context.getEntity().getHorizontalFacing(),pos1,false), pos1, tileEntity.unitsPerBlock);
				pos2 = pos2.offset(dir.getOpposite());
				
				if (dir == Direction.DOWN) pos2 = new BlockPos(pos2.getX(),-1,pos2.getZ());
				else if (dir == Direction.UP) pos2 = new BlockPos(pos2.getX(),tileEntity.unitsPerBlock,pos2.getZ());
				else if (dir == Direction.WEST) pos2 = new BlockPos(-1,pos2.getY(),pos2.getZ());
				else if (dir == Direction.EAST) pos2 = new BlockPos(tileEntity.unitsPerBlock,pos2.getY(),pos2.getZ());
				else if (dir == Direction.NORTH) pos2 = new BlockPos(pos2.getX(),pos2.getY(),-1);
				else if (dir == Direction.SOUTH) pos2 = new BlockPos(pos2.getX(),pos2.getY(),tileEntity.unitsPerBlock);
				
				return VoxelShapes.combine(shape1,
						VoxelShapes.create(
								new AxisAlignedBB(
										pos2.getX() / (float) tileEntity.unitsPerBlock, pos2.getY() / (float) tileEntity.unitsPerBlock, pos2.getZ() / (float) tileEntity.unitsPerBlock,
										pos2.getX() / (float) tileEntity.unitsPerBlock + 1f / tileEntity.unitsPerBlock, pos2.getY() / (float) tileEntity.unitsPerBlock + 1f / tileEntity.unitsPerBlock, pos2.getZ() / (float) tileEntity.unitsPerBlock + 1f / tileEntity.unitsPerBlock
								)), IBooleanFunction.AND);
			}
		}
		
		if (shape.isEmpty()) return super.getRayTraceShape(state, reader, pos, context);
		
		return shape;
	}
	
	public BlockPos getHit(RayTraceResult result, BlockPos worldPos, int scale) {
		Vector3d pos;
		{
			Vector3d pos1 = result.getHitVec().subtract(new Vector3d(worldPos.getX(), worldPos.getY(), worldPos.getZ())).scale(scale);
			pos = new Vector3d(pos1.x, pos1.y, pos1.z);
			pos = invRound(pos);
		}
		
		return new BlockPos(
				Math.floor(pos.x),
				Math.floor(pos.y),
				Math.floor(pos.z)
		);
	}
	
	//idk why, but this makes selection work
	public Vector3d invRound(Vector3d pos) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		
		if (pos.x <= 0.5) x = Math.ceil(pos.x) - 1;
		else x = Math.floor(pos.x);

		if (pos.y <= 0.5) y = Math.ceil(pos.y) - 1;
		else y = Math.floor(pos.y);

		if (pos.z <= 0.5) z = Math.ceil(pos.z) - 1;
		else z = Math.floor(pos.z);
		
		return new Vector3d(Math.round(x), Math.round(y), Math.round(z));
	}
	
	public ArrayList<AxisAlignedBB> shrink(VoxelShape shape, int scale) {
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
