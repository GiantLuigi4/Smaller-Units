package com.tfc.smallerunits.utils;

import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.registry.Deferred;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.concurrent.DelegatedTaskExecutor;
import net.minecraft.util.concurrent.ITaskExecutor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.ServerWorldLightManager;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public class FakeServerWorld extends ServerWorld {
	private static final WorldBorder border = new WorldBorder();
	private static final Unsafe theUnsafe;
	private static final LongSet forcedChunks = LongSets.singleton(new ChunkPos(new BlockPos(0, 0, 0)).asLong());
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public HashMap<BlockPos, SmallUnit> blockMap;
	public ArrayList<SmallUnit> tileEntityChanges;
	public ArrayList<BlockPos> tileEntityPoses;
	public WorldLightManager lightManager;
	public UnitTileEntity owner;
	private boolean hasInit = false;
	private boolean isFirstTick;
	private IChunk chunk;
	private Profiler blankProfiler;
	
	public FakeServerWorld(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> p_i241885_5_, DimensionType p_i241885_6_, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_) {
		super(p_i241885_1_, p_i241885_2_, p_i241885_3_, p_i241885_4_, p_i241885_5_, p_i241885_6_, p_i241885_7_, p_i241885_8_, p_i241885_9_, p_i241885_10_, p_i241885_12_, p_i241885_13_);
	}
	
	@Override
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(this.owner.getPos())) {
					if (context.teInRealWorld != null) {
						((UnitTileEntity) context.teInRealWorld).world.setTileEntity(context.posInFakeWorld, tileEntityIn);
						return;
					}
				}
			}
		}
		SmallUnit unit = blockMap.getOrDefault(pos, new SmallUnit(pos, Blocks.AIR.getDefaultState()));
		if (unit.tileEntity != null) loadedTileEntityList.remove(tileEntityIn);
		if (tileEntityIn.getType().isValidBlock(unit.state.getBlock())) {
			unit.tileEntity = tileEntityIn;
			tileEntityIn.setWorldAndPos(this, pos);
			loadedTileEntityList.add(unit.tileEntity);
			if (!blockMap.containsKey(pos)) blockMap.put(pos, unit);
		} else {
			unit.tileEntity = null;
		}
		tileEntityChanges.add(unit);
	}
	
	@Override
	public void removeTileEntity(BlockPos pos) {
		SmallUnit unit = blockMap.getOrDefault(pos, new SmallUnit(pos, Blocks.AIR.getDefaultState()));
		loadedTileEntityList.remove(unit.tileEntity);
		tileEntityChanges.add(unit);
		unit.tileEntity = null;
	}
	
	//Due to usage of theUnsafe, all constructor and field declaration code must be in a method
	public void init(UnitTileEntity owner) {
		if (!hasInit) {
			tileEntityChanges = new ArrayList<>();
			this.owner = owner;
			hasInit = true;
			field_241102_C_ = null;
			blockMap = new HashMap<>();
			tileEntityPoses = new ArrayList<>();
			chunk = new FakeChunk(this);
			FakeServerWorld world = this;
			field_241102_C_ = FakeServerChunkProvider.getProvider(this);
			DelegatedTaskExecutor<Runnable> delegatedtaskexecutor1 = DelegatedTaskExecutor.create(Runnable::run, "light");
			ITaskExecutor<Unit> unitExecutor = ITaskExecutor.inline("idk", unit -> {
			});
			ITaskExecutor<ChunkTaskPriorityQueueSorter.FunctionEntry<Runnable>> itaskexecutor = ITaskExecutor.inline("su_world", (entry) -> entry.task.apply(unitExecutor).run());
			lightManager = new ServerWorldLightManager(
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
					this.getChunkProvider().chunkManager,
					true, delegatedtaskexecutor1, itaskexecutor
			);
			
			//MC code
			pendingBlockTicks = new ServerTickList<>(this, (p_205341_0_) -> {
				return p_205341_0_ == null || p_205341_0_.getDefaultState().isAir();
			}, Registry.BLOCK::getKey, this::tickBlock);
			pendingFluidTicks = new ServerTickList<>(this, (p_205774_0_) -> {
				return p_205774_0_ == null || p_205774_0_ == Fluids.EMPTY;
			}, Registry.FLUID::getKey, this::tickFluid);
			field_241103_E_ = new FakeServerWorldInfo(this);
			worldInfo = field_241103_E_;
			rand = new Random();
			blankProfiler = new Profiler(() -> 0, () -> 0, false);
			profiler = () -> blankProfiler;
			worldBorder = border;
			isFirstTick = true;
			blockEventQueue = new ObjectLinkedOpenHashSet<>();
			players = new ArrayList<>();
			entitiesToAdd = new PriorityQueue<>();
			entitiesByUuid = new Object2ObjectLinkedOpenHashMap<>();
			entitiesById = new Int2ObjectArrayMap<>();
			try {
				theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(ObfuscationReflectionHelper.findField(World.class, "field_147483_b")), Collections.newSetFromMap(new IdentityHashMap<>()));
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
			tickableTileEntities = new ArrayList<>();
			loadedTileEntityList = new ArrayList<>();
			addedTileEntityList = new ArrayList<>();
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
	public LongSet getForcedChunks() {
		return forcedChunks;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(this.owner.getPos())) {
					return ((UnitTileEntity) context.teInRealWorld).world.getBlockState(context.posInFakeWorld);
				}
			} else if (context.stateInRealWorld.equals(Blocks.BEDROCK.getDefaultState())) {
				return Blocks.BEDROCK.getDefaultState();
			} else if (context.stateInRealWorld.equals(Blocks.BARRIER.getDefaultState())) {
				return Blocks.BARRIER.getDefaultState();
			}
		}
		return blockMap.getOrDefault(pos, new SmallUnit(pos, Blocks.AIR.getDefaultState())).state;
	}
	
	@Override
	public void func_241123_a_(boolean p_241123_1_, boolean p_241123_2_) {
	}
	
	@Override
	//TODO: make this linearly interpolate through neighboring biomes
	public Biome getBiome(BlockPos pos) {
		return owner.getWorld().getBiome(pos);
	}
	
	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		return chunk;
	}
	
	@Override
	public void tick(BooleanSupplier hasTimeLeft) {
		if (isFirstTick) {
			raids = new RaidManager(this);
			dimension = owner.world.dimension;
			dimensionType = owner.world.dimensionType;
			isFirstTick = false;
			server = owner.getWorld().getServer();
			this.isRemote = this.owner.getWorld().isRemote;
		}
		blankProfiler.startTick();
		super.tick(hasTimeLeft);
		blankProfiler.endTick();
		
		//Random Ticks
		for (int i = 0; i < Math.max(1, owner.unitsPerBlock / 8); i++) {
			int x = rand.nextInt(owner.unitsPerBlock);
			int y = rand.nextInt(owner.unitsPerBlock);
			int z = rand.nextInt(owner.unitsPerBlock);
			BlockPos randTickPos = new BlockPos(x, y, z);
			BlockState state = getBlockState(randTickPos);
			if (state.ticksRandomly()) {
				state.randomTick(this, randTickPos, rand);
			}
		}
		
		ArrayList<TileEntity> toRemove = new ArrayList<>();
		for (SmallUnit unit : tileEntityChanges) {
			if (unit.tileEntity != null) {
				if (!tickableTileEntities.contains(unit.tileEntity) && unit.tileEntity instanceof ITickableTileEntity)
					tickableTileEntities.add(unit.tileEntity);
				if (!loadedTileEntityList.contains(unit.tileEntity)) loadedTileEntityList.add(unit.tileEntity);
			}
			unit.oldTE = unit.tileEntity;
		}
		for (TileEntity tileEntity : loadedTileEntityList) {
			if (!tileEntity.getType().isValidBlock(getBlockState(tileEntity.getPos()).getBlock())) {
				toRemove.add(tileEntity);
			}
		}
		loadedTileEntityList.removeAll(toRemove);
		tickableTileEntities.removeAll(toRemove);
		tileEntityChanges.clear();
	}
	
	@Override
	public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
		return lightManager.getLightEngine(lightTypeIn).getLightFor(blockPosIn);
	}
	
	@Override
	public int getLightSubtracted(BlockPos blockPosIn, int amount) {
		return lightManager.getLightSubtracted(blockPosIn, amount);
	}
	
	@Override
	public int getLightValue(BlockPos pos) {
		return
				Math.max(
						lightManager.getLightEngine(LightType.BLOCK).getLightFor(pos),
						lightManager.getLightEngine(LightType.SKY).getLightFor(pos)
				);
	}
	
	@Override
	public Chunk getChunk(int chunkX, int chunkZ) {
		return new Chunk(this, new ChunkPos(chunkX, chunkZ), new BiomeContainer(new ObjectIntIdentityMap<>()));
	}
	
	@Override
	public WorldLightManager getLightManager() {
		return lightManager;
	}
	
	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		owner.getWorld().playSound(player, owner.getPos().getX() + (x / (float) owner.unitsPerBlock), owner.getPos().getY() + (y / (float) owner.unitsPerBlock), owner.getPos().getZ() + (z / (float) owner.unitsPerBlock), soundIn, category, volume / owner.unitsPerBlock, pitch);
	}
	
	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
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
//		return chunk.getTileEntity(pos);
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(this.owner.getPos())) {
					if (context.teInRealWorld != null) {
						return ((UnitTileEntity) context.teInRealWorld).world.getTileEntity(context.posInFakeWorld);
					}
				}
			}
		}
		return blockMap.getOrDefault(pos, new SmallUnit(pos, Blocks.AIR.getDefaultState())).tileEntity;
	}
	
	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (recursionLeft < 0) return false;
		if (context.stateInRealWorld != null) {
			if (!context.posInRealWorld.equals(owner.getPos())) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						return ((UnitTileEntity) context.teInRealWorld).world.setBlockState(context.posInFakeWorld, state, flags, recursionLeft - 1);
					}
					return false;
				} else if (context.stateInRealWorld.isAir(owner.getWorld(), context.posInRealWorld)) {
					owner.getWorld().setBlockState(context.posInRealWorld, Deferred.UNIT.get().getDefaultState());
					UnitTileEntity tileEntity = new UnitTileEntity();
					owner.getWorld().setTileEntity(context.posInRealWorld, tileEntity);
					tileEntity.unitsPerBlock = this.owner.unitsPerBlock;
				}
			}
		}
		
		owner.markDirty();
		owner.getWorld().notifyBlockUpdate(owner.getPos(), state, state, 3);
		
		{
			IChunk chunk = this.chunk;
			
			pos = pos.toImmutable(); // Forge - prevent mutable BlockPos leaks
			net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
			if (this.captureBlockSnapshots && !this.isRemote) {
				blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.create(this.dimension, this, pos, flags);
				this.capturedBlockSnapshots.add(blockSnapshot);
			}
			
			BlockState old = getBlockState(pos);
			int oldLight = old.getLightValue(this, pos);
			int oldOpacity = old.getOpacity(this, pos);
			
			if (!state.getFluidState().isEmpty()) {
				state.onBlockAdded(this, pos, old, false);
			}
			
			BlockState blockstate = chunk.setBlockState(pos, state, (flags & 64) != 0);
			if (blockstate == null) {
				if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
				return false;
			} else {
				BlockState blockstate1 = this.getBlockState(pos);
				if ((flags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getOpacity(this, pos) != oldOpacity || blockstate1.getLightValue(this, pos) != oldLight || blockstate1.isTransparent() || blockstate.isTransparent())) {
					this.getProfiler().startSection("queueCheckLight");
					try {
						this.getChunkProvider().getLightManager().checkBlock(pos);
					} catch (Throwable ignored) {
					}
					this.getProfiler().endSection();
				}
				
				if (!state.getFluidState().isEmpty()) {
					if (state.getFluidState().getBlockState().equals(state.getBlockState())) {
						Fluid fluid = state.getFluidState().getFluid();
						for (Direction dir : Direction.values()) {
							this.getBlockState(pos.offset(dir)).neighborChanged(this, pos.offset(dir), state.getBlock(), pos, false);
						}
						getPendingFluidTicks().scheduleTick(pos, fluid, fluid.getTickRate(this));
					}
				}
				
				this.markAndNotifyBlock(pos, chunk, blockstate, state, flags, recursionLeft);
				
				if (state.equals(Blocks.AIR.getDefaultState())) {
					this.blockMap.remove(pos);
				}
				
				return true;
			}
		}
	}
	
	public void markAndNotifyBlock(BlockPos pos, @Nullable IChunk chunk, BlockState blockstate, BlockState state, int flags, int recursionLeft) {
		Block block = state.getBlock();
		BlockState blockstate1 = getBlockState(pos);
		{
			{
				if (blockstate1 == state) {
					if (blockstate != blockstate1) {
						this.markBlockRangeForRenderUpdate(pos, blockstate, blockstate1);
					}
					
					if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (this.isRemote)) {
						this.notifyBlockUpdate(pos, blockstate, state, flags);
					}
					
					if ((flags & 1) != 0) {
						this.func_230547_a_(pos, blockstate.getBlock());
						if (!this.isRemote && state.hasComparatorInputOverride()) {
							this.updateComparatorOutputLevel(pos, block);
						}
					}
					
					if ((flags & 16) == 0 && recursionLeft > 0) {
						int i = flags & -34;
						blockstate.updateDiagonalNeighbors(this, pos, i, recursionLeft - 1);
						state.updateNeighbours(this, pos, i, recursionLeft - 1);
						state.updateDiagonalNeighbors(this, pos, i, recursionLeft - 1);
						
						this.notifyNeighborsOfStateChange(pos, blockstate.getBlock());
					}
					
					this.onBlockStateChange(pos, blockstate, blockstate1);
				}
			}
		}
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}
	
	@Override
	public IProfiler getProfiler() {
		return blankProfiler;
	}
	
	@Override
	public WorldBorder getWorldBorder() {
		return border;
	}
	
	@Override
	public GameRules getGameRules() {
		return owner.getWorld().getGameRules();
	}
	
	@Override
	public DimensionType getDimensionType() {
		return owner.getWorld().getDimensionType();
	}
	
	@Override
	public RegistryKey<World> getDimensionKey() {
		return owner.getWorld().getDimensionKey();
	}
	
	@Override
	public ServerTickList<Block> getPendingBlockTicks() {
		return super.getPendingBlockTicks();
	}
	
	@Override
	public ServerTickList<Fluid> getPendingFluidTicks() {
		return super.getPendingFluidTicks();
	}
	
	@Override
	public ServerChunkProvider getChunkProvider() {
		return super.getChunkProvider();
	}
}
