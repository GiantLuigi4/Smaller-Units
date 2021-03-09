package com.tfc.smallerunits.block;

import com.google.common.collect.ImmutableSet;
import com.tfc.smallerunits.SmallerUnitsConfig;
import com.tfc.smallerunits.helpers.ContainerMixinHelper;
import com.tfc.smallerunits.helpers.PacketHacksHelper;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.*;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
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
	
	public static VoxelShape getShapeOld(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape shape;
		
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		
		if (context.getEntity() == null || !(tileEntityUncasted instanceof UnitTileEntity))
			return Deferred.UNIT.get().getShape(state, reader, pos, context);
		
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
				BlockPos pos2 = ((SmallerUnitBlock) Deferred.UNIT.get()).getHit(new BlockRayTraceResult(hit, context.getEntity().getHorizontalFacing(), pos1, false), pos1, tileEntity.unitsPerBlock);
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
				
				return VoxelShapes.combine(shape1, VoxelShapes.create(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)
//						.expand(0.01f, 0.01f, 0.01f).offset(-0.005f, -0.005f, -0.005f)
				), IBooleanFunction.AND);
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
				return Deferred.UNIT.get().getCollisionShape(state, reader, pos, context);
			}
		}
		
		if (shape.isEmpty()) return Deferred.UNIT.get().getCollisionShape(state, reader, pos, context);
		
		return VoxelShapes.empty();
	}
	
	@Override
	public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
		TileEntity tileEntityUncasted = world.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return 0;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		return tileEntity.getBlockMap().isEmpty() ? 0 : -1;
	}
	
	@Override
	public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn) {
	
	}
	
	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		if (world.isRemote) return;
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof UnitTileEntity)) return;
		UnitTileEntity tileEntity = (UnitTileEntity) te;
		ArrayList<SmallUnit> toRemove = new ArrayList<>();
		for (SmallUnit value : tileEntity.getBlockMap().values()) {
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
			if (blockPos.state.getExplosionResistance(((UnitTileEntity) te).getFakeWorld(), blockPos.pos, explosion) < 1200.0F) {
				BlockState state1 = blockPos.state;
				
				List<ItemStack> stacks = state1.getDrops(
						new LootContext.Builder(tileEntity.worldServer)
								.withLuck(0)
								.withRandom(tileEntity.worldServer.rand)
								.withSeed(tileEntity.worldServer.rand.nextLong())
								.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
								.withParameter(LootParameters.field_237457_g_, new Vector3d(blockPos.pos.getX() + 0.5, blockPos.pos.getY() + 0.5, blockPos.pos.getZ() + 0.5))
								.withNullableParameter(LootParameters.BLOCK_ENTITY, tileEntity.worldServer.getTileEntity(blockPos.pos))
								.withParameter(LootParameters.BLOCK_STATE, state1)
								.withNullableParameter(LootParameters.THIS_ENTITY, explosion.getExploder())
				);
				
				for (ItemStack stack : stacks) {
					ItemEntity entity = new ItemEntity(tileEntity.getFakeWorld(), blockPos.pos.getX(), blockPos.pos.getY(), blockPos.pos.getZ(), stack);
					tileEntity.getFakeWorld().addEntity(entity);
				}
				
				blockPos.state.onBlockExploded(tileEntity.getFakeWorld(), blockPos.pos, explosion);
			}
		}
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
		TileEntity tileEntityUncasted = worldIn.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return 1;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		if (tileEntity.getBlockMap().isEmpty()) return 1;
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, pos, Optional.empty());
		
		BlockPos hitPos;
		if (worldIn instanceof FakeServerWorld) {
			hitPos = new BlockPos(((FakeServerWorld) worldIn).owner.worldServer.result.getHitVec());
		} else if (raytraceContext.shapeHit == null || raytraceContext.shapeHit.isEmpty()) {
			(player).resetActiveHand();
			return 0;
		} else hitPos = raytraceContext.posHit;
		
		BlockState state1 = tileEntity.getFakeWorld().getBlockState(hitPos);
		
		float amt = state1.getPlayerRelativeBlockHardness(player, tileEntity.getFakeWorld(), hitPos);
		System.out.println(1 / amt);
		amt *= tileEntity.unitsPerBlock;
		System.out.println(1 / amt);
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
		
		if (tileEntity.getBlockMap().isEmpty()) {
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
		
		raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(Direction.UP));
		raytraceContext.vecHit = raytraceContext.vecHit
				.subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ())
				.subtract(raytraceContext.posHit.getX() / ((float) tileEntity.unitsPerBlock), (raytraceContext.posHit.getY() - 64) / ((float) tileEntity.unitsPerBlock), raytraceContext.posHit.getZ() / ((float) tileEntity.unitsPerBlock))
		;
		raytraceContext.vecHit = raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).add(raytraceContext.posHit.getX(), raytraceContext.posHit.getY(), raytraceContext.posHit.getZ());
		Direction face = raytraceContext.hitFace.orElse(Direction.UP);
		raytraceContext.posHit = raytraceContext.posHit.offset(face.getOpposite());
		tileEntity.setRaytraceResult(new BlockRayTraceResult(
				raytraceContext.vecHit,
				face,
				raytraceContext.posHit,
				ticksRandomly
		));
		
		BlockState state1 = tileEntity.getFakeWorld().getBlockState(hitPos);
		
		if (state1.removedByPlayer(tileEntity.getFakeWorld(), hitPos, player, true, state1.getFluidState())) {
			List<ItemStack> stacks = state1.getDrops(
					new LootContext.Builder(tileEntity.worldServer)
							.withLuck(player.getLuck())
							.withRandom(tileEntity.worldServer.rand)
							.withSeed(tileEntity.worldServer.rand.nextLong())
							.withParameter(LootParameters.TOOL, player.getHeldItem(Hand.MAIN_HAND))
							.withParameter(LootParameters.field_237457_g_, raytraceContext.vecHit)
							.withNullableParameter(LootParameters.BLOCK_ENTITY, tileEntity.worldServer.getTileEntity(raytraceContext.posHit))
							.withParameter(LootParameters.BLOCK_STATE, state1)
							.withParameter(LootParameters.THIS_ENTITY, player)
			);
			
			for (ItemStack stack : stacks) {
				ItemEntity entity = new ItemEntity(tileEntity.getFakeWorld(), hitPos.getX(), hitPos.getY(), hitPos.getZ(), stack);
				tileEntity.getFakeWorld().addEntity(entity);
			}
			
			state1.onReplaced(tileEntity.getFakeWorld(), raytraceContext.posHit, Blocks.AIR.getDefaultState(), false);
			tileEntity.getFakeWorld().removeBlock(hitPos, false);
		}
		
		return false;
	}
	
	public boolean canBeRemoved(PlayerEntity player, World world, UnitTileEntity tileEntity, BlockPos worldPos) {
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, worldPos, Optional.empty());
		
		raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(Direction.UP));
		raytraceContext.vecHit = raytraceContext.vecHit
				.subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ())
				.subtract(raytraceContext.posHit.getX() / ((float) tileEntity.unitsPerBlock), (raytraceContext.posHit.getY() - 64) / ((float) tileEntity.unitsPerBlock), raytraceContext.posHit.getZ() / ((float) tileEntity.unitsPerBlock))
		;
		raytraceContext.vecHit = raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).add(raytraceContext.posHit.getX(), raytraceContext.posHit.getY(), raytraceContext.posHit.getZ());
		Direction face = raytraceContext.hitFace.orElse(Direction.UP);
		raytraceContext.posHit = raytraceContext.posHit.offset(face.getOpposite());
		
		tileEntity.setRaytraceResult(new BlockRayTraceResult(
				raytraceContext.vecHit,
				face,
				raytraceContext.posHit,
				ticksRandomly
		));
		
		RayTraceResult result = Minecraft.getInstance().objectMouseOver;
		if (FMLEnvironment.dist.isClient()) {
			Minecraft.getInstance().objectMouseOver = tileEntity.getResult();
		}
		Vector3d playerPos = player.getPositionVec();
		World currentWorld = player.world;
		player.setWorld(tileEntity.getFakeWorld());
		
		{
			Vector3d miniWorldPos = player.getPositionVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ());
			player.setRawPosition(miniWorldPos.getX(), miniWorldPos.getY(), miniWorldPos.getZ());
		}
		boolean canRemove = true;
		
		if (PacketHacksHelper.unitPos != null) {
			while (PacketHacksHelper.unitPos != null) {
				try {
					Thread.sleep(1);
				} catch (Throwable ignored) {
				}
			}
		}
		PacketHacksHelper.unitPos = new BlockPos(worldPos.getX(), worldPos.getY(), worldPos.getZ());
		canRemove = !player.getHeldItem(Hand.MAIN_HAND).onBlockStartBreak(raytraceContext.posHit, player);
		PacketHacksHelper.unitPos = null;
		
		Minecraft.getInstance().objectMouseOver = result;
		player.setWorld(currentWorld);
		player.setRawPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
		player.recenterBoundingBox();
		
		if (player.isCreative()) {
			return canRemove;
		}
		
		return true;
	}
	
	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getCollisionShape(state, worldIn, pos);
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.HIGH);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof UnitTileEntity) {
			if (!worldIn.isRemote) {
				((UnitTileEntity) te).createServerWorld();
			}
		}
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
		TileEntity tileEntityUncasted = world.getTileEntity(pos);
		if (!(tileEntityUncasted instanceof UnitTileEntity))
			return super.getPickBlock(state, target, world, pos, player);
		
		if (player.isSneaking()) {
			ItemStack stack = super.getPickBlock(state, target, world, pos, player);
			if (Screen.hasControlDown()) {
				CompoundNBT tileNBT = tileEntityUncasted.write(new CompoundNBT());
				System.out.println(tileNBT);
				stack.getOrCreateTag().put("BlockEntityTag", tileNBT);
				CompoundNBT compoundnbt1 = new CompoundNBT();
				ListNBT listnbt = new ListNBT();
				listnbt.add(StringNBT.valueOf("\"(+NBT)\""));
				compoundnbt1.put("Lore", listnbt);
				stack.setTagInfo("display", compoundnbt1);
			} else {
				CompoundNBT scaleNBT = new CompoundNBT();
				scaleNBT.putInt("upb", ((UnitTileEntity) tileEntityUncasted).unitsPerBlock);
				stack.getOrCreateTag().put("BlockEntityTag", scaleNBT);
			}
			return stack;
		}
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlock(tileEntity, player, true, pos, Optional.empty());
		
		BlockPos hitPos;
		if (raytraceContext.shapeHit.isEmpty()) {
			ItemStack stack = super.getPickBlock(state, target, world, pos, player);
			if (Screen.hasControlDown()) {
				CompoundNBT tileNBT = tileEntityUncasted.write(new CompoundNBT());
				System.out.println(tileNBT);
				stack.getOrCreateTag().put("BlockEntityTag", tileNBT);
				CompoundNBT compoundnbt1 = new CompoundNBT();
				ListNBT listnbt = new ListNBT();
				listnbt.add(StringNBT.valueOf("\"(+NBT)\""));
				compoundnbt1.put("Lore", listnbt);
				stack.setTagInfo("display", compoundnbt1);
			} else {
				CompoundNBT scaleNBT = new CompoundNBT();
				scaleNBT.putInt("upb", ((UnitTileEntity) tileEntityUncasted).unitsPerBlock);
				stack.getOrCreateTag().put("BlockEntityTag", scaleNBT);
			}
			return stack;
		} else hitPos = raytraceContext.posHit;
		
		BlockState state1 = tileEntity.getFakeWorld().getBlockState(hitPos);
		if (state1.getBlock() instanceof SmallerUnitBlock) {
			return super.getPickBlock(state, target, world, pos, player);
		}
		
		ItemStack stack;
		if (target instanceof BlockRayTraceResult) {
			stack = state1.getPickBlock(new BlockRayTraceResult(raytraceContext.vecHit, ((BlockRayTraceResult) target).getFace(), raytraceContext.posHit, ((BlockRayTraceResult) target).isInside()), world, pos, player);
		} else {
			stack = state1.getPickBlock(new BlockRayTraceResult(raytraceContext.vecHit, Direction.UP, raytraceContext.posHit, false), world, pos, player);
		}
		
		if (Screen.hasControlDown()) {
			TileEntity te = tileEntity.getFakeWorld().getTileEntity(raytraceContext.posHit);
			if (te != null) {
				Minecraft.getInstance().storeTEInStack(stack, te);
			}
		}
		return stack;
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
		boolean isEdge = false;
		if (raytraceContext.vecHit.equals(new Vector3d(-100, -100, -100))) {
			raytraceContext.vecHit = hit.getHitVec()
					.subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ())
					.add(worldPos.getX(), worldPos.getY(), worldPos.getZ())
			;
			if (raytraceContext.hitFace.orElse(hit.getFace()).equals(Direction.UP)) {
				raytraceContext.vecHit = raytraceContext.vecHit.subtract(0, -(1f / 16) / 4, 0);
			}
			raytraceContext.posHit = raytraceContext.posHit.up();
			isEdge = true;
		}
		raytraceContext.vecHit = raytraceContext.vecHit
				.subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ())
				.subtract(raytraceContext.posHit.getX() / ((float) tileEntity.unitsPerBlock), (raytraceContext.posHit.getY() - 64) / ((float) tileEntity.unitsPerBlock), raytraceContext.posHit.getZ() / ((float) tileEntity.unitsPerBlock))
		;
		raytraceContext.vecHit = raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).add(raytraceContext.posHit.getX(), raytraceContext.posHit.getY(), raytraceContext.posHit.getZ());
		Direction face = raytraceContext.hitFace.orElse(Direction.UP);
		ItemStack stack = player.getHeldItem(handIn);
		raytraceContext.posHit = raytraceContext.posHit.offset(face.getOpposite());
		tileEntity.setRaytraceResult(new BlockRayTraceResult(
				raytraceContext.vecHit,
				face,
				raytraceContext.posHit,
				hit.isInside()
		));
		
		BlockRayTraceResult result = new BlockRayTraceResult(
				hit.getHitVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ()).scale(tileEntity.unitsPerBlock).add(0, 64, 0),
				raytraceContext.hitFace.orElse(hit.getFace()), raytraceContext.posHit, hit.isInside()
		);
		boolean playerIsSleeping = player.isSleeping();
		ActionResultType resultType = ActionResultType.FAIL;
		Vector3d playerPos = player.getPositionVec();
		World currentWorld = player.world;
		player.setWorld(tileEntity.getFakeWorld());
		if (tileEntity.getFakeWorld().isRemote) {
			Minecraft.getInstance().world = (ClientWorld) tileEntity.getFakeWorld();
		}
		{
			Vector3d miniWorldPos = player.getPositionVec().subtract(worldPos.getX(), worldPos.getY(), worldPos.getZ());
			player.setRawPosition(miniWorldPos.getX(), miniWorldPos.getY(), miniWorldPos.getZ());
		}
		PacketHacksHelper.unitPos = worldPos;
		try {
			BlockPos pos = raytraceContext.posHit;
//			BlockPos pos = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite());
			if (!(player.isSneaking()) || stack.doesSneakBypassUse(worldIn, pos, player)) {
				resultType = tileEntity.getFakeWorld().getBlockState(pos).onBlockActivated(tileEntity.getFakeWorld(), player, handIn, result);
			}
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
		PacketHacksHelper.unitPos = null;
		if (!playerIsSleeping && player.isSleeping()) {
			player.stopSleepInBed(true, true);
			if (player instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) player).connection.setPlayerLocation(playerPos.getX(), playerPos.getY(), playerPos.getZ(), player.rotationYaw, player.rotationPitch);
			}
			player.sendStatusMessage(new StringTextComponent("Sorry, but you can't sleep in tiny beds."), true);
		}
		player.setWorld(currentWorld);
		if (tileEntity.getFakeWorld().isRemote) {
			Minecraft.getInstance().world = (ClientWorld) currentWorld;
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
		if (isEdge) {
			raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite());
			tileEntity.setRaytraceResult(new BlockRayTraceResult(
					raytraceContext.vecHit,
					face,
					raytraceContext.posHit,
					hit.isInside()
			));
		}
		
		if (stack.getItem() instanceof BlockItem) {
//			if (worldIn.isRemote) return ActionResultType.SUCCESS;
			
			BlockItem item = (BlockItem) stack.getItem();
			
			if (item.getBlock() instanceof SmallerUnitBlock
//					&& stack.getOrCreateTag().getCompound("BlockEntityTag").contains("ContainedUnits")
			)
				return ActionResultType.CONSUME;
//			if (item.getBlock() instanceof SmallerUnitBlock) return ActionResultType.CONSUME;
			
			BlockPos posOffset = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(hit.getFace()));
