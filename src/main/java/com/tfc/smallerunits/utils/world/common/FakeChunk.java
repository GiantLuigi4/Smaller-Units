package com.tfc.smallerunits.utils.world.common;

import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.world.client.FakeClientWorld;
import com.tfc.smallerunits.utils.world.server.FakeServerWorld;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Consumer;

public class FakeChunk extends Chunk {
	private World world;
	
	public FakeChunk(World worldIn, ChunkPos chunkPosIn, BiomeContainer biomeContainerIn, World world) {
		super(worldIn, chunkPosIn, biomeContainerIn);
		this.world = world;
	}
	
	public FakeChunk(World worldIn, ChunkPos chunkPosIn, BiomeContainer biomeContainerIn, UpgradeData upgradeDataIn, ITickList<Block> tickBlocksIn, ITickList<Fluid> tickFluidsIn, long inhabitedTimeIn, @Nullable ChunkSection[] sectionsIn, @Nullable Consumer<Chunk> postLoadConsumerIn, World world) {
		super(worldIn, chunkPosIn, biomeContainerIn, upgradeDataIn, tickBlocksIn, tickFluidsIn, inhabitedTimeIn, sectionsIn, postLoadConsumerIn);
		this.world = world;
	}
	
	public FakeChunk(World worldIn, ChunkPrimer primer, World world) {
		super(worldIn, primer);
		this.world = world;
	}
	
	@Override
	public Set<BlockPos> getTileEntitiesPos() {
		ObjectArraySet<BlockPos> tileEntityPoses = new ObjectArraySet<>();
		for (TileEntity tileEntity : world.loadedTileEntityList) {
			tileEntityPoses.add(tileEntity.getPos());
		}
		return tileEntityPoses;
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
		TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(nbt.getString("id")));
		if (type == null) return;
		TileEntity te = type.create();
		if (te == null) return;
		te.read(world.getBlockState(new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"))), nbt);
		world.addTileEntity(te);
	}
	
	@Nullable
	@Override
	public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
		Long2ObjectLinkedOpenHashMap<SmallUnit> blockMap;
		if (world instanceof FakeServerWorld) {
			blockMap = ((FakeServerWorld) world).blockMap;
		} else {
			blockMap = ((FakeClientWorld) world).blockMap;
		}
		if (blockMap.containsKey(pos.toLong())) {
			blockMap.get(pos.toLong()).state = state;
		} else {
			blockMap.put(pos.toLong(), new SmallUnit(pos, state));
		}
		return state;
	}
	
	@Nullable
	@Override
	public WorldLightManager getWorldLightManager() {
		return world.getLightManager();
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
