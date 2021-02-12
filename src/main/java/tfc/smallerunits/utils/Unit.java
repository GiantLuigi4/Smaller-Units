package tfc.smallerunits.utils;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class Unit {
	public BlockPos pos;
	public BlockState state;
	public TileEntity tileEntity;
	
	public Unit(BlockPos pos, BlockState state) {
		this.pos = pos;
		this.state = state;
	}
	
	public Unit(BlockPos pos, BlockState state, TileEntity tileEntity) {
		this.pos = pos;
		this.state = state;
		this.tileEntity = tileEntity;
	}
}
