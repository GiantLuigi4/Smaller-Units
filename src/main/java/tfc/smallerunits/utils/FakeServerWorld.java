package tfc.smallerunits.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.ServerWorldInfo;
import tfc.smallerunits.block.UnitTileEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executor;

public class FakeServerWorld extends ServerWorld {
	public FakeServerWorld(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> p_i241885_5_, DimensionType p_i241885_6_, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_) {
		super(p_i241885_1_, p_i241885_2_, p_i241885_3_, p_i241885_4_, p_i241885_5_, p_i241885_6_, p_i241885_7_, p_i241885_8_, p_i241885_9_, p_i241885_10_, p_i241885_12_, p_i241885_13_);
	}
	
	private boolean hasInit = false;
	
	//Due to usage of theUnsafe, all constructor and field declaration code must be in a method
	public void init() {
		if (!hasInit) {
			hasInit = true;
			field_241102_C_ = null;
			blockMap = new HashMap<>();
			tileEntityPoses = new ArrayList<>();
			chunk = new FakeChunk(this);
			FakeServerWorld world = this;
			lightManager = new WorldLightManager(
					new IChunkLightProvider() {
						@Nullable
						@Override
						public IBlockReader getChunkForLight(int chunkX, int chunkZ) {
							return world;
						}
						
						@Override
						public IBlockReader getWorld() {
							return world;
						}
					},
					true, true
			);
			
			//MC code
			pendingBlockTicks = new ServerTickList<>(this, (p_205341_0_) -> {
				return p_205341_0_ == null || p_205341_0_.getDefaultState().isAir();
			}, Registry.BLOCK::getKey, this::tickBlock);
			pendingFluidTicks = new ServerTickList<>(this, (p_205774_0_) -> {
				return p_205774_0_ == null || p_205774_0_ == Fluids.EMPTY;
			}, Registry.FLUID::getKey, this::tickFluid);
		}
	}
	
	@Override
	public long getGameTime() {
		return owner.getWorld().getGameTime();
	}
	
	@Override
	public long getDayTime() {
		return owner.getWorld().getDayTime();
	}
	
	@Override
	public boolean isDaytime() {
		return owner.getWorld().isDaytime();
	}
	
	@Override
	public boolean isNightTime() {
		return owner.world.isNightTime();
	}
	
	@Override
	//TODO: make this account for tiny blocks blocking the way
	public boolean canSeeSky(BlockPos blockPosIn) {
		return owner.getWorld().canSeeSky(owner.getPos());
	}
	
	@Override
	public boolean canBlockSeeSky(BlockPos pos) {
		return canSeeSky(pos);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blockMap.getOrDefault(pos, new SmallUnit(pos, Blocks.AIR.getDefaultState())).state;
	}
	
	@Override
	//TODO: make this linearly interpolate through neighboring biomes
	public Biome getBiome(BlockPos pos) {
		return owner.getWorld().getBiome(pos);
	}
	
	public HashMap<BlockPos, SmallUnit> blockMap;
	public ArrayList<BlockPos> tileEntityPoses;
	
	private IChunk chunk;
	
	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		this.isRemote = this.owner.getWorld().isRemote;
		return chunk;
	}
	
	@Override
	public Chunk getChunk(int chunkX, int chunkZ) {
		return null;
	}
	
	public WorldLightManager lightManager;
	
	@Override
	public WorldLightManager getLightManager() {
		this.isRemote = this.owner.getWorld().isRemote;
		return lightManager;
	}
	
	public UnitTileEntity owner;
	
	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		this.isRemote = this.owner.getWorld().isRemote;
		owner.getWorld().playSound(player, owner.getPos().getX() + (x / (float) owner.unitsPerBlock), owner.getPos().getY() + (y / (float) owner.unitsPerBlock), owner.getPos().getZ() + (z / (float) owner.unitsPerBlock), soundIn, category, volume / owner.unitsPerBlock, pitch);
	}
	
	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		this.isRemote = this.owner.getWorld().isRemote;
		if (!isRemote) {
			owner.getWorld().getServer().getPlayerList()
					.sendToAllNearExcept(
							player,
							(double) owner.getPos().getX() + (pos.getX() / (float) owner.unitsPerBlock),
							(double) owner.getPos().getY() + (pos.getY() / (float) owner.unitsPerBlock),
							(double) owner.getPos().getZ() + (pos.getZ() / (float) owner.unitsPerBlock),
							64.0D, owner.getWorld().getDimensionKey(),
							new SPlaySoundEventPacket(type, pos, data, false)
					);
		} else {
			owner.getWorld().playEvent(player, type, owner.getPos(), data);
		}
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return chunk.getTileEntity(pos);
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		if (state == null) return false;
		this.isRemote = this.owner.getWorld().isRemote;
		chunk.setBlockState(pos, state, false);
		lightManager.checkBlock(pos);
		return true;
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}
	
	@Override
	public ServerTickList<Block> getPendingBlockTicks() {
		return super.getPendingBlockTicks();
	}
	
	@Override
	public ServerTickList<Fluid> getPendingFluidTicks() {
		return super.getPendingFluidTicks();
	}
}
