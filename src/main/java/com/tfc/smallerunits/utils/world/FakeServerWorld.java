package com.tfc.smallerunits.utils.world;

import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.ExternalUnitInteractionContext;
import com.tfc.smallerunits.utils.SmallUnit;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.particles.IParticleData;
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
		if (tileEntityIn != null && tileEntityIn.getType().isValidBlock(unit.state.getBlock())) {
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
			lightManager = new FakeLightingManager(
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
					true, delegatedtaskexecutor1, itaskexecutor,
					this
			);
			
			//MC code
			pendingBlockTicks = new FakeServerTickList<>(this, (p_205341_0_) -> {
				return p_205341_0_ == null || p_205341_0_.getDefaultState().isAir();
			}, Registry.BLOCK::getKey, this::tickBlock, true);
			pendingFluidTicks = new FakeServerTickList<>(this, (p_205774_0_) -> {
				return p_205774_0_ == null || p_205774_0_ == Fluids.EMPTY;
			}, Registry.FLUID::getKey, this::tickFluid, false);
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
		return owner.getWorld() == null ? 0 : owner.getWorld().getGameTime();
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
	
	public boolean isRendering = false;
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(this.owner.getPos())) {
					if (!isRendering) {
						return ((UnitTileEntity) context.teInRealWorld).world.getBlockState(context.posInFakeWorld);
					} else {
						return ((UnitTileEntity) context.teInRealWorld).world.getBlockState(context.posInFakeWorld).getFluidState().getBlockState();
					}
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
	public RecipeManager getRecipeManager() {
		return owner.getWorld().getRecipeManager();
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
			dimension = owner.getWorld().dimension;
			dimensionType = owner.getWorld().dimensionType;
			raids = new RaidManager(this);
			isFirstTick = false;
			server = owner.getWorld().getServer();
			this.isRemote = this.owner.getWorld().isRemote;
		}
		
		if (this.isRemote) {
			for (SmallUnit value : this.blockMap.values()) {
				if (value.tileEntity instanceof ITickableTileEntity)
					((ITickableTileEntity) value.tileEntity).tick();
			}
			return;
		}
		
		for (SmallUnit value : blockMap.values()) {
			if (value.tileEntity != null) {
				if (!loadedTileEntityList.contains(value.tileEntity)) {
					loadedTileEntityList.add(value.tileEntity);
					tileEntityChanges.add(value);
				}
				
				if (isRemote) {
					if (value.tileEntity instanceof ITickableTileEntity) {
						try {
							((ITickableTileEntity) value.tileEntity).tick();
						} catch (Throwable ignored) {
						}
					}
				}
			}
		}
		
		if (!this.isRemote)
			lightManager.tick(100, false, true);
		
		blankProfiler.startTick();
		super.tick(hasTimeLeft);
		blankProfiler.endTick();
		
		//Random Ticks
		for (int i = 0; i < Math.max(1, owner.unitsPerBlock / 4) * owner.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED); i++) {
			int x = rand.nextInt(owner.unitsPerBlock);
			int y = rand.nextInt(owner.unitsPerBlock);
			int z = rand.nextInt(owner.unitsPerBlock);
			BlockPos randTickPos = new BlockPos(x, y + 64, z);
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
	public Difficulty getDifficulty() {
		return owner.getWorld().getDifficulty();
	}
	
	@Override
	public void sendQueuedBlockEvents() {
		if (this.isRemote) return;
		super.sendQueuedBlockEvents();
	}
	
	//TODO: cm integration
	@Override
	public <T extends IParticleData> int spawnParticle(T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
		if (owner.getWorld() instanceof ServerWorld) {
			return ((ServerWorld) owner.getWorld()).spawnParticle(type,
					owner.getPos().getX() + (posX / owner.unitsPerBlock),
					owner.getPos().getY() + ((posY - 64) / owner.unitsPerBlock),
					owner.getPos().getZ() + (posZ / owner.unitsPerBlock),
					particleCount / owner.unitsPerBlock, xOffset / owner.unitsPerBlock, yOffset / owner.unitsPerBlock, zOffset / owner.unitsPerBlock, speed / owner.unitsPerBlock
			);
		}
		return super.spawnParticle(type,
				owner.getPos().getX() + (posX / owner.unitsPerBlock),
				owner.getPos().getY() + ((posY - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + (posZ / owner.unitsPerBlock),
				particleCount / owner.unitsPerBlock, xOffset / owner.unitsPerBlock, yOffset / owner.unitsPerBlock, zOffset / owner.unitsPerBlock, speed / owner.unitsPerBlock
		);
	}
	
	//TODO: cm integration
	@Override
	public <T extends IParticleData> boolean spawnParticle(ServerPlayerEntity player, T type, boolean longDistance, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
		if (owner.getWorld() instanceof ServerWorld) {
			return ((ServerWorld) owner.getWorld()).spawnParticle(player, type, longDistance,
					owner.getPos().getX() + (posX / owner.unitsPerBlock),
					owner.getPos().getY() + ((posY - 64) / owner.unitsPerBlock),
					owner.getPos().getZ() + (posZ / owner.unitsPerBlock),
					particleCount / owner.unitsPerBlock, xOffset / owner.unitsPerBlock, yOffset / owner.unitsPerBlock, zOffset / owner.unitsPerBlock, speed / owner.unitsPerBlock
			);
		}
		return super.spawnParticle(
				player, type, longDistance,
				owner.getPos().getX() + (posX / owner.unitsPerBlock),
				owner.getPos().getY() + ((posY - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + (posZ / owner.unitsPerBlock),
				particleCount / owner.unitsPerBlock, xOffset / owner.unitsPerBlock, yOffset / owner.unitsPerBlock, zOffset / owner.unitsPerBlock, speed / owner.unitsPerBlock
		);
	}
	
	@Override
	public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		owner.getWorld().addParticle(particleData,
				owner.getPos().getX() + (x / owner.unitsPerBlock),
				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + (z / owner.unitsPerBlock),
				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
		);
	}
	
	@Override
	public void addParticle(IParticleData particleData, boolean forceAlwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		owner.getWorld().addParticle(particleData, forceAlwaysRender,
				owner.getPos().getX() + (x / owner.unitsPerBlock),
				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + (z / owner.unitsPerBlock),
				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
		);
	}
	
	@Override
	public void addOptionalParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		owner.getWorld().addOptionalParticle(particleData,
				owner.getPos().getX() + (x / owner.unitsPerBlock),
				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + (z / owner.unitsPerBlock),
				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
//				xSpeed,ySpeed,zSpeed
		);
	}
	
	@Override
	public void addOptionalParticle(IParticleData particleData, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		owner.getWorld().addOptionalParticle(particleData, ignoreRange,
				owner.getPos().getX() + (x / owner.unitsPerBlock),
				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + (z / owner.unitsPerBlock),
				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
		);
	}
	
	@Override
	public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
		if (lightTypeIn.equals(LightType.BLOCK)) {
			ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, blockPosIn);
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (context.teInRealWorld != null) {
							return ((UnitTileEntity) context.teInRealWorld).world.getLightFor(lightTypeIn, context.posInFakeWorld);
						}
					}
				}
			}
		}
		
		if (lightTypeIn.equals(LightType.BLOCK)) {
			return Math.max(
					((FakeLightingManager) lightManager).getBlockLight(blockPosIn.offset(Direction.DOWN, 64)),
					owner.getWorld().getLightFor(lightTypeIn, owner.getPos())
			);
		} else {
			return Math.max(
					lightManager.getLightEngine(lightTypeIn).getLightFor(blockPosIn),
					owner.getWorld().getLightFor(lightTypeIn, owner.getPos())
			);
		}
	}
	
	@Override
	public int getLightSubtracted(BlockPos blockPosIn, int amount) {
		return Math.max(
				lightManager.getLightSubtracted(blockPosIn, amount),
				owner.getWorld().getLightSubtracted(owner.getPos(), amount)
		);
	}
	
	@Override
	public int getLightValue(BlockPos pos) {
		return
				Math.max(
						getLightFor(LightType.BLOCK, pos),
						getLightFor(LightType.SKY, pos)
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
		owner.getWorld().playSound(player, owner.getPos().getX() + (x / (float) owner.unitsPerBlock), owner.getPos().getY() + ((y - 64) / (float) owner.unitsPerBlock), owner.getPos().getZ() + (z / (float) owner.unitsPerBlock), soundIn, category, (volume / owner.unitsPerBlock), pitch);
	}
	
	/**
	 * {@link net.minecraft.client.renderer.WorldRenderer#playEvent}
	 */
	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		this.isRemote = owner.getWorld().isRemote;
		Random random = rand;
		switch (type) {
			case 1000:
				this.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 1.2F);
				break;
			case 1001:
				this.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.2F);
				break;
			case 1002:
				this.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.2F);
				break;
			//TODO:1003-1004
			case 1005:
				this.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1006:
				this.playSound(null, pos, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1007:
				this.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1008:
				this.playSound(null, pos, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1009:
				this.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
				break;
			//TODO:1010
			case 1011:
				this.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1012:
				this.playSound(null, pos, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1013:
				this.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1014:
				this.playSound(null, pos, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			//TODO: everything else
//			case 2000:
//				Direction direction = Direction.byIndex(data);
//				int j1 = direction.getXOffset();
//				int j2 = direction.getYOffset();
//				int k2 = direction.getZOffset();
//				double d18 = (double) pos.getX() + (double) j1 * 0.6D + 0.5D;
//				double d24 = (double) pos.getY() + (double) j2 * 0.6D + 0.5D;
//				double d28 = (double) pos.getZ() + (double) k2 * 0.6D + 0.5D;
//
//				for (int i3 = 0; i3 < 10; ++i3) {
//					double d4 = random.nextDouble() * 0.2D + 0.01D;
//					double d6 = d18 + (double) j1 * 0.01D + (random.nextDouble() - 0.5D) * (double) k2 * 0.5D;
//					double d8 = d24 + (double) j2 * 0.01D + (random.nextDouble() - 0.5D) * (double) j2 * 0.5D;
//					double d30 = d28 + (double) k2 * 0.01D + (random.nextDouble() - 0.5D) * (double) j1 * 0.5D;
//					double d9 = (double) j1 * d4 + random.nextGaussian() * 0.01D;
//					double d10 = (double) j2 * d4 + random.nextGaussian() * 0.01D;
//					double d11 = (double) k2 * d4 + random.nextGaussian() * 0.01D;
//					if (!isRemote) {
//						spawnParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0, 0, 0, 0);
//					} else {
//						this.addOptionalParticle(ParticleTypes.SMOKE, d6, d8, d30, d9, d10, d11);
//					}
//				}
//				break;
			//TODO: everything else else
			default:
				if (!isRemote) {
					owner.getWorld().getServer().getPlayerList()
							.sendToAllNearExcept(
									player,
									(double) owner.getPos().getX() + (pos.getX() / (float) owner.unitsPerBlock),
									(double) owner.getPos().getY() + (((pos.getY() - 64)) / (float) owner.unitsPerBlock),
									(double) owner.getPos().getZ() + (pos.getZ() / (float) owner.unitsPerBlock),
									(64.0D), owner.getWorld().getDimensionKey(),
									new SPlaySoundEventPacket(type, owner.getPos(), data, false)
							);
				} else {
					owner.getWorld().playEvent(player, type, owner.getPos(), data);
				}
		}
	}
	
	@Override
	public void playMovingSound(@Nullable PlayerEntity playerIn, Entity entityIn, SoundEvent eventIn, SoundCategory categoryIn, float volume, float pitch) {
		playSound(playerIn, entityIn.getPositionVec().x, entityIn.getPositionVec().y, entityIn.getPositionVec().z, eventIn, categoryIn, volume, pitch);
	}
	
	@Override
	public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
		this.playSound(null, x, y, z, soundIn, category, volume, pitch);
	}
	
	@Override
	public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		playSound(player, pos.getX(), pos.getY(), pos.getZ(), soundIn, category, volume, pitch);
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
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
//					owner.getWorld().setBlockState(context.posInRealWorld, Deferred.UNIT.get().getDefaultState());
//					UnitTileEntity tileEntity = new UnitTileEntity();
//					owner.getWorld().setTileEntity(context.posInRealWorld, tileEntity);
//					tileEntity.unitsPerBlock = this.owner.unitsPerBlock;
				}
			}
		}
		if (World.isOutsideBuildHeight(context.posInRealWorld)) {
			return false;
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
			
			BlockState blockstate = chunk.setBlockState(pos, state, (flags & 64) != 0);
			if (blockstate == null) {
				if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
				return false;
			} else {
				BlockState blockstate1 = this.getBlockState(pos);
				if ((flags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getOpacity(this, pos) != oldOpacity || blockstate1.getLightValue(this, pos) != oldLight || blockstate1.isTransparent() || blockstate.isTransparent())) {
					this.getProfiler().startSection("queueCheckLight");
					lightManager.checkBlock(pos);
					this.getProfiler().endSection();
				}
				
				if (!state.getFluidState().isEmpty()) {
					if (state.getFluidState().getBlockState().equals(state.getBlockState())) {
						Fluid fluid = state.getFluidState().getFluid();
//						for (Direction dir : Direction.values()) {
//							this.getBlockState(pos.offset(dir)).neighborChanged(this, pos.offset(dir), state.getBlock(), pos, false);
//						}
						this.dimensionType = owner.getWorld().dimensionType;
						getPendingFluidTicks().scheduleTick(pos, fluid, fluid.getTickRate(this));
					}
				}

//				if (state.getBlockState().equals(state.getFluidState().getBlockState())) {
				if (old.getBlock() != state.getBlock()) {
					try {
						state.onBlockAdded(this, pos, old, false);
						//TODO: figure out why LootContext$Builder throws null pointers
					} catch (NullPointerException ignored) {
					}
					
					{
						BlockState statePlace = state;
						UnitTileEntity tileEntity = owner;
						if (statePlace.getBlock() instanceof ITileEntityProvider) {
							TileEntity te = ((ITileEntityProvider) statePlace.getBlock()).createNewTileEntity(tileEntity.world);
							tileEntity.world.setTileEntity(pos, te);
						} else if (statePlace.getBlock().hasTileEntity(statePlace)) {
							TileEntity te = statePlace.getBlock().createTileEntity(statePlace, tileEntity.world);
							tileEntity.world.setTileEntity(pos, te);
						}
					}
				}
//				}
				
				this.markAndNotifyBlock(pos, chunk, blockstate, state, flags, recursionLeft);
				
				if (state.equals(Blocks.AIR.getDefaultState())) {
					this.blockMap.remove(pos);
				}
				
				int newLight = state.getLightValue(this, pos);
				lightManager.blockLight.storage.updateSourceLevel(pos.toLong(), newLight, oldLight > newLight);
				
				return true;
			}
		}
	}
	
	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return owner.getWorld().getDifficultyForLocation(owner.getPos());
	}
	
	//TODO: fix properly
	@Override
	public Explosion createExplosion(@Nullable Entity exploder, @Nullable DamageSource damageSource, @Nullable ExplosionContext context, double x, double y, double z, float size, boolean causesFire, Explosion.Mode mode) {
		try {
			return super.createExplosion(exploder, damageSource, context, x, y, z, size, causesFire, mode);
		} catch (Throwable ignored) {
			return new Explosion(this, exploder, x, y, z, size, causesFire, mode);
		}
	}
	
	@Override
	public boolean addEntity(Entity entityIn) {
		entityIn.teleportKeepLoaded(
				owner.getPos().getX() + entityIn.getPositionVec().getX() / owner.unitsPerBlock,
				owner.getPos().getY() + ((entityIn.getPositionVec().getY() - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + entityIn.getPositionVec().getZ() / owner.unitsPerBlock
		);
		entityIn.setWorld(owner.getWorld());
		return owner.getWorld().addEntity(entityIn);
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
						for (Direction value : Direction.values()) {
							BlockPos pos1 = pos.offset(value);
							BlockState state1 = this.getBlockState(pos1);
							ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos1);
							if (context.teInRealWorld instanceof UnitTileEntity) {
								state1.updatePostPlacement(value.getOpposite(), state, ((UnitTileEntity) context.teInRealWorld).world, pos1, pos);
							}
						}
					}
					
					this.onBlockStateChange(pos, blockstate, blockstate1);
				}
			}
		}
	}
	
	//TODO: fix properly
	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
		try {
			return super.destroyBlock(pos, dropBlock, entity, recursionLeft);
		} catch (Throwable ignored) {
			return false;
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
		return dimensionType == null ? owner.getWorld().getDimensionType() : dimensionType;
	}
	
	@Override
	public RegistryKey<World> getDimensionKey() {
		return owner.getWorld().getDimensionKey();
	}
	
	@Override
	public boolean addTileEntity(TileEntity tile) {
		setTileEntity(tile.getPos(), tile);
		return true;
	}
	
	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
		for (TileEntity tileEntity : tileEntityCollection) {
			addTileEntity(tileEntity);
		}
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
	
	@Override
	public void playBroadcastSound(int id, BlockPos pos, int data) {
		//TODO: tiny-ify this
		owner.getWorld().playBroadcastSound(id, owner.getPos(), data);
	}
}
