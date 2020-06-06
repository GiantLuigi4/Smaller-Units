package tfc.smallerunits;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import tfc.smallerunits.Registry.Deferred;
import tfc.smallerunits.Utils.SmallUnit;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Stream;

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
						shape= VoxelShapes.or(shape,VoxelShapes.create(
								bb.minX/te.containedWorld.upb,bb.minY/te.containedWorld.upb,bb.minZ/te.containedWorld.upb,
								bb.maxX/te.containedWorld.upb,bb.maxY/te.containedWorld.upb,bb.maxZ/te.containedWorld.upb
						).withOffset(u.x/(float)te.containedWorld.upb,u.y/(float)te.containedWorld.upb,u.z/(float)te.containedWorld.upb));
					}
				} catch (Exception err) {}
			}
		} catch (Exception err) {}
		return shape;
	}
	
	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		try {
			return getShape(state, worldIn, pos, new ISelectionContext() {
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
			VoxelShape shape=Block.makeCuboidShape(0,0,0,0.01,0.01,0.01);
			try {
				SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)worldIn.getTileEntity(pos);
				for (SmallUnit u:te.containedWorld.unitHashMap.values()) {
					for (AxisAlignedBB bb:u.s.getShape(te.containedWorld,new BlockPos(u.x,u.y,u.z)).toBoundingBoxList()) {
						if (te.containedWorld.upb==0) {
							te.containedWorld.upb=4;
						}
						shape= VoxelShapes.or(shape,VoxelShapes.create(
								bb.minX/te.containedWorld.upb,bb.minY/te.containedWorld.upb,bb.minZ/te.containedWorld.upb,
								bb.maxX/te.containedWorld.upb,bb.maxY/te.containedWorld.upb,bb.maxZ/te.containedWorld.upb
						).withOffset(u.x/(float)te.containedWorld.upb,u.y/(float)te.containedWorld.upb,u.z/(float)te.containedWorld.upb));
					}
				}
			} catch (Throwable ignored) {}
			return shape;
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		try {
			VoxelShape returnVal=Block.makeCuboidShape(0,0,0,0.01,0.01,0.01);
			double distanceBest=9999;
			Vec3d start=context.getEntity().getEyePosition(0).subtract(context.getEntity().getLookVec());
			SmallerUnitsTileEntity te=(SmallerUnitsTileEntity)worldIn.getTileEntity(pos);
			for (SmallUnit u:te.containedWorld.unitHashMap.values()) {
				try {
					double bestDist=9999;
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
								shape=VoxelShapes.or(shape,VoxelShapes.create(newBox));
							}
							if (newBox.offset(pos).rayTrace(start,start.add(context.getEntity().getLookVec().scale(9))).get()!=null) {
								double thisDist=newBox.offset(pos).rayTrace(start,start.add(context.getEntity().getLookVec().scale(9))).get().distanceTo(start);
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
				returnVal=VoxelShapes.or(returnVal,Block.makeCuboidShape(0,0,0,0.01,0.01,0.01));
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
//			System.out.println(blockpos);
			Vec3d offset=(new Vec3d(hit.getFace().getDirectionVec())).normalize();
			Vec3d newblockpos=blockpos;
			BlockState heldState=Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState();
			if (offset.getX()==-1||offset.getY()==-1||offset.getZ()==-1) {
				newblockpos=blockpos.add(offset);
			}
			BlockPos loc=new BlockPos(Math.floor(blockpos.x),Math.floor(blockpos.y),Math.floor(blockpos.z));
			if (hit.getFace().equals(Direction.NORTH)) {
				loc=loc.north();
			} else if (hit.getFace().equals(Direction.WEST)) {
				loc=loc.east();
			} else if (hit.getFace().equals(Direction.DOWN)) {
				loc=loc.down();
			} else if (hit.getFace().equals(Direction.NORTH)) {
				loc=loc.south();
			} else if (hit.getFace().equals(Direction.SOUTH)) {
				loc=loc.north();
			}
			try {
				if (hit.getFace().equals(Direction.EAST)) {
					loc=loc.west();
				} else if (hit.getFace().equals(Direction.UP)) {
					loc=loc.down();
				} else if (hit.getFace().equals(Direction.WEST)) {
					loc=loc.west();
				} else if (hit.getFace().equals(Direction.NORTH)) {
					loc=loc.south();
				} else if (hit.getFace().equals(Direction.DOWN)) {
					loc=loc.up();
				}
				BlockState clickedState=te.containedWorld.getBlockState(loc);
				VoxelShape shape=clickedState.getShape(te.containedWorld,loc.add(new Vec3i(hit.getFace().getOpposite().getDirectionVec().getX(),hit.getFace().getOpposite().getDirectionVec().getY(),hit.getFace().getOpposite().getDirectionVec().getZ())));
				if (shape.isEmpty()) {
					if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
						te.containedWorld.setBlockState(loc,heldState,0);
					}
				} else {
//					System.out.println(loc);
//					System.out.println(clickedState);
//					System.out.println(shape.withOffset(loc.getX()/(float)te.containedWorld.upb,loc.getY()/(float)te.containedWorld.upb,loc.getZ()/(float)te.containedWorld.upb));
//					Vec3d start=player.getEyePosition(0).subtract(player.getLookVec()).subtract(new Vec3d(pos));
//					Vec3d stop =player.getEyePosition(0).add(player.getLookVec().scale(8)).subtract(new Vec3d(pos));
//					System.out.println(start);
//					System.out.println(stop);
//					BlockRayTraceResult result=shape.rayTrace(start,stop,loc);
//					System.out.println(result.toString());
//					System.out.println(result.getPos());
//					System.out.println(blockpos);
					if (clickedState.getBlock().onBlockActivated(clickedState,te.containedWorld,loc,player,handIn,hit).equals(ActionResultType.PASS)) {
						if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
							loc=loc.offset(hit.getFace());
							te.containedWorld.setBlockState(loc,heldState,0);
							heldState.updatePostPlacement(hit.getFace(),heldState,te.containedWorld,loc,loc);
						}
					}
				}
			} catch (Exception err) {
				if (!Block.getBlockFromItem(player.getHeldItem(handIn).getItem()).getDefaultState().equals(Blocks.AIR.getDefaultState())) {
					te.containedWorld.setBlockState(loc,heldState,0);
				}
			}
			te.markDirty();
			worldIn.notifyBlockUpdate(pos,state,state,0);
		} catch (Exception err) {}
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
			System.out.println(blockpos);
			Vec3d offset=(new Vec3d(hit.getFace().getOpposite().getDirectionVec())).normalize();
			Vec3d newblockpos=blockpos;
			if (offset.getX()==-1||offset.getY()==-1||offset.getZ()==-1) {
				newblockpos=blockpos.add(offset);
			}
			te.containedWorld.setBlockState(new BlockPos(newblockpos), Blocks.AIR.getDefaultState(),0);
			world.notifyBlockUpdate(pos,state,state,0);
		} catch (Exception err) {}
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
