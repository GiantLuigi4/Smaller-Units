package com.tfc.smallerunits.utils;

import com.tfc.smallerunits.block.SmallerUnitBlock;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ExternalUnitInteractionContext {
	public BlockPos posInFakeWorld;
	public BlockPos posInRealWorld;
	public BlockState stateInRealWorld;
	public TileEntity teInRealWorld;
	
	public ExternalUnitInteractionContext(BlockPos posInFakeWorld, BlockPos posInRealWorld, BlockState stateInRealWorld) {
		this.posInFakeWorld = posInFakeWorld;
		this.posInRealWorld = posInRealWorld;
		this.stateInRealWorld = stateInRealWorld;
	}
	
	public ExternalUnitInteractionContext(FakeServerWorld world, BlockPos pos) {
		int y = pos.getY() - 64;
		if (
				pos.getX() < 0 ||
						pos.getX() > world.owner.unitsPerBlock - 1 ||
						pos.getZ() < 0 ||
						pos.getZ() > world.owner.unitsPerBlock - 1 ||
						y < 0 ||
						y > world.owner.unitsPerBlock - 1
		) {
			BlockPos westPos = world.owner.getPos();
			BlockPos posInFakeWorld = pos;
			int i = 0;
			while (posInFakeWorld.getX() > world.owner.unitsPerBlock - 1 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.WEST, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.EAST, 1);
//				i++;
			}
			i = 0;
			while (posInFakeWorld.getX() < 0 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.EAST, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.WEST, 1);
//				i++;
			}
			i = 0;
			while (posInFakeWorld.getZ() > world.owner.unitsPerBlock - 1 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.NORTH, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.SOUTH, 1);
//				i++;
			}
			i = 0;
			while (posInFakeWorld.getZ() < 0 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.SOUTH, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.NORTH, 1);
//				i++;
			}
			i = 0;
			while ((posInFakeWorld.getY() - 64) > world.owner.unitsPerBlock - 1 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.DOWN, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.UP, 1);
//				i++;
			}
			i = 0;
			while ((posInFakeWorld.getY() - 64) < 0 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.UP, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.DOWN, 1);
//				i++;
			}
			BlockState westState = world.owner.getWorld().getBlockState(westPos);
			if (World.isOutsideBuildHeight(westPos)) {
				this.posInFakeWorld = posInFakeWorld;
				posInRealWorld = westPos;
				stateInRealWorld = Blocks.BARRIER.getDefaultState();
				teInRealWorld = null;
			}
			if (westState.getBlock() instanceof SmallerUnitBlock) {
				TileEntity westTE = world.owner.getWorld().getTileEntity(westPos);
				if (westTE instanceof UnitTileEntity) {
					if (((UnitTileEntity) westTE).unitsPerBlock == world.owner.unitsPerBlock) {
						this.posInFakeWorld = posInFakeWorld;
						posInRealWorld = westPos;
						stateInRealWorld = westState;
						teInRealWorld = westTE;
					} else {
						this.posInFakeWorld = posInFakeWorld;
						posInRealWorld = westPos;
						stateInRealWorld = Blocks.BARRIER.getDefaultState();
						teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
					}
				} else {
					this.posInFakeWorld = pos;
					posInRealWorld = westPos;
					stateInRealWorld = Blocks.BEDROCK.getDefaultState();
					teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				}
				return;
			} else if (
					westState.isAir(world.owner.getWorld(), westPos) ||
							westState.isTransparent() ||
							!westState.isSolid() ||
							!westState.isNormalCube(world.owner.getWorld(), westPos)
			) {
				this.posInFakeWorld = pos;
				posInRealWorld = westPos;
				stateInRealWorld = westState.isAir(world.owner.getWorld(), westPos) ? westState : Blocks.BARRIER.getDefaultState();
				teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				return;
			} else {
				this.posInFakeWorld = pos;
				posInRealWorld = westPos;
				stateInRealWorld = Blocks.BEDROCK.getDefaultState();
				teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				return;
			}
		}
		this.posInFakeWorld = pos;
		this.posInRealWorld = world.owner.getPos();
	}
	
	@OnlyIn(Dist.CLIENT)
	public ExternalUnitInteractionContext(FakeClientWorld world, BlockPos pos) {
		int y = pos.getY() - 64;
		if (
				pos.getX() < 0 ||
						pos.getX() > world.owner.unitsPerBlock - 1 ||
						pos.getZ() < 0 ||
						pos.getZ() > world.owner.unitsPerBlock - 1 ||
						y < 0 ||
						y > world.owner.unitsPerBlock - 1
		) {
			BlockPos westPos = world.owner.getPos();
			BlockPos posInFakeWorld = pos;
			int i = 0;
			while (posInFakeWorld.getX() > world.owner.unitsPerBlock - 1 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.WEST, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.EAST, 1);
//				i++;
			}
			i = 0;
			while (posInFakeWorld.getX() < 0 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.EAST, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.WEST, 1);
//				i++;
			}
			i = 0;
			while (posInFakeWorld.getZ() > world.owner.unitsPerBlock - 1 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.NORTH, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.SOUTH, 1);
//				i++;
			}
			i = 0;
			while (posInFakeWorld.getZ() < 0 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.SOUTH, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.NORTH, 1);
//				i++;
			}
			i = 0;
			while ((posInFakeWorld.getY() - 64) > world.owner.unitsPerBlock - 1 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.DOWN, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.UP, 1);
//				i++;
			}
			i = 0;
			while ((posInFakeWorld.getY() - 64) < 0 && i < 64) {
				posInFakeWorld = posInFakeWorld.offset(Direction.UP, world.owner.unitsPerBlock);
				westPos = westPos.offset(Direction.DOWN, 1);
//				i++;
			}
			BlockState westState = world.owner.getWorld().getBlockState(westPos);
			if (World.isOutsideBuildHeight(westPos)) {
				this.posInFakeWorld = posInFakeWorld;
				posInRealWorld = westPos;
				stateInRealWorld = Blocks.BARRIER.getDefaultState();
				teInRealWorld = null;
			}
			if (westState.getBlock() instanceof SmallerUnitBlock) {
				TileEntity westTE = world.owner.getWorld().getTileEntity(westPos);
				if (westTE instanceof UnitTileEntity) {
					if (((UnitTileEntity) westTE).unitsPerBlock == world.owner.unitsPerBlock) {
						this.posInFakeWorld = posInFakeWorld;
						posInRealWorld = westPos;
						stateInRealWorld = westState;
						teInRealWorld = westTE;
					} else {
						this.posInFakeWorld = posInFakeWorld;
						posInRealWorld = westPos;
						stateInRealWorld = Blocks.BARRIER.getDefaultState();
						teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
					}
				} else {
					this.posInFakeWorld = pos;
					posInRealWorld = westPos;
					stateInRealWorld = Blocks.BEDROCK.getDefaultState();
					teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				}
				return;
			} else if (
					westState.isAir(world.owner.getWorld(), westPos) ||
							westState.isTransparent() ||
							!westState.isSolid() ||
							!westState.isNormalCube(world.owner.getWorld(), westPos)
			) {
				this.posInFakeWorld = pos;
				posInRealWorld = westPos;
				stateInRealWorld = westState.isAir(world.owner.getWorld(), westPos) ? westState : Blocks.BARRIER.getDefaultState();
				teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				return;
			} else {
				this.posInFakeWorld = pos;
				posInRealWorld = westPos;
				stateInRealWorld = Blocks.BEDROCK.getDefaultState();
				teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				return;
			}
		}
		this.posInFakeWorld = pos;
		this.posInRealWorld = world.owner.getPos();
	}
}
