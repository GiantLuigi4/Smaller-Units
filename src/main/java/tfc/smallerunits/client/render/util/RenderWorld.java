package tfc.smallerunits.client.render.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class RenderWorld implements BlockAndTintGetter {
	Level lvl;
	//	BlockState[] states;
	BlockPos minPos;
	int upb;
	LevelChunk chunk;
	
	//	public RenderWorld(Level lvl, BlockState[] states, BlockPos minPos, int upb) {
	public RenderWorld(Level lvl, BlockPos minPos, int upb) {
		this.lvl = lvl;
//		this.states = states;
		this.minPos = minPos;
		this.upb = upb;
		chunk = lvl.getChunkAt(minPos);
	}
	
	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return lvl.getShade(pDirection, pShade);
	}
	
	@Override
	public LevelLightEngine getLightEngine() {
		return lvl.getLightEngine();
	}
	
	@Override
	public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
		return lvl.getBlockTint(pBlockPos, pColorResolver);
	}
	
	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pPos) {
		return lvl.getBlockEntity(pPos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos p_45571_) {
//		BlockPos offset = p_45571_.offset(-minPos.getX(), -minPos.getY(), -minPos.getZ());
//		int x = offset.getX();
//		int y = offset.getY();
//		int z = offset.getZ();
//		if (x >= 0 && y >= 0 && z >= 0 && x < upb && y < upb && z < upb) {
//			int indx = (((x * upb) + y) * upb) + z;
//			return states[indx];
//		}
		if (
				chunk.getPos().getMaxBlockX() >= p_45571_.getX() &&
						chunk.getPos().getMinBlockX() <= p_45571_.getX() &&
						lvl.getMaxBuildHeight() >= p_45571_.getY() &&
						lvl.getMinBuildHeight() <= p_45571_.getY() &&
						chunk.getPos().getMaxBlockZ() >= p_45571_.getZ() &&
						chunk.getPos().getMinBlockZ() <= p_45571_.getZ()
		)
			return chunk.getBlockState(p_45571_);
		return lvl.getBlockState(p_45571_);
	}
	
	@Override
	public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
		return lvl.getBrightness(pLightType, pBlockPos);
	}
	
	@Override
	public int getRawBrightness(BlockPos pBlockPos, int pAmount) {
		return lvl.getRawBrightness(pBlockPos, pAmount);
	}
	
	@Override
	public FluidState getFluidState(BlockPos pPos) {
		return getBlockState(pPos).getFluidState();
	}
	
	@Override
	public int getHeight() {
		return lvl.getHeight();
	}
	
	@Override
	public int getMinBuildHeight() {
		return lvl.getMinBuildHeight();
	}
}
