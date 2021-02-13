package tfc.smallerunits.block;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
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
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
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
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, worldPos, Optional.empty());
		
		if (raytraceContext.shapeHit.isEmpty()) {
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
			
			raytraceContext.posHit = hitPos;
		}
		
		ItemStack stack = player.getHeldItem(handIn);
		
		tileEntity.world.getBlockState(raytraceContext.posHit.offset(hit.getFace().getOpposite())).onBlockClicked(
				tileEntity.world, raytraceContext.posHit.offset(hit.getFace().getOpposite()), player
		);
		
		ActionResultType resultType = tileEntity.world.getBlockState(raytraceContext.posHit.offset(hit.getFace().getOpposite())).onBlockActivated(tileEntity.world, player, handIn,
				new BlockRayTraceResult(
						hit.getHitVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ()).scale(tileEntity.unitsPerBlock),
						hit.getFace(), raytraceContext.posHit.offset(hit.getFace().getOpposite()), hit.isInside()
				)
		);
		
		if (resultType.isSuccessOrConsume()) {
			tileEntity.markDirty();
			worldIn.markChunkDirty(worldPos, tileEntity);
			
			return resultType;
		}
		
		if (stack.getItem() instanceof BlockItem) {
			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			BlockItem item = (BlockItem) stack.getItem();
			BlockPos posOffset = raytraceContext.posHit.offset(hit.getFace());
			BlockState statePlace = item.getBlock().getStateForPlacement(
					new BlockItemUseContext(
							tileEntity.world,
							player, handIn,
									stack,
									new BlockRayTraceResult(
											raytraceContext.vecHit.scale(tileEntity.unitsPerBlock),
											hit.getFace(), posOffset, hit.isInside()
									)
					));
			if (statePlace != null) {
				BlockState oldState = tileEntity.world.getBlockState(posOffset);
				tileEntity.world.setBlockState(posOffset,
						statePlace
				);
				statePlace.onBlockAdded(tileEntity.world, posOffset, oldState, false);
				for (Direction value : Direction.values()) {
					if (value.getAxis().isHorizontal()) {
						tileEntity.world.getBlockState(posOffset.offset(value))
								.updatePostPlacement(
										hit.getFace(),
										statePlace, tileEntity.world,
										posOffset.offset(value),
										new BlockPos(0, 0, 0).offset(value)
								);
					}
				}
			}
			
			tileEntity.markDirty();
			worldIn.notifyBlockUpdate(worldPos, state, state, 3);
		} else if (stack.getItem() instanceof BucketItem) {
			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			Fluid fluid = ((BucketItem) stack.getItem()).getFluid();
			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
			if (clicked.getBlock() instanceof IWaterLoggable) {
				IWaterLoggable waterLoggableBlock = (IWaterLoggable)clicked.getBlock();
				if (waterLoggableBlock.canContainFluid(tileEntity.world,raytraceContext.posHit,clicked,fluid)) {
					waterLoggableBlock.receiveFluid(tileEntity.world,raytraceContext.posHit,clicked,fluid.getDefaultState());
					
					tileEntity.markDirty();
					worldIn.notifyBlockUpdate(worldPos, state, state, 3);
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean removedByPlayer(BlockState state, World worldIn, BlockPos worldPos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		if (worldIn.isRemote)
			return false;
		
		TileEntity tileEntityUncasted = worldIn.getTileEntity(worldPos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return true;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, worldPos, Optional.empty());
		
		BlockPos hitPos;
		if (raytraceContext.shapeHit.isEmpty()) return false;
		else hitPos = raytraceContext.posHit;
		
		tileEntity.world.removeBlock(hitPos, false);
		
		tileEntity.markDirty();
		worldIn.notifyBlockUpdate(worldPos, state, state, 3);
		
		return false;
	}
	
	@Override
	public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape shape;
		
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		
		if (context.getEntity() == null || !(tileEntityUncasted instanceof UnitTileEntity)) return super.getShape(state,reader,pos,context);
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		shape = UnitRaytraceHelper.raytraceBlock(tileEntity,context.getEntity(),true,pos,Optional.of(context)).shapeHit;
		
		if (!shape.isEmpty()) return shape;
		
		Vector3d start = context.getEntity().getEyePosition(0);
		double reach = 8;
		
		if (context.getEntity() instanceof PlayerEntity)
			reach = ((LivingEntity) context.getEntity()).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d look = context.getEntity().getLookVec().scale(reach);
		Vector3d end = context.getEntity().getEyePosition(0).add(look);
		
		for (Direction dir : Direction.values()) {
			BlockPos pos1 = pos.offset(dir);
			BlockState state1 = reader.getBlockState(pos1);
			
			if (state1.getBlock() instanceof SmallerUnitBlock) break;
			
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
	
	public static final VoxelShape virtuallyEmptyShape = VoxelShapes.create(0, 0, 0, 0.001f, 0.001f, 0.001f);
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return getCollisionShape(state,worldIn,pos);
	}
	
	public static final HashMap<CompoundNBT, VoxelShape> shapeMap = new HashMap<>();
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity)) return virtuallyEmptyShape;
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		CompoundNBT nbt = tileEntity.serializeNBT();
		
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");
		nbt.remove("id");
		nbt.remove("ForgeData");
		nbt.remove("ForgeCaps");
		
		if (!shapeMap.containsKey(nbt)) {
			VoxelShape shape = VoxelShapes.empty();
			
			for (SmallUnit value : tileEntity.world.blockMap.values()) {
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.world, value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					shape2 = VoxelShapes.or(shape2, VoxelShapes.create(axisAlignedBB));
				}
				shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, value.pos.getY() / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
				shape = VoxelShapes.or(shape, shape2);
			}
			
			if (shape.isEmpty()) return virtuallyEmptyShape;
			
			shapeMap.put(nbt,shape);
			return shape;
		} else {
			return shapeMap.get(nbt);
		}
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
