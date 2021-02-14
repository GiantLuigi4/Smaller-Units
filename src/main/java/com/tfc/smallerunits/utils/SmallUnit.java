package com.tfc.smallerunits.utils;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class SmallUnit {
	public BlockPos pos;
	public BlockState state;
	public TileEntity tileEntity;
	public TileEntity oldTE;
	
	public SmallUnit(BlockPos pos, BlockState state) {
		this.pos = pos;
		this.state = state;
	}
	
	public SmallUnit(BlockPos pos, BlockState state, TileEntity tileEntity) {
		this.pos = pos;
		this.state = state;
		this.tileEntity = tileEntity;
	}
}
