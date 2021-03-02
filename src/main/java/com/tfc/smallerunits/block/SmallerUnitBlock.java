package com.tfc.smallerunits.block;

import com.google.common.collect.ImmutableSet;
import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.*;
import com.tfc.smallerunits.utils.world.FakeServerWorld;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import java.util.*;

public class SmallerUnitBlock extends Block implements ITileEntityProvider {
	public SmallerUnitBlock() {
		super(Properties.from(Blocks.STONE).setOpaque((a, b, c) -> false).notSolid().hardnessAndResistance(1, 1));
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
	
	public static final HashMap<CompoundNBT, VoxelShape> shapeMap = new HashMap<>();
	
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
//		VoxelShape renderShape = VoxelShapes.empty();
//		for (AxisAlignedBB axisAlignedBB : getRenderShape(state, worldIn, pos).toBoundingBoxList()) {
//			float padding = 0.005f;
//			axisAlignedBB = axisAlignedBB.shrink(padding).offset(padding/2, padding/2, padding/2);
//			renderShape = VoxelShapes.combine(renderShape, VoxelShapes.create(axisAlignedBB), IBooleanFunction.OR);
//		}
//		return VoxelShapes.or(renderShape,getRayTraceShape(state, worldIn, pos, context));
//		return getRayTraceShape(state, worldIn, pos, context);
		return getRayTraceShape(state, worldIn, pos, context);
//		return super.getShape(state,worldIn,pos,context);
	}
	
	@Override
	public Block getBlock() {
		return this;
	}
	
	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		if (FMLEnvironment.dist.isClient() && ((World) worldIn).isRemote) {
			ISelectionContext context = new ISelectionContext() {
				@Override
				public boolean getPosY() {
					return false;
				}
				
				@Override
				public boolean func_216378_a(VoxelShape shape, BlockPos pos, boolean p_216378_3_) {
					return false;
				}
				
				@Override
				public boolean hasItem(Item itemIn) {
					return ((PlayerEntity) getEntity()).inventory.hasAny(ImmutableSet.of(itemIn));
				}
				
				@Override
				public boolean func_230426_a_(FluidState p_230426_1_, FlowingFluid p_230426_2_) {
					return false;
				}
				
				@Nullable
				@Override
				public Entity getEntity() {
					return Minecraft.getInstance().player;
				}
			};
			return getRayTraceShape(state, worldIn, pos, context);
		}
		return getRenderShape(state, worldIn, pos);
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
		
		raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()));
		
		ItemStack stack = player.getHeldItem(handIn);
		
		BlockRayTraceResult result = new BlockRayTraceResult(
				hit.getHitVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ()).scale(tileEntity.unitsPerBlock).add(0, 64, 0),
				raytraceContext.hitFace.orElse(hit.getFace()), raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite()), hit.isInside()
		);
		boolean playerIsSleeping = player.isSleeping();
		Vector3d playerPos = player.getPositionVec();
		ActionResultType resultType = ActionResultType.FAIL;
		World currentWorld = player.world;
		player.setWorld(tileEntity.world);
		tileEntity.world.result = new BlockRayTraceResult(
				raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).subtract(raytraceContext.posHit.getX(), raytraceContext.posHit.getY(), raytraceContext.posHit.getZ()).scale(tileEntity.unitsPerBlock),
				raytraceContext.hitFace.orElse(Direction.UP),
				raytraceContext.posHit, true
		);
		{
			Vector3d miniWorldPos = player.getPositionVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ());
			player.setRawPosition(miniWorldPos.getX(), miniWorldPos.getY(), miniWorldPos.getZ());
		}
		try {
			if (!(player.isSneaking()) || stack.doesSneakBypassUse(worldIn, raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite()), player)) {
				resultType = tileEntity.world.getBlockState(raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite())).onBlockActivated(tileEntity.world, player, handIn, result);
			}
		} catch (Throwable ignored) {
		}
		player.setWorld(currentWorld);
		if (!playerIsSleeping && player.isSleeping()) {
			player.stopSleepInBed(true, true);
			if (player instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) player).connection.setPlayerLocation(playerPos.getX(), playerPos.getY(), playerPos.getZ(), player.rotationYaw, player.rotationPitch);
			}
			player.sendStatusMessage(new StringTextComponent("Sorry, but you can't sleep in tiny beds."), true);
		}
		player.setRawPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
		player.recenterBoundingBox();
		
		if (resultType.isSuccessOrConsume()) {
			tileEntity.markDirty();
			worldIn.markChunkDirty(worldPos, tileEntity);
			
			if (player.openContainer != null)
				ContainerMixinHelper.setNaturallyClosable(player.openContainer, false);
			
			return resultType;
		}
		
		raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite());
		
		if (stack.getItem() instanceof BlockItem) {
			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			BlockItem item = (BlockItem) stack.getItem();
			
			if (item.getBlock() instanceof SmallerUnitBlock && stack.getOrCreateTag().getCompound("BlockEntityTag").contains("ContainedUnits"))
				return ActionResultType.CONSUME;
//			if (item.getBlock() instanceof SmallerUnitBlock) return ActionResultType.CONSUME;
			
			BlockPos posOffset = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()));
			
			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
			BlockItemUseContext context = new BlockItemUseContext(tileEntity.world, player, handIn, stack, result);
			if (true) {
				ActionResultType type = item.tryPlace(context);
				if (type.isSuccessOrConsume()) {
					BlockState statePlace = item.getBlock().getStateForPlacement(context);
					
					SoundType type1 = item.getBlock().getSoundType(statePlace);
					SoundEvent event = type1.getPlaceSound();
					tileEntity.world.playSound(
							null,
							posOffset.getX() + 0.5, posOffset.getY() + 0.5, posOffset.getZ() + 0.5,
							event, SoundCategory.BLOCKS, type1.getVolume(), type1.getPitch() - 0.25f
					);
					ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(tileEntity.world.dimension, tileEntity.world, posOffset), raytraceContext.hitFace.orElse(hit.getFace()));
					return type;
				}
			}
			
			if (clicked.isReplaceable(context))
				posOffset = posOffset.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite());
			BlockState statePlace = item.getBlock().getStateForPlacement(context);
			if (statePlace != null) {
				if (tileEntity.world.getBlockState(posOffset).isReplaceable(context) || clicked.isAir()) {
					if (statePlace.isValidPosition(tileEntity.world, posOffset)) {
						if (!player.isCreative()) stack.shrink(1);
						
						tileEntity.world.setBlockState(posOffset, statePlace);
						statePlace.getBlock().onBlockPlacedBy(tileEntity.world, posOffset, statePlace, player, stack);
						
						if (!(stack.getItem() instanceof SkullItem)) {
							if (statePlace.getBlock() instanceof ITileEntityProvider) {
								TileEntity te = ((ITileEntityProvider) statePlace.getBlock()).createNewTileEntity(tileEntity.world);
								
								if (stack.hasTag()) {
									CompoundNBT nbt = stack.getOrCreateTag();
									
									if (nbt.contains("BlockEntityTag")) {
										nbt = nbt.getCompound("BlockEntityTag");
										te.read(statePlace, nbt);
									}
								}
								
								tileEntity.world.setTileEntity(posOffset, te);
							} else if (statePlace.getBlock().hasTileEntity(statePlace)) {
								TileEntity te = statePlace.getBlock().createTileEntity(statePlace, tileEntity.world);
								
								if (stack.hasTag()) {
									CompoundNBT nbt = stack.getOrCreateTag();
									
									if (nbt.contains("BlockEntityTag")) {
										nbt = nbt.getCompound("BlockEntityTag");
										te.read(statePlace, nbt);
									}
								}
								
								tileEntity.world.setTileEntity(posOffset, te);
							}
						}
					}
				}
			}
		} else if (stack.getItem() instanceof BucketItem) {
			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			UnitRaytraceContext context = UnitRaytraceHelper.raytraceFluid(tileEntity, player, false, worldPos, Optional.empty());
			if (!context.shapeHit.isEmpty() && ((BucketItem) stack.getItem()).getFluid() == Fluids.EMPTY) {
				FluidState state1 = tileEntity.world.getFluidState(context.posHit);
				SoundEvent soundevent = state1.getFluid().getAttributes().getFillSound();
				if (state1.getFluid() != Fluids.EMPTY) {
					if (soundevent == null)
						soundevent = state1.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
//				worldIn.playSound(
					player.playSound(
//						null,
//						worldPos.getX() + ((context.posHit.getX()) / tileEntity.unitsPerBlock),
//						worldPos.getY() + ((context.posHit.getY() - 64) / tileEntity.unitsPerBlock),
//						worldPos.getZ() + ((context.posHit.getZ()) / tileEntity.unitsPerBlock),
							soundevent, /*SoundCategory.PLAYERS, */1, 1
					);
					if (tileEntity.world.getBlockState(context.posHit).getBlock() instanceof IWaterLoggable) {
						((IWaterLoggable) tileEntity.world.getBlockState(context.posHit).getBlock()).pickupFluid(tileEntity.world, context.posHit, tileEntity.world.getBlockState(context.posHit));
					} else {
						tileEntity.world.setBlockState(context.posHit, Blocks.AIR.getDefaultState());
					}
					stack.shrink(1);
					if (stack.getCount() == 0)
						player.setHeldItem(handIn, new ItemStack(state1.getFluid().getFilledBucket()));
					else if (!player.addItemStackToInventory(new ItemStack(state1.getFluid().getFilledBucket())))
						player.dropItem(new ItemStack(state1.getFluid().getFilledBucket()), true);
				}
			} else {
				Fluid fluid = ((BucketItem) stack.getItem()).getFluid();
				SoundEvent soundevent = fluid.getAttributes().getEmptySound();
				if (soundevent == null)
					soundevent = fluid.isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
//				worldIn.playSound(
				player.playSound(
//						null,
//						worldPos.getX() + ((context.posHit.getX()) / tileEntity.unitsPerBlock),
//						worldPos.getY() + ((context.posHit.getY() - 64) / tileEntity.unitsPerBlock),
//						worldPos.getZ() + ((context.posHit.getZ()) / tileEntity.unitsPerBlock),
						soundevent, /*SoundCategory.PLAYERS, */1, 1
				);
				((BucketItem) stack.getItem()).tryPlaceContainedLiquid(
						player, tileEntity.world, raytraceContext.posHit, result
				);
				player.setHeldItem(handIn, ((BucketItem) stack.getItem()).emptyBucket(stack, player));
			}

//			Fluid fluid = ((BucketItem) stack.getItem()).getFluid();
//			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
//			if (clicked.getBlock() instanceof IWaterLoggable) {
//				IWaterLoggable waterLoggableBlock = (IWaterLoggable) clicked.getBlock();
//				if (waterLoggableBlock.canContainFluid(tileEntity.world, raytraceContext.posHit, clicked, fluid)) {
//					waterLoggableBlock.receiveFluid(tileEntity.world, raytraceContext.posHit, clicked, fluid.getDefaultState());
//				}
//			} else {
//				BlockPos posOffset = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()));
//				if (tileEntity.world.getBlockState(posOffset).isAir(tileEntity.world, posOffset)) {
//					tileEntity.world.setBlockState(posOffset, fluid.getDefaultState().getBlockState());
//					tileEntity.world.getPendingFluidTicks().scheduleTick(posOffset, fluid, fluid.getTickRate(tileEntity.world));
//				}
//			}
		} else if (stack.getItem() instanceof BoneMealItem) {
			BlockState clicked = tileEntity.world.getBlockState(raytraceContext.posHit);
			if (clicked.getBlock() instanceof IGrowable) {
				if (((IGrowable) clicked.getBlock()).canGrow(tileEntity.world, raytraceContext.posHit, clicked, worldIn.isRemote)) {
					((IGrowable) clicked.getBlock()).grow(tileEntity.world, tileEntity.world.rand, raytraceContext.posHit, clicked);
				}
			}
		} else {
			BlockPos posOffset = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite());
			if (!(worldIn.isRemote && stack.getItem() instanceof DebugStickItem)) {
				stack.getItem().onItemUse(
						new BlockItemUseContext(
								tileEntity.world, player, handIn, stack,
								new BlockRayTraceResult(
										raytraceContext.vecHit.scale(tileEntity.unitsPerBlock),
										raytraceContext.hitFace.orElse(hit.getFace()), posOffset, hit.isInside()
								)
						)
				);
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
		TileEntity tileEntityUncasted = world.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return 0;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		return tileEntity.world.blockMap.isEmpty() ? 0 : -1;
	}
	
	@Override
	public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
	
	}
	
	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof UnitTileEntity)) return;
		UnitTileEntity tileEntity = (UnitTileEntity) te;
		ArrayList<SmallUnit> toRemove = new ArrayList<>();
		for (SmallUnit value : tileEntity.world.blockMap.values()) {
			Vector3d pos1 = new Vector3d(
					value.pos.getX() / (float) tileEntity.unitsPerBlock,
					(value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock,
					value.pos.getZ() / (float) tileEntity.unitsPerBlock
			);
			pos1 = pos1.add(pos.getX(), pos.getY(), pos.getZ());
			if (pos1.distanceTo(explosion.getPosition()) < (explosion.size)) {
				toRemove.add(value);
			}
		}
		for (SmallUnit blockPos : toRemove) {
			if (blockPos.state.getExplosionResistance(((UnitTileEntity) te).world, blockPos.pos, explosion) < 1200.0F) {
				BlockState state1 = blockPos.state;
				
				List<ItemStack> stacks = state1.getDrops(
						new LootContext.Builder(tileEntity.world)
								.withLuck(0)
								.withRandom(tileEntity.world.rand)
								.withSeed(tileEntity.world.rand.nextLong())
								.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
								.withParameter(LootParameters.field_237457_g_, new Vector3d(blockPos.pos.getX() + 0.5, blockPos.pos.getY() + 0.5, blockPos.pos.getZ() + 0.5))
								.withNullableParameter(LootParameters.BLOCK_ENTITY, tileEntity.world.getTileEntity(blockPos.pos))
								.withParameter(LootParameters.BLOCK_STATE, state1)
								.withNullableParameter(LootParameters.THIS_ENTITY, explosion.getExploder())
				);
				
				for (ItemStack stack : stacks) {
					ItemEntity entity = new ItemEntity(tileEntity.world, blockPos.pos.getX(), blockPos.pos.getY(), blockPos.pos.getZ(), stack);
					tileEntity.world.addEntity(entity);
				}
				
				blockPos.state.onBlockExploded(tileEntity.world, blockPos.pos, explosion);
			}
		}
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
		TileEntity tileEntityUncasted = worldIn.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return 1;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		if (tileEntity.world.blockMap.isEmpty()) return 1;
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, pos, Optional.empty());
		
		BlockPos hitPos;
		if (worldIn instanceof FakeServerWorld) {
			hitPos = new BlockPos(((FakeServerWorld) worldIn).owner.world.result.getHitVec());
		} else if (raytraceContext.shapeHit.isEmpty()) {
			(player).resetActiveHand();
			return 0;
		} else hitPos = raytraceContext.posHit;
		
		BlockState state1 = tileEntity.world.getBlockState(hitPos);
		
		float amt = state1.getPlayerRelativeBlockHardness(player, tileEntity.world, hitPos) * tileEntity.unitsPerBlock;
		return amt;
	}
	
	@Override
	public boolean removedByPlayer(BlockState state, World worldIn, BlockPos worldPos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		if (worldIn.isRemote)
			return false;
		
		TileEntity tileEntityUncasted = worldIn.getTileEntity(worldPos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return true;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		if (tileEntity.world.blockMap.isEmpty()) {
			ItemStack stack = new ItemStack(Deferred.UNITITEM.get());
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("upb", tileEntity.unitsPerBlock);
			stack.getOrCreateTag().put("BlockEntityTag", nbt);
			player.addItemStackToInventory(stack);
			worldIn.setBlockState(worldPos, Blocks.AIR.getDefaultState());
			return true;
		}
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, worldPos, Optional.empty());
		
		BlockPos hitPos;
		if (raytraceContext.shapeHit.isEmpty()) return false;
		else hitPos = raytraceContext.posHit;
		
		tileEntity.world.result = new BlockRayTraceResult(
				raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ()).scale(tileEntity.unitsPerBlock),
				raytraceContext.hitFace.orElse(Direction.UP),
				hitPos, true
		);
		
		BlockState state1 = tileEntity.world.getBlockState(hitPos);
		
		if (state1.removedByPlayer(tileEntity.world, hitPos, player, true, state1.getFluidState())) {
			List<ItemStack> stacks = state1.getDrops(
					new LootContext.Builder(tileEntity.world)
							.withLuck(player.getLuck())
							.withRandom(tileEntity.world.rand)
							.withSeed(tileEntity.world.rand.nextLong())
							.withParameter(LootParameters.TOOL, player.getHeldItem(Hand.MAIN_HAND))
							.withParameter(LootParameters.field_237457_g_, raytraceContext.vecHit)
							.withNullableParameter(LootParameters.BLOCK_ENTITY, tileEntity.world.getTileEntity(raytraceContext.posHit))
							.withParameter(LootParameters.BLOCK_STATE, state1)
							.withParameter(LootParameters.THIS_ENTITY, player)
			);
			
			for (ItemStack stack : stacks) {
				ItemEntity entity = new ItemEntity(tileEntity.world, hitPos.getX(), hitPos.getY(), hitPos.getZ(), stack);
				tileEntity.world.addEntity(entity);
			}
			
			state1.onReplaced(tileEntity.world, raytraceContext.posHit, Blocks.AIR.getDefaultState(), false);
			tileEntity.world.removeBlock(hitPos, false);
		}
		
		return false;
	}
	
	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getCollisionShape(state, worldIn, pos);
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.HIGH);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addHitEffects(BlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
		if (!(target instanceof BlockRayTraceResult)) return false;
		BlockPos worldPos = ((BlockRayTraceResult) target).getPos();
		PlayerEntity playerEntity = Minecraft.getInstance().player;
		TileEntity te = worldObj.getTileEntity(worldPos);
		if (!(te instanceof UnitTileEntity)) return false;
		UnitTileEntity tileEntity = (UnitTileEntity) te;
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, playerEntity, false, worldPos, Optional.empty());
		
		Direction dir = ((BlockRayTraceResult) target).getFace();
		BlockPos hitPos = raytraceContext.posHit;
		
		if (worldObj.isRemote) {
			double x = worldObj.rand.nextFloat();
			double y = worldObj.rand.nextFloat();
			double z = worldObj.rand.nextFloat();
			
			x = dir.getXOffset() == 0 ? x : Math.max(dir.getXOffset(), 0);
			y = dir.getYOffset() == 0 ? y : Math.max(dir.getYOffset(), 0);
			z = dir.getZOffset() == 0 ? z : Math.max(dir.getZOffset(), 0);
			
			float scl = ((1f / tileEntity.unitsPerBlock));
			Particle particle = ParticleHelper.create(worldObj, x, y, z, hitPos, tileEntity, worldPos, scl);
			
			manager.addEffect(particle);
		}
		return true;
	}
	
	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		if (player.isSneaking()) {
			return super.getPickBlock(state, target, world, pos, player);
		}
		
		TileEntity tileEntityUncasted = world.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return super.getPickBlock(state, target, world, pos, player);
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, pos, Optional.empty());
		
		BlockPos hitPos;
		if (raytraceContext.shapeHit.isEmpty()) return super.getPickBlock(state, target, world, pos, player);
		else hitPos = raytraceContext.posHit;
		
		BlockState state1 = tileEntity.world.getBlockState(hitPos);
		if (state1.getBlock() instanceof SmallerUnitBlock) {
			return super.getPickBlock(state, target, world, pos, player);
		}
		
		if (target instanceof BlockRayTraceResult) {
			return state1.getPickBlock(new BlockRayTraceResult(raytraceContext.vecHit, ((BlockRayTraceResult) target).getFace(), raytraceContext.posHit, ((BlockRayTraceResult) target).isInside()), world, pos, player);
		} else {
			return state1.getPickBlock(new BlockRayTraceResult(raytraceContext.vecHit, Direction.UP, raytraceContext.posHit, false), world, pos, player);
		}
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		super.tick(state, worldIn, pos, rand);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.HIGH);
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof UnitTileEntity)) return;
		UnitTileEntity tileEntity1 = (UnitTileEntity) tileEntity;
		if (tileEntity1.world != null) {
			ArrayList<SmallUnit> toRemove = new ArrayList<>();
			ArrayList<SmallUnit> toMove = new ArrayList<>();
			for (SmallUnit value : tileEntity1.world.blockMap.values()) {
				BlockPos blockPos = value.pos;
				if (value.pos == null) {
					toRemove.add(value);
					continue;
				}
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
			for (SmallUnit smallUnit : toRemove) {
				tileEntity1.world.blockMap.remove(smallUnit.pos);
			}
			for (SmallUnit value : toMove) {
				BlockPos blockPos = value.pos;
				ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(((UnitTileEntity) tileEntity).world, value.pos);
				if (context.teInRealWorld instanceof UnitTileEntity) {
					if (((UnitTileEntity) context.teInRealWorld).world.blockMap.isEmpty()) {
						((UnitTileEntity) context.teInRealWorld).unitsPerBlock = tileEntity1.unitsPerBlock;
					}
				}
				if (context.stateInRealWorld.isAir(worldIn, context.posInRealWorld)) {
					UnitTileEntity tileEntity2 = new UnitTileEntity();
					worldIn.setBlockState(context.posInRealWorld, Deferred.UNIT.get().getDefaultState());
					worldIn.setTileEntity(context.posInRealWorld, tileEntity2);
					tileEntity2.isNatural = true;
					continue;
				}
				TileEntity te = context.teInRealWorld;
				if (te instanceof UnitTileEntity) {
					value.pos = context.posInFakeWorld;
					((UnitTileEntity) te).world.setBlockState(value.pos, value.state, 3, 0);
					((UnitTileEntity) te).world.setTileEntity(value.pos, value.tileEntity);
					tileEntity1.world.blockMap.remove(blockPos);
					
					tileEntity.markDirty();
					te.markDirty();
					worldIn.notifyBlockUpdate(tileEntity.getPos(), state, state, 3);
					worldIn.notifyBlockUpdate(te.getPos(), state, state, 3);
				}
			}
			long start = new Date().getTime();
			tileEntity1.world.tick(() -> Math.abs(new Date().getTime() - start) <= 10);
			
			if (tileEntity1.isNatural && tileEntity1.world.blockMap.isEmpty()) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
	}
	
	public static final VoxelShape virtuallyEmptyShape = VoxelShapes.create(0, 0, 0, 0.001f, 0.001f, 0.001f);
	
	@Override
	public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape shape;
		
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		
		if (context.getEntity() == null || !(tileEntityUncasted instanceof UnitTileEntity))
			return super.getShape(state, reader, pos, context);
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		shape = UnitRaytraceHelper.raytraceBlock(tileEntity, context.getEntity(), true, pos, Optional.of(context)).shapeHit;
		
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
			
			if (state1.getBlock() instanceof SmallerUnitBlock) continue;
			
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
				BlockPos pos2 = getHit(new BlockRayTraceResult(hit, context.getEntity().getHorizontalFacing(), pos1, false), pos1, tileEntity.unitsPerBlock);
				pos2 = pos2.offset(dir.getOpposite());
				
				if (dir == Direction.DOWN) pos2 = new BlockPos(pos2.getX(), -1, pos2.getZ());
				else if (dir == Direction.UP) pos2 = new BlockPos(pos2.getX(), tileEntity.unitsPerBlock, pos2.getZ());
				else if (dir == Direction.WEST) pos2 = new BlockPos(-1, pos2.getY(), pos2.getZ());
				else if (dir == Direction.EAST) pos2 = new BlockPos(tileEntity.unitsPerBlock, pos2.getY(), pos2.getZ());
				else if (dir == Direction.NORTH) pos2 = new BlockPos(pos2.getX(), pos2.getY(), -1);
				else if (dir == Direction.SOUTH)
					pos2 = new BlockPos(pos2.getX(), pos2.getY(), tileEntity.unitsPerBlock);
				
				float minX = pos2.getX() / (float) tileEntity.unitsPerBlock;
				float maxX = pos2.getX() / (float) tileEntity.unitsPerBlock + 1f / tileEntity.unitsPerBlock;
				float minY = pos2.getY() / (float) tileEntity.unitsPerBlock;
				float maxY = pos2.getY() / (float) tileEntity.unitsPerBlock + 1f / tileEntity.unitsPerBlock;
				float minZ = pos2.getZ() / (float) tileEntity.unitsPerBlock;
				float maxZ = pos2.getZ() / (float) tileEntity.unitsPerBlock + 1f / tileEntity.unitsPerBlock;
				
				float size = 0.001f;
				if (dir == Direction.UP) maxY = minY + size;
				else if (dir == Direction.DOWN) minY = maxY - size;
				else if (dir == Direction.WEST) minX = maxX - size;
				else if (dir == Direction.EAST) maxX = minX + size;
				else if (dir == Direction.SOUTH) maxZ = minZ + size;
				else if (dir == Direction.NORTH) minZ = maxZ - size;
				
				return VoxelShapes.combine(shape1, VoxelShapes.create(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).expand(0.01f, 0.01f, 0.01f).offset(-0.005f, -0.005f, -0.005f)), IBooleanFunction.AND);
			}
		}
		
		if (shape.isEmpty()) {
//			BlockPos hitPos;
			if (reader instanceof FakeServerWorld) {
//				hitPos = new BlockPos(((FakeServerWorld) reader).owner.world.result.getHitVec());
//				VoxelShape shape1 = VoxelShapes.empty();
//				BlockState state1 = tileEntity.world.getBlockState(hitPos);
//				for (AxisAlignedBB axisAlignedBB : shrink(state1.getRaytraceShape(reader, pos, context), tileEntity.unitsPerBlock)) {
//					shape1 = VoxelShapes.or(VoxelShapes.create(axisAlignedBB));
//				}
//				return shape1;
				return getCollisionShape(state, reader, pos, context);
			}
		}
		
		if (shape.isEmpty()) return super.getRayTraceShape(state, reader, pos, context);
		
		return VoxelShapes.empty();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
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
		nbt.remove("entities");
		nbt = NBTStripper.stripOfTEData(nbt);
		
		VoxelShape shape;
		if (!shapeMap.containsKey(nbt)) {
			shape = VoxelShapes.empty();
			
			for (SmallUnit value : tileEntity.world.blockMap.values()) {
				if (value.tileEntity != null) continue;
				
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.world, value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					shape2 = VoxelShapes.combine(shape2, VoxelShapes.create(axisAlignedBB), IBooleanFunction.OR);
				}
				shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
				shape = VoxelShapes.or(shape, shape2);
			}
			
			if (shapeMap.size() >= 11900) shapeMap.clear();
			shapeMap.put(nbt, shape);
			
			float padding = 0.05f;
			if (context.getEntity() != null)
				padding += context.getEntity().getMotion().distanceTo(new Vector3d(0, 0, 0)) * 2;
			for (SmallUnit value : tileEntity.world.blockMap.values()) {
				if (value.tileEntity == null) continue;
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.world, value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				AxisAlignedBB otherBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
				if (context.getEntity() != null) {
					if (context.getEntity() instanceof PlayerEntity)
						otherBB = new AxisAlignedBB(
								context.getEntity().getPosX() - (context.getEntity().getSize(Pose.STANDING).width / 2f),
								context.getEntity().getPosY(),
								context.getEntity().getPosZ() - (context.getEntity().getSize(Pose.STANDING).width / 2f),
								context.getEntity().getPosX() + (context.getEntity().getSize(Pose.STANDING).width / 2f),
								context.getEntity().getPosY() + (context.getEntity().getSize(Pose.STANDING).height),
								context.getEntity().getPosZ() + (context.getEntity().getSize(Pose.STANDING).width / 2f)
						);
					else otherBB = context.getEntity().getBoundingBox();
				}
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					if (
							context.getEntity() == null ||
									axisAlignedBB
											.expand(padding, padding, padding)
											.offset(-padding / 2f, -padding / 2f, -padding / 2f)
											.offset(
													value.pos.getX() / (float) tileEntity.unitsPerBlock,
													(value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock,
													value.pos.getZ() / (float) tileEntity.unitsPerBlock
											).offset(pos.getX(), pos.getY(), pos.getZ())
											.intersects(otherBB)
					) shape2 = VoxelShapes.combine(shape2, VoxelShapes.create(axisAlignedBB), IBooleanFunction.OR);
				}
				shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
				shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
			}
			
			if (shape.isEmpty()) return shape;
		} else {
			shape = shapeMap.get(nbt);
			float padding = 0.05f;
			if (context.getEntity() != null)
				padding += context.getEntity().getMotion().distanceTo(new Vector3d(0, 0, 0)) * 2;
			AxisAlignedBB otherBB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
			if (context.getEntity() != null) {
				if (context.getEntity() instanceof PlayerEntity)
					otherBB = new AxisAlignedBB(
							context.getEntity().getPosX() - (context.getEntity().getSize(Pose.STANDING).width / 2f),
							context.getEntity().getPosY(),
							context.getEntity().getPosZ() - (context.getEntity().getSize(Pose.STANDING).width / 2f),
							context.getEntity().getPosX() + (context.getEntity().getSize(Pose.STANDING).width / 2f),
							context.getEntity().getPosY() + (context.getEntity().getSize(Pose.STANDING).height),
							context.getEntity().getPosZ() + (context.getEntity().getSize(Pose.STANDING).width / 2f)
					);
				else otherBB = context.getEntity().getBoundingBox();
			}
			for (SmallUnit value : tileEntity.world.blockMap.values()) {
				if (value.tileEntity == null) continue;
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.world, value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					if (
							context.getEntity() == null ||
									axisAlignedBB
											.expand(padding, padding, padding)
											.offset(-padding / 2f, -padding / 2f, -padding / 2f)
											.offset(
													value.pos.getX() / (float) tileEntity.unitsPerBlock,
													(value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock,
													value.pos.getZ() / (float) tileEntity.unitsPerBlock
											).offset(pos.getX(), pos.getY(), pos.getZ())
											.intersects(otherBB)
					) shape2 = VoxelShapes.combine(shape2, VoxelShapes.create(axisAlignedBB), IBooleanFunction.OR);
				}
				shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
				shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
			}
		}
		return shape;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos) {
		return getCollisionShape(state, reader, pos, new ISelectionContext() {
			@Override
			public boolean getPosY() {
				return false;
			}
			
			@Override
			public boolean func_216378_a(VoxelShape shape, BlockPos pos, boolean p_216378_3_) {
				return false;
			}
			
			@Override
			public boolean hasItem(Item itemIn) {
				return false;
			}
			
			@Override
			public boolean func_230426_a_(FluidState p_230426_1_, FlowingFluid p_230426_2_) {
				return false;
			}
		});
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
	
	//idk why, but this makes selection work (better)
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
	
	@Override
	public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		TileEntity tileEntityUncasted = worldIn.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, pos, Optional.empty());
		
		BlockPos hitPos;
		if (raytraceContext.shapeHit.isEmpty()) return;
		else hitPos = raytraceContext.posHit;
		
		tileEntity.world.result = new BlockRayTraceResult(
				raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ()).scale(tileEntity.unitsPerBlock),
				raytraceContext.hitFace.orElse(Direction.UP),
				hitPos, true
		);
		
		tileEntity.world.getBlockState(hitPos).onBlockClicked(
				tileEntity.world, hitPos, player
		);
	}
}