//			BlockPos posOffset = raytraceContext.posHit;
			BlockState clicked = tileEntity.getFakeWorld().getBlockState(raytraceContext.posHit);
			BlockItemUseContext context = new BlockItemUseContext(tileEntity.getFakeWorld(), player, handIn, stack, result);
			context.offsetPos = posOffset;
			if (true) {
				player.setWorld(tileEntity.getFakeWorld());
				PacketHacksHelper.unitPos = worldPos;
				if (worldIn.isRemote && stack.getItem() instanceof SignItem) {
					return ActionResultType.SUCCESS;
				}
//				ActionResultType type = item.onItemUse(context);
				ActionResultType type = ForgeHooks.onPlaceItemIntoWorld(context);
				PacketHacksHelper.unitPos = null;
				player.setWorld(currentWorld);
				player.setRawPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
				player.recenterBoundingBox();
				if (worldIn.isRemote) return type;
//				ActionResultType type = item.tryPlace(context);
				if (type.isSuccessOrConsume()) {
					BlockState statePlace = item.getBlock().getStateForPlacement(context);
					if (statePlace != null) {
						statePlace.onBlockAdded(tileEntity.getFakeWorld(), context.getPos(), Blocks.AIR.getDefaultState(), false);
						
						SoundType type1 = item.getBlock().getSoundType(statePlace);
						SoundEvent event = type1.getPlaceSound();
//					tileEntity.world.playSound(
//							null,
//							posOffset.getX() + 0.5, posOffset.getY() + 0.5, posOffset.getZ() + 0.5,
//							event, SoundCategory.BLOCKS, type1.getVolume(), type1.getPitch() - 0.25f
//					);
						ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(tileEntity.getFakeWorld().dimension, tileEntity.getFakeWorld(), posOffset), raytraceContext.hitFace.orElse(hit.getFace()));
					}
					return type;
				}
				return ActionResultType.PASS;
			}
			
			if (clicked.isReplaceable(context))
				posOffset = posOffset.offset(raytraceContext.hitFace.orElse(hit.getFace()).getOpposite());
			BlockState statePlace = item.getBlock().getStateForPlacement(context);
			if (statePlace != null) {
				if (tileEntity.getFakeWorld().getBlockState(posOffset).isReplaceable(context) || clicked.isAir()) {
					if (statePlace.isValidPosition(tileEntity.getFakeWorld(), posOffset)) {
						if (!player.isCreative()) stack.shrink(1);
						
						tileEntity.getFakeWorld().setBlockState(posOffset, statePlace);
						statePlace.getBlock().onBlockPlacedBy(tileEntity.getFakeWorld(), posOffset, statePlace, player, stack);
						
						if (!(stack.getItem() instanceof SkullItem)) {
							if (statePlace.getBlock() instanceof ITileEntityProvider) {
								TileEntity te = ((ITileEntityProvider) statePlace.getBlock()).createNewTileEntity(tileEntity.getFakeWorld());
								
								if (stack.hasTag()) {
									CompoundNBT nbt = stack.getOrCreateTag();
									
									if (nbt.contains("BlockEntityTag")) {
										nbt = nbt.getCompound("BlockEntityTag");
										te.read(statePlace, nbt);
									}
								}
								
								tileEntity.getFakeWorld().setTileEntity(posOffset, te);
							} else if (statePlace.getBlock().hasTileEntity(statePlace)) {
								TileEntity te = statePlace.getBlock().createTileEntity(statePlace, tileEntity.getFakeWorld());
								
								if (stack.hasTag()) {
									CompoundNBT nbt = stack.getOrCreateTag();
									
									if (nbt.contains("BlockEntityTag")) {
										nbt = nbt.getCompound("BlockEntityTag");
										te.read(statePlace, nbt);
									}
								}
								
								tileEntity.getFakeWorld().setTileEntity(posOffset, te);
							}
						}
						
						return ActionResultType.SUCCESS;
					}
				}
			}
		} else {
			raytraceContext.posHit = raytraceContext.posHit.offset(face);
			tileEntity.setRaytraceResult(new BlockRayTraceResult(
					raytraceContext.vecHit,
					face,
					raytraceContext.posHit,
					hit.isInside()
			));
			if (stack.getItem() instanceof BucketItem) {
//			if (worldIn.isRemote) return ActionResultType.SUCCESS;
				UnitRaytraceContext context = UnitRaytraceHelper.raytraceFluid(tileEntity, player, false, worldPos, Optional.empty());
				if (!context.shapeHit.isEmpty() && ((BucketItem) stack.getItem()).getFluid() == Fluids.EMPTY) {
					FluidState state1 = tileEntity.getFakeWorld().getFluidState(context.posHit);
					SoundEvent soundevent = state1.getFluid().getAttributes().getFillSound();
					if (state1.getFluid() != Fluids.EMPTY) {
						if (soundevent == null)
							soundevent = state1.getFluid().isIn(FluidTags.LAVA) ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
//						worldIn.playSound(
						player.playSound(
//								null,
//								worldPos.getX() + ((context.posHit.getX()) / tileEntity.unitsPerBlock),
//								worldPos.getY() + ((context.posHit.getY() - 64) / tileEntity.unitsPerBlock),
//								worldPos.getZ() + ((context.posHit.getZ()) / tileEntity.unitsPerBlock),
								soundevent, /*SoundCategory.PLAYERS, */1, 1
						);
						if (tileEntity.getFakeWorld().getBlockState(context.posHit).getBlock() instanceof IWaterLoggable) {
							((IWaterLoggable) tileEntity.getFakeWorld().getBlockState(context.posHit).getBlock()).pickupFluid(tileEntity.getFakeWorld(), context.posHit, tileEntity.getFakeWorld().getBlockState(context.posHit));
						} else {
							tileEntity.getFakeWorld().setBlockState(context.posHit, Blocks.AIR.getDefaultState());
						}
						stack.shrink(1);
						if (stack.getCount() == 0)
							player.setHeldItem(handIn, new ItemStack(state1.getFluid().getFilledBucket()));
						else if (!player.addItemStackToInventory(new ItemStack(state1.getFluid().getFilledBucket())))
							player.dropItem(new ItemStack(state1.getFluid().getFilledBucket()), true);
						return ActionResultType.SUCCESS;
					}
				} else {
					Fluid fluid = ((BucketItem) stack.getItem()).getFluid();
					if (fluid != Fluids.EMPTY) {
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
						raytraceContext.posHit = new BlockPos(
								Math.max(-2, raytraceContext.posHit.getX()),
								Math.max(63, raytraceContext.posHit.getY()),
								Math.max(-2, raytraceContext.posHit.getZ())
						);
						raytraceContext.posHit = raytraceContext.posHit.offset(face);
						((BucketItem) stack.getItem()).tryPlaceContainedLiquid(
								player, tileEntity.getFakeWorld(), raytraceContext.posHit, result
						);
						player.setHeldItem(handIn, ((BucketItem) stack.getItem()).emptyBucket(stack, player));
						return ActionResultType.SUCCESS;
					}
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
				BlockState clicked = tileEntity.getFakeWorld().getBlockState(raytraceContext.posHit);
				if (clicked.getBlock() instanceof IGrowable) {
					if (((IGrowable) clicked.getBlock()).canGrow(tileEntity.getFakeWorld(), raytraceContext.posHit, clicked, worldIn.isRemote)) {
						if (!worldIn.isRemote)
							((IGrowable) clicked.getBlock()).grow(tileEntity.worldServer, tileEntity.getFakeWorld().rand, raytraceContext.posHit, clicked);
						return ActionResultType.SUCCESS;
					}
				}
				if (!player.isCreative()) {
					stack.shrink(1);
				}
			} else {
				BlockPos posOffset = raytraceContext.posHit;
				if (!(stack.getItem() instanceof DebugStickItem)) {
					player.setWorld(tileEntity.getFakeWorld());
					player.setRawPosition(
							(playerPos.getX() - worldPos.getX()) * tileEntity.unitsPerBlock,
							((playerPos.getY() - worldPos.getY() - player.getEyeHeight()) * tileEntity.unitsPerBlock) + 64,
							(playerPos.getZ() - worldPos.getZ()) * tileEntity.unitsPerBlock
					);
					tileEntity.getFakeWorld().isRemote = worldIn.isRemote;
					PacketHacksHelper.unitPos = worldPos;
					BlockItemUseContext context = new BlockItemUseContext(
							tileEntity.getFakeWorld(), player, handIn, stack,
							new BlockRayTraceResult(
									raytraceContext.vecHit.scale(tileEntity.unitsPerBlock),
									raytraceContext.hitFace.orElse(hit.getFace()), posOffset, hit.isInside()
							)
					);
					context.offsetPos = posOffset;
					World currWorld = worldIn;
					player.world = tileEntity.getFakeWorld();
					if (currWorld.isRemote) {
						Minecraft.getInstance().world = tileEntity.worldClient;
					}
					ActionResultType type = ActionResultType.PASS;
					type = stack.getItem().onItemUse(context);
					if (currWorld.isRemote) {
						Minecraft.getInstance().world = (ClientWorld) currWorld;
					}
					player.world = currWorld;
					PacketHacksHelper.unitPos = null;
					player.setWorld(currentWorld);
					player.setRawPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
					return type;
				}
			}
		}
		
		return ActionResultType.PASS;
	}
	
	public static final VoxelShape virtuallyEmptyShape = VoxelShapes.create(0, 0, 0, 0.001f, 0.001f, 0.001f);
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		super.tick(state, worldIn, pos, rand);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.HIGH);
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof UnitTileEntity)) return;
		UnitTileEntity tileEntity1 = (UnitTileEntity) tileEntity;
		if (tileEntity1.getFakeWorld() != null) {
			ArrayList<SmallUnit> toRemove = new ArrayList<>();
			ArrayList<SmallUnit> toMove = new ArrayList<>();
			for (SmallUnit value : tileEntity1.getBlockMap().values()) {
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
				tileEntity1.getBlockMap().remove(smallUnit.pos.toLong());
			}
			for (SmallUnit value : toMove) {
				BlockPos blockPos = value.pos;
				ExternalUnitInteractionContext context;
				if (((UnitTileEntity) tileEntity).getFakeWorld() instanceof FakeServerWorld) {
					context = new ExternalUnitInteractionContext(((UnitTileEntity) tileEntity).worldServer, value.pos);
				} else {
					context = new ExternalUnitInteractionContext(((UnitTileEntity) tileEntity).worldClient, value.pos);
				}
				if (context.teInRealWorld instanceof UnitTileEntity) {
					if (((UnitTileEntity) context.teInRealWorld).getBlockMap().isEmpty()) {
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
					if (!worldIn.isRemote)
						((UnitTileEntity) te).createServerWorld();
					value.pos = context.posInFakeWorld;
					if (((UnitTileEntity) te).getFakeWorld() == null) continue;
					((UnitTileEntity) te).getFakeWorld().setBlockState(value.pos, value.state, 3, 0);
					((UnitTileEntity) te).getFakeWorld().setTileEntity(value.pos, value.tileEntity);
					tileEntity1.getBlockMap().remove(blockPos.toLong());
					
					tileEntity.markDirty();
					te.markDirty();
					worldIn.notifyBlockUpdate(tileEntity.getPos(), state, state, 3);
					worldIn.notifyBlockUpdate(te.getPos(), state, state, 3);
				}
			}
			
			if (tileEntity1.isNatural && tileEntity1.getBlockMap().isEmpty()) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
	}
	
	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
		TileEntity tileEntityUncasted = world.getTileEntity(pos);
		
		if (entity == null || !(tileEntityUncasted instanceof UnitTileEntity))
			return false;
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		for (SmallUnit value : tileEntity.getBlockMap().values()) {
			if (value.state.isIn(BlockTags.createOptional(new ResourceLocation("minecraft:climbable")))) {
				Vector3d pos1 = new Vector3d(value.pos.getX(), (value.pos.getY() - 64), value.pos.getZ());
				pos1 = pos1.add(0.5, 0.5, 0.5);
				pos1 = pos1.mul(1f / tileEntity.unitsPerBlock, 1f / tileEntity.unitsPerBlock, 1f / tileEntity.unitsPerBlock);
				pos1 = pos1.add(pos.getX(), pos.getY(), pos.getZ());
				if (entity.getBoundingBox().expand(0.05f, 0.05f, 0.05f).offset(-0.025f, -0.025f, -0.025f).contains(pos1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public VoxelShape getRayTraceShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		VoxelShape shape;
		
		TileEntity tileEntityUncasted = reader.getTileEntity(pos);
		
		if (context.getEntity() == null || !(tileEntityUncasted instanceof UnitTileEntity))
			return super.getShape(state, reader, pos, context);
		
		UnitTileEntity tileEntity = (UnitTileEntity) tileEntityUncasted;
		
		if ((!(reader instanceof ServerWorld)) && (!(reader instanceof World) || !((World) reader).isRemote || SmallerUnitsConfig.CLIENT.useExperimentalSelection.get())) {
			Minecraft.getInstance().getProfiler().startSection("su_create_raytrace_shape");
			shape = VoxelShapes.empty();
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
//				if (context.getEntity() != null) {
//					Entity entity = context.getEntity();
//
//					double reach = 8;
//					Vector3d start = entity.getPositionVec();
//					if (entity instanceof PlayerEntity)
//						reach = ((LivingEntity) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
//					Vector3d look = entity.getLookVec().scale(reach);
//					Vector3d end = entity.getEyePosition(0).add(look);
//
//					AxisAlignedBB aabb = new AxisAlignedBB(0,0,0,1f/tileEntity.unitsPerBlock,1f/tileEntity.unitsPerBlock,1f/tileEntity.unitsPerBlock);
//					aabb = aabb.expand((1f/tileEntity.unitsPerBlock)/4f,(1f/tileEntity.unitsPerBlock)/4f,(1f/tileEntity.unitsPerBlock)/4f);
//					aabb = aabb.offset(-(1f/tileEntity.unitsPerBlock)/8f,-(1f/tileEntity.unitsPerBlock)/8f,-(1f/tileEntity.unitsPerBlock)/8f);
//					aabb = aabb.offset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
//					aabb = aabb.offset(pos.getX(), pos.getY(), pos.getZ());
//					Optional<Vector3d> intercept = aabb.rayTrace(start, end);
//					if (!intercept.isPresent() && !aabb.contains(start) && !aabb.contains(end))
//						continue;
//				}
				Minecraft.getInstance().getProfiler().startSection("get_shape_for_" + value.state.toString());
				VoxelShape shape1 = value.state.getRaytraceShape(tileEntity.getFakeWorld(), value.pos, ISelectionContext.dummy());
				if (shape1.isEmpty()) shape1 = value.state.getShape(tileEntity.getFakeWorld(), tileEntity.getPos());
				Minecraft.getInstance().getProfiler().startSection("shrink_shape");
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					shape2 = VoxelShapes.combine(shape2, VoxelShapes.create(axisAlignedBB), IBooleanFunction.OR);
				}
				Minecraft.getInstance().getProfiler().endStartSection("move_shape");
				shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
				Minecraft.getInstance().getProfiler().endStartSection("merge_shape");
				shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
				Minecraft.getInstance().getProfiler().endSection();
				Minecraft.getInstance().getProfiler().endSection();
			}
			VoxelShape shape2 = VoxelShapes.empty();
			for (Direction value : Direction.values()) {
				BlockPos pos1 = pos.offset(value);
				BlockState state1 = reader.getBlockState(pos1);
				if (state1.getBlock() instanceof SmallerUnitBlock) continue;
				VoxelShape shape1 = state1.getRayTraceShape(reader, pos1);
				if (shape1.isEmpty()) shape1 = state1.getShape(reader, pos1);
				shape1 = shape1.withOffset(value.getXOffset(), value.getYOffset(), value.getZOffset());
				shape2 = VoxelShapes.combine(shape2, shape1, IBooleanFunction.OR);
			}
			shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
			Minecraft.getInstance().getProfiler().endSection();
			return shape;
		}
		
		return getShapeOld(state, reader, pos, context);
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
			
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
				if (value.tileEntity != null) continue;
				
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.getFakeWorld(), value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					shape2 = VoxelShapes.combine(shape2, VoxelShapes.create(axisAlignedBB), IBooleanFunction.OR);
				}
				if (!shape2.isEmpty()) {
					shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
					shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
				}
			}
			
			if (shapeMap.size() >= 11900) shapeMap.clear();
			shapeMap.put(nbt, shape);
			
			float padding = 0.05f;
			if (context.getEntity() != null)
				padding += context.getEntity().getMotion().distanceTo(new Vector3d(0, 0, 0)) * 2;
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
				if (value.tileEntity == null) continue;
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.getFakeWorld(), value.pos);
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
							context.getEntity() != null &&
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
				if (!shape2.isEmpty()) {
					shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
					shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
				}
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
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
				if (value.tileEntity == null) continue;
				VoxelShape shape1 = value.state.getCollisionShape(tileEntity.getFakeWorld(), value.pos);
				VoxelShape shape2 = VoxelShapes.empty();
				for (AxisAlignedBB axisAlignedBB : shrink(shape1, tileEntity.unitsPerBlock)) {
					if (
							context.getEntity() != null &&
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
				if (!shape2.isEmpty()) {
					shape2 = shape2.withOffset(value.pos.getX() / (float) tileEntity.unitsPerBlock, (value.pos.getY() - 64) / (float) tileEntity.unitsPerBlock, value.pos.getZ() / (float) tileEntity.unitsPerBlock);
					shape = VoxelShapes.combine(shape, shape2, IBooleanFunction.OR);
				}
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
		raytraceContext.posHit = raytraceContext.posHit.offset(raytraceContext.hitFace.orElse(Direction.UP));
		raytraceContext.vecHit = raytraceContext.vecHit
				.subtract(pos.getX(), pos.getY(), pos.getZ())
				.subtract(raytraceContext.posHit.getX() / ((float) tileEntity.unitsPerBlock), (raytraceContext.posHit.getY() - 64) / ((float) tileEntity.unitsPerBlock), raytraceContext.posHit.getZ() / ((float) tileEntity.unitsPerBlock))
		;
		raytraceContext.vecHit = raytraceContext.vecHit.scale(tileEntity.unitsPerBlock).add(raytraceContext.posHit.getX(), raytraceContext.posHit.getY(), raytraceContext.posHit.getZ());
		Direction face = raytraceContext.hitFace.orElse(Direction.UP);
		raytraceContext.posHit = raytraceContext.posHit.offset(face.getOpposite());
		tileEntity.setRaytraceResult(new BlockRayTraceResult(
				raytraceContext.vecHit,
				face,
				raytraceContext.posHit,
				ticksRandomly
		));
		if (!player.isCreative()) {
			player.getHeldItem(Hand.MAIN_HAND).onBlockStartBreak(pos, player);
		}
		
		tileEntity.getFakeWorld().getBlockState(hitPos).onBlockClicked(
				tileEntity.getFakeWorld(), hitPos, player
		);
	}
}
