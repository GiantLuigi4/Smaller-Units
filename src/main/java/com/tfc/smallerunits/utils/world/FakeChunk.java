package com.tfc.smallerunits.utils.world;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

public class FakeChunk extends Chunk {
	private FakeServerWorld world;
	
	public FakeChunk(World worldIn, ChunkPos chunkPosIn, BiomeContainer biomeContainerIn, FakeServerWorld world) {
		super(worldIn, chunkPosIn, biomeContainerIn);
		this.world = world;
	}
	
	public FakeChunk(World worldIn, ChunkPos chunkPosIn, BiomeContainer biomeContainerIn, UpgradeData upgradeDataIn, ITickList<Block> tickBlocksIn, ITickList<Fluid> tickFluidsIn, long inhabitedTimeIn, @Nullable ChunkSection[] sectionsIn, @Nullable Consumer<Chunk> postLoadConsumerIn, FakeServerWorld world) {
		super(worldIn, chunkPosIn, biomeContainerIn, upgradeDataIn, tickBlocksIn, tickFluidsIn, inhabitedTimeIn, sectionsIn, postLoadConsumerIn);
		this.world = world;
	}
	
	public FakeChunk(World worldIn, ChunkPrimer primer, FakeServerWorld world) {
		super(worldIn, primer);
		this.world = world;
	}
	
	@Override
	public Set<BlockPos> getTileEntitiesPos() {
		return ImmutableSet.copyOf(world.tileEntityPoses);
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return world.getTileEntity(pos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}
	
	@Override
	public FluidState getFluidState(int bx, int by, int bz) {
		return world.getFluidState(new BlockPos(bx, by, bz));
	}
	
	@Override
	public void addTileEntity(TileEntity tileEntityIn) {
		world.addTileEntity(tileEntityIn);
	}
	
	@Override
	public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		world.setTileEntity(pos, tileEntityIn);
	}
	
	@Override
	public void addTileEntity(CompoundNBT nbt) {
		world.chunk.addTileEntity(nbt);
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		world.setBlockState(pos, state);
		return state;
	}
	
	@Nullable
	@Override
	public WorldLightManager getWorldLightManager() {
		return world.lightManager;
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos, CreateEntityType creationMode) {
		return world.getTileEntity(pos);
	}
	
	@Nullable
	@Override
	public CompoundNBT getTileEntityNBT(BlockPos pos) {
		return getTileEntity(pos).serializeNBT();
	}
}
