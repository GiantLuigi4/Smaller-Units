package tfc.smallerunits;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.impl.LootCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.common.ToolType;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.FakePlayer;
import tfc.smallerunits.utils.SmallUnit;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

public class SmallerUnitBlock extends Block implements ITileEntityProvider {
	public SmallerUnitBlock() {
		super(Properties.create(Material.ROCK)
				.hardnessAndResistance(3.0F, 40.0F)
				.sound(SoundType.STONE)
				.harvestLevel(0)
				.harvestTool(ToolType.PICKAXE)
		);
	}
	
	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
		return super.getBlockHardness(blockState,worldIn,pos);
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
		VoxelShape shape = getSelectedShape(worldIn, pos, new ISelectionContext() {
			@Override
			public boolean func_225581_b_() {
				return false;
			}
			
			@Override
			public boolean func_216378_a(VoxelShape shape, BlockPos pos, boolean p_216378_3_) {
				return true;
			}
			
			@Override
			public boolean hasItem(Item itemIn) {
				return false;
			}
			
			@Nullable
			@Override
			public Entity getEntity() {
				return player;
			}
		});
		
		TileEntity uncastedTE = worldIn.getTileEntity(pos);
		
		if (uncastedTE instanceof SmallerUnitsTileEntity) {
			SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) uncastedTE;
			Vec3d posBreak = shape.getBoundingBox().getCenter().scale(te.containedWorld.unitsPerBlock);
			BlockState stateRemove = te.containedWorld.getBlockState(new BlockPos(posBreak));
			return 1f / ((1f / (stateRemove.getPlayerRelativeBlockHardness(player, te.containedWorld, new BlockPos(posBreak)))) / te.containedWorld.unitsPerBlock);
		}
		
		return 1f;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape shape = Block.makeCuboidShape(0, 0, 0, 0, 0, 0);
		
		if (context != null) {
			Entity entity = context.getEntity();
			AxisAlignedBB bb1 = null;
			SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) worldIn.getTileEntity(pos);
			
			if (entity != null && te != null) {
				bb1 = entity.getCollisionBox(entity);
				
				if (bb1 == null) bb1 = entity.getCollisionBoundingBox();
				if (bb1 == null) bb1 = entity.getBoundingBox();
				
				bb1 = bb1.offset(new Vec3d(pos).scale(-1));
				float expand = 0.2125f;
				expand = Math.max(expand, entity.getCollisionBorderSize() * expand);
				bb1 = bb1.grow(expand, expand, expand);
				bb1 = bb1.grow(entity.getCollisionBorderSize());
			}
			
			AxisAlignedBB finalBb = bb1;
			if (te != null)
				for (SmallUnit u : te.containedWorld.unitHashMap.values()) {
					VoxelShape shape1 = u.heldState.getCollisionShape(te.containedWorld, new BlockPos(u.x, u.y, u.z));
					AtomicBoolean collides = new AtomicBoolean(false);
					
					if (entity != null) shape1.toBoundingBoxList().forEach((b) -> {
						if (b != null) {
							b = new AxisAlignedBB(
									b.minX / te.containedWorld.unitsPerBlock, b.minY / te.containedWorld.unitsPerBlock, b.minZ / te.containedWorld.unitsPerBlock,
									b.maxX / te.containedWorld.unitsPerBlock, b.maxY / te.containedWorld.unitsPerBlock, b.maxZ / te.containedWorld.unitsPerBlock
							).offset(u.x / (float) te.containedWorld.unitsPerBlock, u.y / (float) te.containedWorld.unitsPerBlock, u.z / (float) te.containedWorld.unitsPerBlock);
							if (checkCollision.apply(b, finalBb))
								collides.set(true);
						}
					});
					else collides.set(true);
					
					if (!shape1.isEmpty() && collides.get() && !u.heldState.isAir())
						if (te.containedWorld.unitsPerBlock == 0)
							te.containedWorld.unitsPerBlock = 4;
						
					for (AxisAlignedBB bb : shape1.toBoundingBoxList())
						if (entity != null) {
							bb = new AxisAlignedBB(
									bb.minX / te.containedWorld.unitsPerBlock, bb.minY / te.containedWorld.unitsPerBlock, bb.minZ / te.containedWorld.unitsPerBlock,
									bb.maxX / te.containedWorld.unitsPerBlock, bb.maxY / te.containedWorld.unitsPerBlock, bb.maxZ / te.containedWorld.unitsPerBlock
							).offset(u.x / (float) te.containedWorld.unitsPerBlock, u.y / (float) te.containedWorld.unitsPerBlock, u.z / (float) te.containedWorld.unitsPerBlock);
							
							if (checkCollision.apply(bb, bb1) || (bb.intersects(bb1) || bb.contains(bb1.getCenter()) || bb1.contains(bb.getCenter()) || context.func_216378_a(shape1, pos, true)))
								shape = VoxelShapes.combine(shape, VoxelShapes.create(bb), IBooleanFunction.OR);
						} else
							shape = VoxelShapes.combine(shape, VoxelShapes.create(
									bb.minX / te.containedWorld.unitsPerBlock, bb.minY / te.containedWorld.unitsPerBlock, bb.minZ / te.containedWorld.unitsPerBlock,
									bb.maxX / te.containedWorld.unitsPerBlock, bb.maxY / te.containedWorld.unitsPerBlock, bb.maxZ / te.containedWorld.unitsPerBlock
							).withOffset(u.x / (float) te.containedWorld.unitsPerBlock, u.y / (float) te.containedWorld.unitsPerBlock, u.z / (float) te.containedWorld.unitsPerBlock), IBooleanFunction.OR);
				}
		} else {
			SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) worldIn.getTileEntity(pos);
			
			if (te != null)
				for (SmallUnit u : te.containedWorld.unitHashMap.values()) {
					if (te.containedWorld.unitsPerBlock == 0)
						te.containedWorld.unitsPerBlock = 4;
					
					VoxelShape shape1 = u.heldState.getCollisionShape(te.containedWorld, new BlockPos(u.x, u.y, u.z));
					
					if (!shape1.isEmpty() && !u.heldState.isAir()) for (AxisAlignedBB bb : shape1.toBoundingBoxList())
						shape = VoxelShapes.combine(shape, VoxelShapes.create(
								bb.minX / te.containedWorld.unitsPerBlock, bb.minY / te.containedWorld.unitsPerBlock, bb.minZ / te.containedWorld.unitsPerBlock,
								bb.maxX / te.containedWorld.unitsPerBlock, bb.maxY / te.containedWorld.unitsPerBlock, bb.maxZ / te.containedWorld.unitsPerBlock
						).withOffset(u.x / (float) te.containedWorld.unitsPerBlock, u.y / (float) te.containedWorld.unitsPerBlock, u.z / (float) te.containedWorld.unitsPerBlock), IBooleanFunction.OR);
				}
		}
		
		return shape;
	}
	
	private static final BiFunction<AxisAlignedBB, AxisAlignedBB, Boolean> checkCollision = (a, b) -> b.intersects(a);
	
	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		try {
			return getSelectedShape(worldIn, pos, new ISelectionContext() {
				@Override
				public boolean func_225581_b_() {
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
				
				@Nullable
				@Override
				public Entity getEntity() {
					return Minecraft.getInstance().renderViewEntity;
				}
			});
		} catch (Throwable err) {
			VoxelShape shape = Block.makeCuboidShape(0, 0, 0, 0, 0, 0);
			
			try {
				SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) worldIn.getTileEntity(pos);
				for (SmallUnit u : te.containedWorld.unitHashMap.values()) {
					for (AxisAlignedBB bb : u.heldState.getShape(te.containedWorld, new BlockPos(u.x, u.y, u.z)).toBoundingBoxList()) {
						if (te.containedWorld.unitsPerBlock == 0) {
							te.containedWorld.unitsPerBlock = 4;
						}
						
						shape = VoxelShapes.combine(shape, VoxelShapes.create(
								bb.minX / te.containedWorld.unitsPerBlock, bb.minY / te.containedWorld.unitsPerBlock, bb.minZ / te.containedWorld.unitsPerBlock,
								bb.maxX / te.containedWorld.unitsPerBlock, bb.maxY / te.containedWorld.unitsPerBlock, bb.maxZ / te.containedWorld.unitsPerBlock
						).withOffset(u.x / (float) te.containedWorld.unitsPerBlock, u.y / (float) te.containedWorld.unitsPerBlock, u.z / (float) te.containedWorld.unitsPerBlock), IBooleanFunction.OR);
					}
				}
			} catch (Throwable ignored) {
			}
			
			if (shape.isEmpty()) {
				return VoxelShapes.create(0, 0, 0, 1, 1, 1);
			}
			
			return shape;
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return getSelectedShape(worldIn, pos, context);
	}
	
	public VoxelShape getSelectedShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (context.getEntity() != null)
			try {
				VoxelShape defaultShape = Block.makeCuboidShape(0, 0, 0, 0.01, 0.01, 0.01);
				VoxelShape returnVal = Block.makeCuboidShape(0, 0, 0, 0.01, 0.01, 0.01);
				double distanceBest = 999999;
				Vec3d start = context.getEntity().getEyePosition(0);
				Vec3d stop = start.add(context.getEntity().getLookVec().scale(9));
				SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) worldIn.getTileEntity(pos);
				
				if (te != null) {
					for (SmallUnit u : te.containedWorld.unitHashMap.values()) {
						try {
							double bestDist = 999999;
							VoxelShape shape = null;
							
							for (AxisAlignedBB bb : u.heldState.getShape(te.containedWorld, new BlockPos(u.x, u.y, u.z)).toBoundingBoxList()) {
								try {
									if (te.containedWorld.unitsPerBlock == 0) {
										te.containedWorld.unitsPerBlock = 4;
									}
									
									AxisAlignedBB newBox = new AxisAlignedBB(
											bb.minX / te.containedWorld.unitsPerBlock, bb.minY / te.containedWorld.unitsPerBlock, bb.minZ / te.containedWorld.unitsPerBlock,
											bb.maxX / te.containedWorld.unitsPerBlock, bb.maxY / te.containedWorld.unitsPerBlock, bb.maxZ / te.containedWorld.unitsPerBlock
									).offset(u.x / (float) te.containedWorld.unitsPerBlock, u.y / (float) te.containedWorld.unitsPerBlock, u.z / (float) te.containedWorld.unitsPerBlock);
									
									if (shape == null) {
										shape = VoxelShapes.create(newBox);
									} else {
										shape = VoxelShapes.combine(shape, VoxelShapes.create(newBox), IBooleanFunction.OR);
									}
									
									if (newBox.offset(pos).rayTrace(start, stop).isPresent()) {
										double thisDist = newBox.offset(pos).rayTrace(start, stop).get().distanceTo(start);
										
										if (thisDist < bestDist) {
											bestDist = thisDist;
										}
									}
								} catch (Exception ignored) {
								}
							}
							
							if (shape != null && !shape.isEmpty()) {
								if (bestDist < distanceBest) {
									returnVal = shape;
									distanceBest = bestDist;
								}
							}
						} catch (Exception ignored) {
						}
					}
					
					if (returnVal.toString().equals(defaultShape.toString())) {
						for (Direction off : Direction.values()) {
							VoxelShape shape = worldIn.getBlockState(pos.offset(off)).getShape(worldIn,pos);
							VoxelShape shapeReturn = VoxelShapes.create(0,0,0,0,0,0);
							
							try {
								for (AxisAlignedBB bb : shape.toBoundingBoxList()) {
									AxisAlignedBB newBox = new AxisAlignedBB(
											bb.minX, bb.minY, bb.minZ,
											bb.maxX, bb.maxY, bb.maxZ
									);
									Optional<Vec3d> result = newBox.offset(pos.offset(off)).rayTrace(start, stop);
							
									if (result.isPresent()) {
										newBox = new AxisAlignedBB(
												bb.minX / te.containedWorld.unitsPerBlock, bb.minY / te.containedWorld.unitsPerBlock, bb.minZ / te.containedWorld.unitsPerBlock,
												bb.maxX / te.containedWorld.unitsPerBlock, bb.maxY / te.containedWorld.unitsPerBlock, bb.maxZ / te.containedWorld.unitsPerBlock
										);
										
										try {
											VoxelShape shapeBox = VoxelShapes.create(newBox)
													.withOffset(off.getXOffset() / (float) te.containedWorld.unitsPerBlock, off.getYOffset() / (float) te.containedWorld.unitsPerBlock, off.getZOffset() / (float) te.containedWorld.unitsPerBlock)
													.withOffset(-off.getXOffset() * (1f / te.containedWorld.unitsPerBlock), -off.getYOffset() * (1f / te.containedWorld.unitsPerBlock), -off.getZOffset() * (1f / te.containedWorld.unitsPerBlock));
											
											Vec3d posA = result.get().subtract(pos.getX(), pos.getY(), pos.getZ());
											
											posA = new Vec3d(
													((float) ((int) (posA.x * te.containedWorld.unitsPerBlock)) / te.containedWorld.unitsPerBlock),
													((float) ((int) (posA.y * te.containedWorld.unitsPerBlock)) / te.containedWorld.unitsPerBlock),
													((float) ((int) (posA.z * te.containedWorld.unitsPerBlock)) / te.containedWorld.unitsPerBlock)
											);
											
											if (off.getXOffset() != 0) {
												posA = new Vec3d(
														off.getXOffset() == 1 ? 0 : 1 - (1f / te.containedWorld.unitsPerBlock),
														posA.y,
														posA.z
												);
											} else if (off.getYOffset() != 0) {
												posA = new Vec3d(
														posA.x,
														off.getYOffset() == 1 ? 0 : 1 - (1f / te.containedWorld.unitsPerBlock),
														posA.z
												);
											} else if (off.getZOffset() != 0) {
												posA = new Vec3d(
														posA.x,
														posA.y,
														off.getZOffset() == 1 ? 0 : 1 - (1f / te.containedWorld.unitsPerBlock)
												);
											}
											
											shapeBox = shapeBox.withOffset(posA.getX(), posA.getY(), posA.getZ());
											
											shapeReturn = VoxelShapes.combine(shapeReturn, shapeBox, IBooleanFunction.OR);
										} catch (Throwable err) {
											err.printStackTrace();
										}
										
										return shapeReturn.withOffset(off.getXOffset(), off.getYOffset(), off.getZOffset());
									}
								}
							} catch (Throwable err) {
								err.printStackTrace();
							}
							
							if (!shapeReturn.isEmpty())
								return shapeReturn;
						}
						
						returnVal = Block.makeCuboidShape(0, 0, 0, 0.01, 0.01, 0.01);
					}
				}
				
				return returnVal;
			} catch (Exception ignored) {
			}
		return Block.makeCuboidShape(0, 0, 0, 0.01, 0.01, 0.01);
	}
	
	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return Block.makeCuboidShape(0, 0, 0, 0, 0, 0);
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		super.onEntityCollision(state, worldIn, pos, entityIn);
	}
	
	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		ItemStack value = new ItemStack(Deferred.UNITITEM.get());
		value.getOrCreateTag().put("BlockEntityTag", world.getTileEntity(pos).serializeNBT());
		return value;
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		super.tick(state, worldIn, pos, rand);
		if (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof SmallerUnitsTileEntity)
			((SmallerUnitsTileEntity) worldIn.getTileEntity(pos)).containedWorld.tick(worldIn);
		worldIn.notifyBlockUpdate(pos, state, state, 0);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (worldIn.isRemote) return ActionResultType.SUCCESS;
		if (player.getHeldItem(handIn).getItem().equals(Deferred.UNIT)) return ActionResultType.PASS;
		try {
			SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) worldIn.getTileEntity(pos);
			Vec3d blockpos = hit.getHitVec().subtract(new Vec3d(pos)).scale(te.containedWorld.unitsPerBlock);
			BlockState heldState = Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState();
			
			if (!hit.getFace().getDirectionVec().toString().contains("-"))
				if (blockpos.getY() % 1 == 0)
					blockpos = blockpos.subtract(0, 1, 0);
				else if (blockpos.getX() % 1 == 0)
					blockpos = blockpos.subtract(1, 0, 0);
				else if (blockpos.getZ() % 1 == 0)
					blockpos = blockpos.subtract(0, 0, 1);
			
			BlockPos loc = new BlockPos(blockpos);
			
			try {
				heldState = Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getStateForPlacement(heldState, hit.getFace(), heldState, te.containedWorld, new BlockPos(blockpos), loc.offset(hit.getFace()), handIn);
			} catch (Throwable ignored) {
			}
			
			try {
				BlockState clickedState = te.containedWorld.getBlockState(loc);
				VoxelShape shape = clickedState.getShape(te.containedWorld, loc);
				if (!shape.isEmpty()) {
					if (clickedState.getBlock().onBlockActivated(clickedState, te.containedWorld, loc, player, handIn, hit).equals(ActionResultType.PASS)) {
						if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
							loc = loc.offset(hit.getFace());
							FakePlayer fakePlayer = new FakePlayer(te.containedWorld, player.getGameProfile());
							fakePlayer.setPositionAndRotation(player.getPosX() - pos.getX(), player.getPosY() - pos.getY(), player.getPosZ() - pos.getZ(), player.rotationYaw, player.rotationPitch);
							fakePlayer.setRotationYawHead(player.getRotationYawHead());
							fakePlayer.setHeldItem(handIn, player.getHeldItem(handIn).copy());
							
							if (player.isCreative()) {
								fakePlayer.setGameType(GameType.CREATIVE);
							}
							
							Vec3d start = player.getEyePosition(0).subtract(player.getLookVec().scale(1)).subtract(new Vec3d(pos));
							Vec3d stop = start.add(player.getLookVec().scale(10));
							VoxelShape newShape = null;
							
							for (AxisAlignedBB bb : shape.toBoundingBoxList()) {
								if (newShape == null) {
									newShape = VoxelShapes.create(bb.shrink(te.containedWorld.unitsPerBlock).offset(loc));
								} else {
									newShape = VoxelShapes.combine(newShape, VoxelShapes.create(bb.shrink(te.containedWorld.unitsPerBlock).offset(loc)), IBooleanFunction.OR);
								}
							}
							
							BlockRayTraceResult result = (newShape.rayTrace(
									start.scale(te.containedWorld.unitsPerBlock),
									stop.scale(te.containedWorld.unitsPerBlock),
									loc.offset(hit.getFace().getOpposite())
							));
//							System.out.println(result);
//							System.out.println(start);
//							System.out.println(stop);
//							System.out.println(loc.offset(hit.getFace().getOpposite()));
							
							if (result != null) {
								try {
									result = result.withFace(hit.getFace());
									fakePlayer.getHeldItem(handIn).onItemUse(new ItemUseContext(fakePlayer, handIn, result));
									if (!player.isCreative())
										player.getHeldItem(handIn).shrink(1);
								} catch (Throwable err) {
									StringBuilder stack = new StringBuilder("\n" + err.toString() + "(" + err.getMessage() + ")");
									
									for (StackTraceElement element : err.getStackTrace())
										stack.append(element.toString()).append("\n");
									
									System.out.println(stack.toString());
									System.out.println(result);
								}
							}
							
							if (te.containedWorld.getBlockState(loc).equals(Blocks.AIR.getDefaultState())) {
								te.containedWorld.setBlockState(loc, heldState);
								if (!player.isCreative())
									player.getHeldItem(handIn).shrink(1);
							}
						}
					}
				}
			} catch (Throwable err) {
				if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
					BlockState clickedState = te.containedWorld.getBlockState(loc.offset(hit.getFace().getOpposite()));
					VoxelShape shape = clickedState.getShape(te.containedWorld, loc.offset(hit.getFace().getOpposite()));
					Vec3d start = player.getEyePosition(0).subtract(new Vec3d(pos));
					Vec3d stop = start.add(player.getLookVec().scale(9));
					BlockRayTraceResult result = (shape.rayTrace(
							start,
							stop,
							loc
					));
					
					try {
						if (result != null) {
							player.getHeldItem(handIn).onItemUse(new ItemUseContext(player, handIn, result));
							if (!player.isCreative())
								player.getHeldItem(handIn).shrink(1);
						} else {
							te.containedWorld.setBlockState(loc, heldState.updatePostPlacement(hit.getFace(), heldState, te.containedWorld, loc, loc), 0);
							if (!player.isCreative())
								player.getHeldItem(handIn).shrink(1);
						}
					} catch (Throwable ignored) {
						StringBuilder stack = new StringBuilder("\n" + err.toString() + "(" + err.getMessage() + ")");
						
						for (StackTraceElement element : err.getStackTrace())
							stack.append(element.toString()).append("\n");
						
						System.out.println(stack.toString());
					}
				}
				
				StringBuilder stack = new StringBuilder("\n" + err.toString() + "(" + err.getMessage() + ")");
				for (StackTraceElement element : err.getStackTrace()) stack.append(element.toString()).append("\n");
				System.out.println(stack.toString());
			}
			
			try {
				TileEntity tileEntity = te.containedWorld.getBlockState(loc).createTileEntity(te.containedWorld);
				if (tileEntity != null) {
					if (player.getHeldItem(handIn).getOrCreateTag().contains("BlockEntityTag")) {
						tileEntity.read(player.getHeldItem(handIn).getOrCreateTag().getCompound("BlockEntityTag"));
						tileEntity.setPos(loc);
					}
					
					te.containedWorld.setTileEntity(loc, tileEntity);
				}
			} catch (Throwable err2) {
			}
			
			te.markDirty();
			worldIn.notifyBlockUpdate(pos, state, state, 0);
		} catch (Throwable err) {
			StringBuilder stack = new StringBuilder("\n" + err.toString() + "(" + err.getMessage() + ")");
		
			for (StackTraceElement element : err.getStackTrace()) stack.append(element.toString()).append("\n");
		
			System.out.println(stack.toString());
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
		TileEntity teUncasted = world.getTileEntity(pos);
		if (teUncasted instanceof SmallerUnitsTileEntity) {
			SmallerUnitsTileEntity te = (SmallerUnitsTileEntity) world.getTileEntity(pos);
			
			if (world.isRemote) return false;

			if (te.containedWorld.unitHashMap.isEmpty()) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
				if (!player.isCreative()) {
					ItemStack stack = new ItemStack(Deferred.UNITITEM.get());
					if (!stack.getOrCreateTag().contains("BlockEntityTag")) {
						CompoundNBT tag = new CompoundNBT();
						stack.getOrCreateTag().put("BlockEntityTag", tag);
					}
					stack.getOrCreateTag().getCompound("BlockEntityTag").putString("world", "");
					ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
					world.addEntity(entity);
				}
				return false;
			}
			
			BlockRayTraceResult hit = this.getRaytraceShape(state, world, pos).rayTrace(player.getEyePosition(0).subtract(player.getLookVec()), player.getEyePosition(0).add(player.getLookVec().scale(8)), pos);
			Vec3d blockpos = hit.getHitVec().subtract(new Vec3d(pos)).scale(te.containedWorld.unitsPerBlock);
			
			if (!hit.getFace().getDirectionVec().toString().contains("-"))
				if (blockpos.getY() % 1 == 0)
					blockpos = blockpos.subtract(0, 1, 0);
				else if (blockpos.getX() % 1 == 0)
					blockpos = blockpos.subtract(1, 0, 0);
				else if (blockpos.getZ() % 1 == 0)
					blockpos = blockpos.subtract(0, 0, 1);
			
			BlockPos loc = new BlockPos(blockpos);
			BlockState removed = te.containedWorld.getBlockState(loc);
			
			try {
				if (world instanceof ServerWorld) {
					Collection<ItemStack> stackCollection;
					ResourceLocation resourcelocation = removed.getBlock().getLootTable();
					if (resourcelocation == LootTables.EMPTY) {
						stackCollection = Collections.emptyList();
					} else {
						LootContext.Builder lootcontext$builder =
								(new LootContext.Builder((ServerWorld)world))
										.withParameter(LootParameters.POSITION, pos)
										.withParameter(LootParameters.BLOCK_STATE, state)
										.withNullableParameter(LootParameters.BLOCK_ENTITY, te.containedWorld.getTileEntity(loc))
										.withNullableParameter(LootParameters.THIS_ENTITY, player)
										.withParameter(LootParameters.TOOL, player.getHeldItem(Hand.MAIN_HAND));
						
						LootContext lootcontext = lootcontext$builder.build(LootParameterSets.BLOCK);
						ServerWorld serverworld = lootcontext.getWorld();
						LootTable loottable = serverworld.getServer().getLootTableManager().getLootTableFromLocation(resourcelocation);
						stackCollection = loottable.generate(lootcontext);
					}
					for (ItemStack stack : stackCollection) {
						if (!player.isCreative()) {
							ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
							world.addEntity(entity);
						}
					}
				}
			} catch (Throwable ignored) {
			}
			
			te.containedWorld.setBlockState(loc, Blocks.AIR.getDefaultState(), 0);
			world.notifyBlockUpdate(pos, state, state, 0);
			
			return false;
		}
		
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new SmallerUnitsTileEntity();
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SmallerUnitsTileEntity();
	}
}
