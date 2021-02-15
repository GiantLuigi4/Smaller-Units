package com.tfc.smallerunits.utils;

import com.tfc.smallerunits.block.SmallerUnitBlock;
import com.tfc.smallerunits.block.UnitTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

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
			if (pos.getX() < 0)
				westPos = westPos.offset(Direction.EAST, (int) Math.floor((1f / world.owner.unitsPerBlock) * pos.getX()));
			else if (pos.getX() > world.owner.unitsPerBlock - 1)
				westPos = westPos.offset(Direction.EAST, (int) Math.ceil((1f / world.owner.unitsPerBlock) * pos.getX()));
			if (pos.getZ() < 0)
				westPos = westPos.offset(Direction.SOUTH, (int) Math.floor((1f / world.owner.unitsPerBlock) * pos.getZ()));
			else if (pos.getZ() > world.owner.unitsPerBlock - 1)
				westPos = westPos.offset(Direction.SOUTH, (int) Math.ceil((1f / world.owner.unitsPerBlock) * pos.getZ()));
			if (y < 0)
				westPos = westPos.offset(Direction.UP, (int) Math.floor((1f / world.owner.unitsPerBlock) * y));
			else if (y > world.owner.unitsPerBlock - 1)
				westPos = westPos.offset(Direction.UP, (int) Math.ceil((1f / world.owner.unitsPerBlock) * y));
			BlockState westState = world.owner.getWorld().getBlockState(westPos);
			if (westState.getBlock() instanceof SmallerUnitBlock) {
				TileEntity westTE = world.owner.getWorld().getTileEntity(westPos);
				if (westTE instanceof UnitTileEntity) {
					if (((UnitTileEntity) westTE).unitsPerBlock == world.owner.unitsPerBlock) {
						BlockPos pos1 = new BlockPos(pos.getX(), y + 64, pos.getZ());
						if (pos1.getX() < 0)
							pos1 = new BlockPos(((world.owner.unitsPerBlock - Math.abs(pos.getX() % world.owner.unitsPerBlock))), pos1.getY(), pos1.getZ());
						else if (pos1.getX() > world.owner.unitsPerBlock - 1)
							pos1 = new BlockPos(world.owner.unitsPerBlock - ((Math.abs(pos.getX() % world.owner.unitsPerBlock))) - world.owner.unitsPerBlock, pos1.getY(), pos1.getZ());
						if (y < 0)
							pos1 = new BlockPos(pos1.getX(), (world.owner.unitsPerBlock - Math.abs(y % world.owner.unitsPerBlock)) + 64, pos1.getZ());
						else if (y > world.owner.unitsPerBlock - 1)
							pos1 = new BlockPos(pos1.getX(), (world.owner.unitsPerBlock - ((Math.abs(y % world.owner.unitsPerBlock))) - world.owner.unitsPerBlock) + 64, pos1.getZ());
						if (pos.getZ() < 0)
							pos1 = new BlockPos(pos1.getX(), pos1.getY(), (world.owner.unitsPerBlock - Math.abs(pos.getZ() % world.owner.unitsPerBlock)));
						else if (pos.getZ() > world.owner.unitsPerBlock - 1)
							pos1 = new BlockPos(pos1.getX(), pos1.getY(), world.owner.unitsPerBlock - ((Math.abs(pos.getZ() % world.owner.unitsPerBlock))) - world.owner.unitsPerBlock);
//						return ((UnitTileEntity) westTE).world.getBlockState(pos1);
						posInFakeWorld = pos1;
						posInRealWorld = westPos;
						stateInRealWorld = westState;
						teInRealWorld = westTE;
					} else {
						posInFakeWorld = pos;
						posInRealWorld = westPos;
						stateInRealWorld = Blocks.BEDROCK.getDefaultState();
						teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
					}
				} else {
					posInFakeWorld = pos;
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
				posInFakeWorld = pos;
				posInRealWorld = westPos;
				stateInRealWorld = westState.isAir(world.owner.getWorld(), westPos) ? westState : Blocks.BARRIER.getDefaultState();
				teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				return;
			} else {
				posInFakeWorld = pos;
				posInRealWorld = westPos;
				stateInRealWorld = Blocks.BEDROCK.getDefaultState();
				teInRealWorld = world.owner.getWorld().getTileEntity(westPos);
				return;
			}
		}
	}
}
