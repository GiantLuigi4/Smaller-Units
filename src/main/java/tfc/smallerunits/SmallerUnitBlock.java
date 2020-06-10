package tfc.smallerunits;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
import net.minecraftforge.common.ToolType;
import org.apache.logging.log4j.Level;
import tfc.smallerunits.Registry.Deferred;
import tfc.smallerunits.Utils.FakePlayer;
import tfc.smallerunits.Utils.SmallUnit;

import javax.annotation.Nullable;
import java.util.Random;

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
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		VoxelShape shape=Block.makeCuboidShape(0,0,0,0,0,0);
		try {
			SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)worldIn.getTileEntity(pos);
			for (SmallUnit u:te.containedWorld.unitHashMap.values()) {
				try {
					for (AxisAlignedBB bb:u.s.getCollisionShape(te.containedWorld,new BlockPos(u.x,u.y,u.z)).toBoundingBoxList()) {
						if (te.containedWorld.upb==0) {
							te.containedWorld.upb=4;
						}
						shape= VoxelShapes.combine(shape,VoxelShapes.create(
								bb.minX/te.containedWorld.upb,bb.minY/te.containedWorld.upb,bb.minZ/te.containedWorld.upb,
								bb.maxX/te.containedWorld.upb,bb.maxY/te.containedWorld.upb,bb.maxZ/te.containedWorld.upb
						).withOffset(u.x/(float)te.containedWorld.upb,u.y/(float)te.containedWorld.upb,u.z/(float)te.containedWorld.upb),IBooleanFunction.OR);
					}
				} catch (Exception err) {}
			}
		} catch (Exception err) {}
		return shape;
	}
	
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
			VoxelShape shape=Block.makeCuboidShape(0,0,0,0,0,0);
			try {
				SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)worldIn.getTileEntity(pos);
				for (SmallUnit u:te.containedWorld.unitHashMap.values()) {
					for (AxisAlignedBB bb:u.s.getShape(te.containedWorld,new BlockPos(u.x,u.y,u.z)).toBoundingBoxList()) {
						if (te.containedWorld.upb==0) {
							te.containedWorld.upb=4;
						}
						shape= VoxelShapes.combine(shape,VoxelShapes.create(
								bb.minX/te.containedWorld.upb,bb.minY/te.containedWorld.upb,bb.minZ/te.containedWorld.upb,
								bb.maxX/te.containedWorld.upb,bb.maxY/te.containedWorld.upb,bb.maxZ/te.containedWorld.upb
						).withOffset(u.x/(float)te.containedWorld.upb,u.y/(float)te.containedWorld.upb,u.z/(float)te.containedWorld.upb),IBooleanFunction.OR);
					}
				}
			} catch (Throwable ignored) {}
			if (shape.isEmpty()) {
				return VoxelShapes.create(0,0,0,1,1,1);
			}
			return shape;
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return getSelectedShape(worldIn,pos,context);
	}
	
	public VoxelShape getSelectedShape(IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		try {
			VoxelShape returnVal=Block.makeCuboidShape(0,0,0,0.01,0.01,0.01);
			double distanceBest=999999;
			Vec3d start=context.getEntity().getEyePosition(0);
			Vec3d stop=start.add(context.getEntity().getLookVec().scale(9));
			SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)worldIn.getTileEntity(pos);
			for (SmallUnit u:te.containedWorld.unitHashMap.values()) {
				try {
					double bestDist=999999;
					VoxelShape shape=null;
					for (AxisAlignedBB bb:u.s.getShape(te.containedWorld,new BlockPos(u.x,u.y,u.z)).toBoundingBoxList()) {
						try {
							if (te.containedWorld.upb==0) {
								te.containedWorld.upb=4;
							}
							AxisAlignedBB newBox=new AxisAlignedBB(
									bb.minX/te.containedWorld.upb,bb.minY/te.containedWorld.upb,bb.minZ/te.containedWorld.upb,
									bb.maxX/te.containedWorld.upb,bb.maxY/te.containedWorld.upb,bb.maxZ/te.containedWorld.upb
							).offset(u.x/(float)te.containedWorld.upb,u.y/(float)te.containedWorld.upb,u.z/(float)te.containedWorld.upb);
							if (shape==null) {
								shape=VoxelShapes.create(newBox);
							} else {
								shape=VoxelShapes.combine(shape,VoxelShapes.create(newBox),IBooleanFunction.OR);
							}
							if (newBox.offset(pos).rayTrace(start,stop).isPresent()) {
								double thisDist=newBox.offset(pos).rayTrace(start,stop).get().distanceTo(start);
								if (thisDist<bestDist) {
									bestDist=thisDist;
								}
							}
						} catch (Exception err) {}
					}
					if (!shape.isEmpty()) {
						if (bestDist<distanceBest) {
							returnVal=shape;
							distanceBest=bestDist;
						}
					}
				} catch (Exception err) {}
			}
			if (returnVal.isEmpty()) {
				returnVal=VoxelShapes.combine(returnVal,Block.makeCuboidShape(0,0,0,0.01,0.01,0.01),IBooleanFunction.OR);
			}
			return returnVal;
		} catch (Exception err) {}
		return Block.makeCuboidShape(0,0,0,0.01,0.01,0.01);
	}
	
	@Override
	public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return Block.makeCuboidShape(0,0,0,0,0,0);
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
		ItemStack value=new ItemStack(Deferred.UNITITEM.get());
		value.getOrCreateTag().put("BlockEntityTag",world.getTileEntity(pos).serializeNBT());
		return value;
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
//		System.out.println("h");
		super.tick(state, worldIn, pos, rand);
		if (worldIn.getTileEntity(pos) instanceof SmallerUnitsTileEntity) {
			((SmallerUnitsTileEntity)worldIn.getTileEntity(pos)).containedWorld.tick(worldIn);
		}
		worldIn.notifyBlockUpdate(pos,state,state,0);
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		try {
			SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)worldIn.getTileEntity(pos);
			Vec3d blockpos=hit.getHitVec().subtract(new Vec3d(pos)).scale(te.containedWorld.upb);
			BlockState heldState=Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState();
			if (!hit.getFace().getDirectionVec().toString().contains("-")) {
				if (blockpos.getY()%1==0) {
					blockpos=blockpos.subtract(0,1,0);
				} else if (blockpos.getX()%1==0) {
					blockpos=blockpos.subtract(1,0,0);
				} else if (blockpos.getZ()%1==0) {
					blockpos=blockpos.subtract(0,0,1);
				}
			}
			BlockPos loc=new BlockPos(blockpos);
			try {
				heldState=Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getStateForPlacement(heldState,hit.getFace(),heldState,te.containedWorld,new BlockPos(blockpos),loc.offset(hit.getFace()),handIn);
			} catch (Throwable err) {}
			try {
				BlockState clickedState=te.containedWorld.getBlockState(loc);
				VoxelShape shape=clickedState.getShape(te.containedWorld,loc);
				if (!shape.isEmpty()) {
					if (clickedState.getBlock().onBlockActivated(clickedState,te.containedWorld,loc,player,handIn,hit).equals(ActionResultType.PASS)) {
						if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
							loc=loc.offset(hit.getFace());
							FakePlayer fakePlayer=new FakePlayer(te.containedWorld,player.getGameProfile());
							fakePlayer.setPositionAndRotation(player.getPosX()-pos.getX(),player.getPosY()-pos.getY(),player.getPosZ()-pos.getZ(),player.rotationYaw,player.rotationPitch);
							fakePlayer.setRotationYawHead(player.getRotationYawHead());
							fakePlayer.setHeldItem(handIn,player.getHeldItem(handIn).copy());
							if (player.isCreative()) {
								fakePlayer.setGameType(GameType.CREATIVE);
							}
							Vec3d start=player.getEyePosition(0).subtract(player.getLookVec().scale(1)).subtract(new Vec3d(pos));
							Vec3d stop=start.add(player.getLookVec().scale(10));
							VoxelShape newShape=null;
							for (AxisAlignedBB bb:shape.toBoundingBoxList()) {
								if (newShape==null) {
									newShape=VoxelShapes.create(bb.shrink(te.containedWorld.upb).offset(loc));
								} else {
									newShape=VoxelShapes.combine(newShape,VoxelShapes.create(bb.shrink(te.containedWorld.upb).offset(loc)),IBooleanFunction.OR);
								}
							}
							BlockRayTraceResult result=(newShape.rayTrace(
									start.scale(te.containedWorld.upb),
									stop.scale(te.containedWorld.upb),
									loc.offset(hit.getFace().getOpposite())
							));
							System.out.println(result);
							System.out.println(start);
							System.out.println(stop);
							System.out.println(loc.offset(hit.getFace().getOpposite()));
							if (result!=null) {
								try {
									result=result.withFace(hit.getFace());
//									System.out.println(result.getPos());
//									System.out.println(result.getFace());
									System.out.println(fakePlayer.getHeldItem(handIn).onItemUse(new ItemUseContext(fakePlayer,handIn,result)));
								} catch (Throwable err) {
									StringBuilder stack=new StringBuilder("\n"+err.toString() + "(" + err.getMessage() + ")");
									for (StackTraceElement element:err.getStackTrace()) stack.append(element.toString()).append("\n");
									Smallerunits.LOGGER.log(Level.INFO, stack.toString());
								}
							}
							if (te.containedWorld.getBlockState(loc).equals(Blocks.AIR.getDefaultState())) {
								te.containedWorld.setBlockState(loc,heldState);
							}
						}
					}
				}
			} catch (Exception err) {
				if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
					BlockState clickedState=te.containedWorld.getBlockState(loc.offset(hit.getFace().getOpposite()));
					VoxelShape shape=clickedState.getShape(te.containedWorld,loc.offset(hit.getFace().getOpposite()));
					Vec3d start=player.getEyePosition(0).subtract(new Vec3d(pos));
					Vec3d stop=start.add(player.getLookVec().scale(9));
					BlockRayTraceResult result=(shape.rayTrace(
							start,
							stop,
							loc
					));
					try {
						if (result!=null) player.getHeldItem(handIn).onItemUse(new ItemUseContext(player,handIn,result));
						else te.containedWorld.setBlockState(loc,heldState.updatePostPlacement(hit.getFace(),heldState,te.containedWorld,loc,loc),0);
					} catch (Throwable ignored) {}
				}
				StringBuilder stack=new StringBuilder("\n"+err.toString() + "(" + err.getMessage() + ")");
				for (StackTraceElement element:err.getStackTrace()) stack.append(element.toString()).append("\n");
				Smallerunits.LOGGER.log(Level.INFO, stack.toString());
			}
			try {
				TileEntity tileEntity=te.containedWorld.getBlockState(loc).createTileEntity(te.containedWorld);
				if (tileEntity!=null) {
					if (player.getHeldItem(handIn).getOrCreateTag().contains("BlockEntityTag")) {
						tileEntity.read(player.getHeldItem(handIn).getOrCreateTag().getCompound("BlockEntityTag"));
						tileEntity.setPos(loc);
					}
					te.containedWorld.setTileEntity(loc,tileEntity);
				}
			} catch (Exception err2) {}
			te.markDirty();
			worldIn.notifyBlockUpdate(pos,state,state,0);
		} catch (Exception err) {
			StringBuilder stack=new StringBuilder("\n"+err.toString() + "(" + err.getMessage() + ")");
			for (StackTraceElement element:err.getStackTrace()) stack.append(element.toString()).append("\n");
			Smallerunits.LOGGER.log(Level.INFO, stack.toString());
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
		if (world.isRemote) {
			return false;
		}
		try {
			SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)world.getTileEntity(pos);
			BlockRayTraceResult hit=this.getRaytraceShape(state,world,pos).rayTrace(player.getEyePosition(0).subtract(player.getLookVec()),player.getEyePosition(0).add(player.getLookVec().scale(8)),pos);
			Vec3d blockpos=hit.getHitVec().subtract(new Vec3d(pos)).scale(te.containedWorld.upb);
			if (!hit.getFace().getDirectionVec().toString().contains("-")) {
				if (blockpos.getY()%1==0) {
					blockpos=blockpos.subtract(0,1,0);
				} else if (blockpos.getX()%1==0) {
					blockpos=blockpos.subtract(1,0,0);
				} else if (blockpos.getZ()%1==0) {
					blockpos=blockpos.subtract(0,0,1);
				}
			}
			BlockPos loc=new BlockPos(blockpos);
			te.containedWorld.setBlockState(loc, Blocks.AIR.getDefaultState(),0);
			world.notifyBlockUpdate(pos,state,state,0);
		} catch (Throwable ignored) {}
		return false;
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
