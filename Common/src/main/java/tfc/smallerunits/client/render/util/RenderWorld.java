package tfc.smallerunits.client.render.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.plat.itf.IMayManageModelData;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;

public class RenderWorld implements BlockAndTintGetter, IMayManageModelData {
	Level lvl;
	//	BlockState[] states;
	BlockPos minPos;
	int upb;
	LevelChunk chunk;
	
	@Override
	public Level getActual() {
		return lvl;
	}
	
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
		if (
				chunk.getPos().getMaxBlockX() >= pPos.getX() &&
						chunk.getPos().getMinBlockX() <= pPos.getX() &&
//						lvl.getMaxBuildHeight() >= pos.getY() &&
//						lvl.getMinBuildHeight() <= pos.getY() &&
						chunk.getPos().getMaxBlockZ() >= pPos.getZ() &&
						chunk.getPos().getMinBlockZ() <= pPos.getZ()
		) {
			return chunk.getBlockEntity(pPos);
		}
		return lvl.getBlockEntity(pPos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		// TODO: cache states
		return getState(pos);
	}

	public boolean inRange(int v0, int v1) {
		int d = v0 - v1;
		if (d < 0) d = -d;
		return d <= 1;
	}

	LevelChunk prevChunk = null;
	long preCrd = Long.MIN_VALUE;

	protected BlockState getState(BlockPos pos) {
		if (
				chunk.getPos().getMaxBlockX() >= pos.getX() &&
						chunk.getPos().getMinBlockX() <= pos.getX() &&
						chunk.getPos().getMaxBlockZ() >= pos.getZ() &&
						chunk.getPos().getMinBlockZ() <= pos.getZ()
		) {
			BasicVerticalChunk bvc = (BasicVerticalChunk) chunk;
			if (
					pos.getY() >= (bvc.yPos * 16) &&
							pos.getY() < (bvc.yPos * 16) + 16
			)
				return bvc.getBlockState$(pos);
			return ((BasicVerticalChunk) chunk).getBlockState(pos);
		}
		ChunkPos cp = new ChunkPos(pos);
		if (preCrd != cp.toLong()) {
			preCrd = cp.toLong();
			prevChunk = lvl.getChunkAt(pos);
		}
		return prevChunk.getBlockState(pos);
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

	public Region getRegion() {
		return ((ITickerLevel) lvl).getRegion();
	}
}
