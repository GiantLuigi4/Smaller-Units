package com.tfc.smallerunits.block;

import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import com.tfc.smallerunits.utils.ExternalUnitInteractionContext;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.UnitRaytraceContext;
import com.tfc.smallerunits.utils.UnitRaytraceHelper;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.*;

public class SmallerUnitBlock extends Block implements ITileEntityProvider {
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
	
	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		UnitTileEntity te = new UnitTileEntity();
		te.loadingWorld = worldIn;
		return te;
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
					Math.floor(pos.y) + 64,
					Math.floor(pos.z)
			);
			
			raytraceContext.posHit = hitPos;
		}
		
		raytraceContext.posHit = raytraceContext.posHit.offset(hit.getFace());
		
		ItemStack stack = player.getHeldItem(handIn);
		
		tileEntity.world.getBlockState(raytraceContext.posHit.offset(hit.getFace().getOpposite())).onBlockClicked(
				tileEntity.world, raytraceContext.posHit.offset(hit.getFace().getOpposite()), player
		);
		
		BlockRayTraceResult result = new BlockRayTraceResult(
				hit.getHitVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ()).scale(tileEntity.unitsPerBlock).add(0, 64, 0),
				hit.getFace(), raytraceContext.posHit.offset(hit.getFace().getOpposite()), hit.isInside()
		);
		ActionResultType resultType = tileEntity.world.getBlockState(raytraceContext.posHit.offset(hit.getFace().getOpposite())).onBlockActivated(tileEntity.world, player, handIn, result);
		
		if (resultType.isSuccessOrConsume()) {
			tileEntity.markDirty();
			worldIn.markChunkDirty(worldPos, tileEntity);
			
			if (player.openContainer != null)
				ContainerMixinHelper.setNaturallyClosable(player.openContainer, false);
			
			return resultType;
		}
		
		raytraceContext.posHit = raytraceContext.posHit.offset(hit.getFace().getOpposite());
		
		if (stack.getItem() instanceof BlockItem) {
			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			BlockItem item = (BlockItem) stack.getItem();
			
			if (item.getBlock() instanceof SmallerUnitBlock && !stack.hasTag()) return ActionResultType.CONSUME;
			
			BlockPos posOffset = raytraceContext.posHit.offset(hit.getFace());
			
			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
			BlockItemUseContext context = new BlockItemUseContext(tileEntity.world, player, handIn, stack, result);
			if (clicked.isReplaceable(context))
				posOffset = posOffset.offset(hit.getFace().getOpposite());
			BlockState statePlace = item.getBlock().getStateForPlacement(context);
			if (statePlace != null) {
				if (statePlace.isValidPosition(tileEntity.world, posOffset)) {
					tileEntity.world.setBlockState(posOffset, statePlace);
					statePlace.getBlock().onBlockPlacedBy(tileEntity.world, posOffset, statePlace, player, stack);
					
					if (statePlace.getBlock() instanceof ITileEntityProvider) {
						TileEntity te = ((ITileEntityProvider) statePlace.getBlock()).createNewTileEntity(tileEntity.world);
						tileEntity.world.setTileEntity(posOffset, te);
						
						if (stack.hasTag()) {
							CompoundNBT nbt = stack.getOrCreateTag();
							if (nbt.contains("BlockEntityTag")) {
								nbt = nbt.getCompound("BlockEntityTag");
								te.read(statePlace, nbt);
							}
						}
					} else if (statePlace.getBlock().hasTileEntity(statePlace)) {
						TileEntity te = statePlace.getBlock().createTileEntity(statePlace, tileEntity.world);
						tileEntity.world.setTileEntity(posOffset, te);
						
						if (stack.hasTag()) {
							CompoundNBT nbt = stack.getOrCreateTag();
							if (nbt.contains("BlockEntityTag")) {
								nbt = nbt.getCompound("BlockEntityTag");
								te.read(statePlace, nbt);
							}
						}
					}
				}
			}
		} else if (stack.getItem() instanceof BucketItem) {
			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			((BucketItem) stack.getItem()).tryPlaceContainedLiquid(
					player, tileEntity.world, raytraceContext.posHit, result
			);

//			Fluid fluid = ((BucketItem) stack.getItem()).getFluid();
//			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
//			if (clicked.getBlock() instanceof IWaterLoggable) {
//				IWaterLoggable waterLoggableBlock = (IWaterLoggable) clicked.getBlock();
//				if (waterLoggableBlock.canContainFluid(tileEntity.world, raytraceContext.posHit, clicked, fluid)) {
//					waterLoggableBlock.receiveFluid(tileEntity.world, raytraceContext.posHit, clicked, fluid.getDefaultState());
//				}
//			} else {
//				BlockPos posOffset = raytraceContext.posHit.offset(hit.getFace());
//				if (tileEntity.world.getBlockState(posOffset).isAir(tileEntity.world, posOffset)) {
//					tileEntity.world.setBlockState(posOffset, fluid.getDefaultState().getBlockState());
//					tileEntity.world.getPendingFluidTicks().scheduleTick(posOffset, fluid, fluid.getTickRate(tileEntity.world));
//				}
//			}
		} else if (stack.getItem() instanceof BoneMealItem) {
			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
			if (clicked.getBlock() instanceof IGrowable) {
				if (((IGrowable) clicked.getBlock()).canGrow(tileEntity.world, raytraceContext.posHit, clicked, worldIn.isRemote)) {
					if (clicked.getBlock() instanceof BambooBlock) {
						//TODO: fix properly
						try {
							((IGrowable) clicked.getBlock()).grow(tileEntity.world, tileEntity.world.rand, raytraceContext.posHit, clicked);
						} catch (Throwable ignored) {
						}
					} else {
						((IGrowable) clicked.getBlock()).grow(tileEntity.world, tileEntity.world.rand, raytraceContext.posHit, clicked);
					}
				}
			}
		} else {
			BlockPos posOffset = raytraceContext.posHit.offset(hit.getFace().getOpposite());
			if (!(worldIn.isRemote && stack.getItem() instanceof DebugStickItem)) {
				stack.getItem().onItemUse(
						new BlockItemUseContext(
								tileEntity.world, player, handIn, stack,
								new BlockRayTraceResult(
										raytraceContext.vecHit.scale(tileEntity.unitsPerBlock),
										hit.getFace(), posOffset, hit.isInside()
								)
						)
				);
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
		
		return false;
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.HIGH);
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		super.tick(state, worldIn, pos, rand);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.HIGH);
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof UnitTileEntity)) return;
		UnitTileEntity tileEntity1 = (UnitTileEntity) tileEntity;
		if (tileEntity1.world != null) {
			ArrayList<SmallUnit> toMove = new ArrayList<>();
			for (SmallUnit value : tileEntity1.world.blockMap.values()) {
				BlockPos blockPos = value.pos;
				int y = value.pos.getY() - 64;
				if (
						blockPos.getX() < 0 ||
								blockPos.getX() > tileEntity1.unitsPerBlock - 1 ||
								blockPos.getZ() < 0 ||
								blockPos.getZ() > tileEntity1.unitsPerBlock - 1 ||
								y < 0 ||
								y > tileEntity1.unitsPerBlock - 1
				) {
					toMove.add(value);
				}
			}
			for (SmallUnit value : toMove) {
				BlockPos blockPos = value.pos;
				ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(((UnitTileEntity) tileEntity).world, value.pos);
				TileEntity te = context.teInRealWorld;
				if (te instanceof UnitTileEntity) {
					value.pos = context.posInFakeWorld;
					((UnitTileEntity) te).world.blockMap.putIfAbsent(value.pos, value);
					tileEntity1.world.blockMap.remove(blockPos);
					
					tileEntity.markDirty();
					te.markDirty();
					worldIn.notifyBlockUpdate(tileEntity.getPos(), state, state, 3);
					worldIn.notifyBlockUpdate(te.getPos(), state, state, 3);
				}
			}
			long start = new Date().getTime();
			tileEntity1.world.tick(() -> Math.abs(new Date().getTime() - start) <= 10);
		}
	}
	
	@Override
	public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape shape;
		
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		
		if (context.getEntity() == null || !(tileEntityUncasted instanceof UnitTileEntity))
			return super.getShape(state, reader, pos, context);
		
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
		nbt.remove("ticks");
		
		if (!shapeMap.containsKey(nbt)) {
			VoxelShape shape = VoxelShapes.empty();
			
			for (SmallUnit value : tileEntity.world.blockMap.values()) {
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.world, value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					shape2 = VoxelShapes.or(shape2, VoxelShapes.create(axisAlignedBB));
				}
				shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
				shape = VoxelShapes.or(shape, shape2);
			}
			
			if (shape.isEmpty()) return shape;
			
			shapeMap.put(nbt, shape);
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
