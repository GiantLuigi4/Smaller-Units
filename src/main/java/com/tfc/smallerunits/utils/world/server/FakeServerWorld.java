package com.tfc.smallerunits.utils.world.server;

import com.tfc.smallerunits.SmallerUnitsConfig;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.ExternalUnitInteractionContext;
import com.tfc.smallerunits.utils.ResizingUtils;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.world.common.FakeChunk;
import com.tfc.smallerunits.utils.world.common.FakeIChunk;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.concurrent.DelegatedTaskExecutor;
import net.minecraft.util.concurrent.ITaskExecutor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.WorldCapabilityData;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
	
	protected static Profiler blankProfiler = new Profiler(() -> 0, () -> 0, false);
	public Map<Long, SmallUnit> blockMap;
	public List<SmallUnit> tileEntityChanges;
	public WorldLightManager lightManager;
	public UnitTileEntity owner;
	protected boolean hasInit = false;
	public boolean isFirstTick;
	public IChunk chunk;
	public List<BlockPos> tileEntityPoses;
	private List<Entity> entitiesToAddArrayList;
	
	public FakeServerWorld(MinecraftServer p_i241885_1_, Executor p_i241885_2_, SaveFormat.LevelSave p_i241885_3_, IServerWorldInfo p_i241885_4_, RegistryKey<World> p_i241885_5_, DimensionType p_i241885_6_, IChunkStatusListener p_i241885_7_, ChunkGenerator p_i241885_8_, boolean p_i241885_9_, long p_i241885_10_, List<ISpecialSpawner> p_i241885_12_, boolean p_i241885_13_) {
		super(p_i241885_1_, p_i241885_2_, p_i241885_3_, p_i241885_4_, p_i241885_5_, p_i241885_6_, p_i241885_7_, p_i241885_8_, p_i241885_9_, p_i241885_10_, p_i241885_12_, p_i241885_13_);
	}

//	private static final ArrayList<Particle> particles = new ArrayList<>();
	
	public BlockRayTraceResult result;
	public Queue<Entity> entitiesToRemove;
	private boolean isErrored = false;

//	private Object2ObjectLinkedOpenHashMap<ResourceLocation, ICapabilityProvider> capabilityObject2ObjectLinkedOpenHashMap;
	
	@OnlyIn(Dist.CLIENT)
//	public void animateTick(IRenderTypeBuffer buffer, float partialTicks) {
	public void animateTick() {
		for (int i = 0; i < 3 * owner.worldServer.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED); i++) {
			int x = rand.nextInt(16);
			int y = rand.nextInt(16);
			int z = rand.nextInt(16);
			if (x > owner.unitsPerBlock || y > owner.unitsPerBlock || z > owner.unitsPerBlock) continue;
			BlockPos randTickPos = new BlockPos(x, y + 64, z);
			BlockState state = getBlockState(randTickPos);
			state.getBlock().animateTick(state, this, randTickPos, rand);
		}
//		for (Particle particle : particles) {
//			particle.tick();
//			particle.renderParticle(buffer.getBuffer(particle.getRenderType()), Minecraft.getInstance().getRenderManager().info, partialTicks);
//		}
	}
	
	@Override
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.posInRealWorld != null && context.posInFakeWorld != null) {
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (context.teInRealWorld != null) {
							((UnitTileEntity) context.teInRealWorld).worldServer.setTileEntity(context.posInFakeWorld, tileEntityIn);
							return;
						}
					}
				}
			}
		}
		SmallUnit unit = blockMap.getOrDefault(pos.toLong(), new SmallUnit(pos, Blocks.AIR.getDefaultState()));
		
		if (unit.tileEntity != null && unit.tileEntity != tileEntityIn) {
			try {
				unit.tileEntity.remove();
			} catch (Throwable ignored) {
			}
			loadedTileEntityList.remove(tileEntityIn);
		}
		
		if (tileEntityIn != null && tileEntityIn.getType().isValidBlock(unit.state.getBlock())) {
			unit.tileEntity = tileEntityIn;
			tileEntityIn.setWorldAndPos(this, pos);
			if (tileEntityIn != null) tileEntityIn.validate();
			loadedTileEntityList.add(unit.tileEntity);
			if (!blockMap.containsKey(pos.toLong())) blockMap.put(pos.toLong(), unit);
		} else {
			if (unit.tileEntity != null)
				unit.tileEntity.remove();
			unit.tileEntity = null;
		}
		
		tileEntityChanges.add(unit);
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
		return owner.worldServer.isNightTime();
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
	public void removeTileEntity(BlockPos pos) {
		SmallUnit unit = blockMap.getOrDefault(pos.toLong(), new SmallUnit(pos, Blocks.AIR.getDefaultState()));
		loadedTileEntityList.remove(unit.tileEntity);
		tileEntityChanges.add(unit);
		if (unit.tileEntity != null)
			unit.tileEntity.remove();
		unit.tileEntity = null;
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
		return owner.getWorld().getBiome(new ExternalUnitInteractionContext(this, pos).posInRealWorld);
	}
	
	@Override
	public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		return chunk;
	}
	
	//Due to usage of theUnsafe, all constructor and field declaration code must be in a method
	public void init(UnitTileEntity owner) {
		if (!hasInit) {
			tileEntityChanges = new ArrayList<>();
			this.owner = owner;
			hasInit = true;
			field_241102_C_ = null;
			blockMap = new Long2ObjectArrayMap<>();
			tileEntityPoses = new ArrayList<>();
			chunk = new FakeIChunk(this);
			FakeServerWorld world = this;
			field_241102_C_ = FakeServerChunkProvider.getProvider(this);
			DelegatedTaskExecutor<Runnable> delegatedtaskexecutor1 = DelegatedTaskExecutor.create(Runnable::run, "light");
			ITaskExecutor<Unit> unitExecutor = ITaskExecutor.inline("idk", unit -> {
			});
			ITaskExecutor<ChunkTaskPriorityQueueSorter.FunctionEntry<Runnable>> itaskexecutor = ITaskExecutor.inline("su_world", (entry) -> entry.task.apply(unitExecutor).run());
			lightManager = new FakeServerLightingManager(
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
//			blankProfiler = new Profiler(() -> 0, () -> 0, false);
			profiler = () -> blankProfiler;
			worldBorder = border;
			isFirstTick = true;
			blockEventQueue = new ObjectLinkedOpenHashSet<>();
			players = new ArrayList<>();
			entitiesToAdd = new PriorityQueue<>();
			entitiesToRemove = new PriorityQueue<>();
			entitiesByUuid = new Object2ObjectLinkedOpenHashMap<>();
			entitiesById = new Int2ObjectLinkedOpenHashMap<>();
			entitiesToAddArrayList = new ArrayList<>();
			tickableTileEntities = new ArrayList<>();
			loadedTileEntityList = new ArrayList<>();
			addedTileEntityList = new ArrayList<>();
			capturedBlockSnapshots = new ArrayList<>();
		}
	}
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//		capabilityObject2ObjectLinkedOpenHashMap.get(cap.getName()).getCapability();
		return super.getCapability(cap, side);
	}
	
	@Override
	public BlockState getBlockState(BlockPos pos) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.posInRealWorld != null && context.posInFakeWorld != null) {
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (((UnitTileEntity) context.teInRealWorld).getFakeWorld() != null) {
							return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().getBlockState(context.posInFakeWorld);
						}
					}
				} else if (true) {
					for (Direction value : Direction.values()) {
						if (context.posInRealWorld.equals(owner.getPos().offset(value))) {
							if (!(context.teInRealWorld instanceof UnitTileEntity)) {
								BlockState state = owner.getWorld().getBlockState(context.posInRealWorld);
								if (state.hasTileEntity() || isRendering) {
									return state;
								}
							}
						}
					}
				}
				if (!context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (context.stateInRealWorld.equals(Blocks.BEDROCK.getDefaultState())) {
						return Blocks.BEDROCK.getDefaultState();
					} else if (context.stateInRealWorld.equals(Blocks.BARRIER.getDefaultState())) {
						return Blocks.BARRIER.getDefaultState();
					}
				}
			}
		}
		return blockMap.getOrDefault(pos.toLong(), new SmallUnit(pos, Blocks.AIR.getDefaultState())).state;
	}
	
	@Override
	public void tick(BooleanSupplier hasTimeLeft) {
		owner.getWorld().getProfiler().startSection("su_simulation");
		
		this.isRemote = this.owner.getWorld().isRemote;
		owner.getWorld().getProfiler().startSection("firstTick");
		if (isFirstTick) {
			init(owner);
			dimension = owner.getWorld().dimension;
			dimensionType = owner.getWorld().dimensionType;
			raids = new RaidManager(this);
			isFirstTick = false;
			server = owner.getWorld().getServer();
			
			if (owner.getWorld().getChunkProvider() instanceof ServerChunkProvider) {
				(this.getChunkProvider()).generator = ((ServerChunkProvider) owner.getWorld().getChunkProvider()).generator;
			}
			
			MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(this));
			try {
				theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(ObfuscationReflectionHelper.findField(World.class, "field_147483_b")), Collections.newSetFromMap(new IdentityHashMap<>()));
				theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(ObfuscationReflectionHelper.findField(ServerWorld.class, "capabilityData")), new WorldCapabilityData("Smaller Units Ticking World Capability Data"));
				theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(ObfuscationReflectionHelper.findField(CapabilityProvider.class, "baseClass")), World.class);
				AttachCapabilitiesEvent<World> event = new AttachCapabilitiesEvent<>(World.class, this);
				MinecraftForge.EVENT_BUS.post(event);
				CapabilityDispatcher dispatcher = new CapabilityDispatcher(event.getCapabilities(), event.getListeners());
				theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(ObfuscationReflectionHelper.findField(CapabilityProvider.class, "capabilities")), dispatcher);
				theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(ObfuscationReflectionHelper.findField(CapabilityProvider.class, "valid")), true);
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
		}
		
		owner.getWorld().getProfiler().endStartSection("iter_add_entities");
		for (Entity entity : entitiesToAddArrayList) {
			if (entity == null) continue;
			entitiesByUuid.put(entity.getUniqueID(), entity);
			entitiesById.put(entity.getEntityId(), entity);
			owner.markDirty();
			owner.getWorld().markChunkDirty(owner.getPos(), owner);
		}
		
		owner.getWorld().getProfiler().endStartSection("iterRemoveAir");
		ArrayList<SmallUnit> unitsToRemove = new ArrayList<>();
		for (SmallUnit value : blockMap.values()) {
			if (value.state.equals(Blocks.AIR.getDefaultState())) {
				unitsToRemove.add(value);
			}
		}
		for (SmallUnit smallUnit : unitsToRemove) {
//			setBlockState(smallUnit.pos,Blocks.AIR.getDefaultState());
			if (smallUnit.tileEntity != null) {
				smallUnit.tileEntity.remove();
				tileEntityChanges.add(smallUnit);
			}
			blockMap.remove(smallUnit.pos.toLong());
		}
		
		entitiesToAddArrayList.clear();
		
		owner.getWorld().getProfiler().endStartSection("iter_remove_entities");
		for (Entity entity : entitiesToRemove) {
			entitiesByUuid.remove(entity.getUniqueID(), entity);
			entitiesById.remove(entity.getEntityId(), entity);
			owner.markDirty();
			owner.getWorld().markChunkDirty(owner.getPos(), owner);
		}
		
		entitiesToRemove.clear();
		
		if (this.isRemote) {
			owner.getWorld().getProfiler().endStartSection("client_tick");
			blankProfiler.startTick();
			MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent.WorldTickEvent(LogicalSide.CLIENT, TickEvent.Phase.START, this));
			for (SmallUnit value : this.blockMap.values()) {
				if (value.tileEntity instanceof ITickableTileEntity) {
					((ITickableTileEntity) value.tileEntity).tick();
				}
			}
			MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent.WorldTickEvent(LogicalSide.CLIENT, TickEvent.Phase.END, this));
			blankProfiler.endTick();
			owner.getWorld().getProfiler().endSection();
			owner.getWorld().getProfiler().endSection();
			return;
		}
		
		blankProfiler.startTick();
		
		owner.getWorld().getProfiler().endStartSection("main_tick");
		if (!isErrored) {
			try {
				MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.START, this));
				super.tick(hasTimeLeft);
				MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, this));
			} catch (Throwable err) {
				StringBuilder stacktrace = new StringBuilder(err.toString() + "\n");
				for (StackTraceElement element : err.getStackTrace()) {
					stacktrace.append(element.toString()).append("\n");
				}
				System.out.println(stacktrace);
				err = err.getCause();
				if (err != null) {
					stacktrace = new StringBuilder(err.toString() + "\n");
					for (StackTraceElement element : err.getStackTrace()) {
						stacktrace.append(element.toString()).append("\n");
					}
					System.out.println(stacktrace);
					isErrored = true;
				}
			}
		}
		
		owner.getWorld().getProfiler().endStartSection("lighting");
		if (!this.isRemote)
			lightManager.tick(SmallerUnitsConfig.SERVER.lightingUpdatesPerTick.get(), false, true);

//		owner.getWorld().getProfiler().endStartSection("");
//		ArrayList<SmallUnit> unitsToRemove = new ArrayList<>();
//		for (SmallUnit value : blockMap.values()) {
//			if (value.tileEntity != null) {
//				if (!loadedTileEntityList.contains(value.tileEntity)) {
//					loadedTileEntityList.add(value.tileEntity);
//					tileEntityChanges.add(value);
//				}
//
//				if (value.state.equals(Deferred.UNIT.get().getDefaultState())) {
//					unitsToRemove.add(value);
//				}
//			}
//		}
		
		owner.getWorld().getProfiler().endStartSection("random_ticks");
		//Random Ticks
		for (int i = 0; i < 3 * owner.worldServer.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED); i++) {
			int x = rand.nextInt(16);
			int y = rand.nextInt(16);
			int z = rand.nextInt(16);
			if (x > owner.unitsPerBlock || y > owner.unitsPerBlock || z > owner.unitsPerBlock) continue;
			BlockPos randTickPos = new BlockPos(x, y + 64, z);
			BlockState state = getBlockState(randTickPos);
			if (state.ticksRandomly()) {
				state.randomTick(this, randTickPos, rand);
			}
		}
		
		owner.getWorld().getProfiler().endStartSection("remove_tile_entities");
		ArrayList<TileEntity> toRemove = new ArrayList<>();
		
		for (SmallUnit unit : tileEntityChanges) {
			if (unit.tileEntity != null) {
				if (!tickableTileEntities.contains(unit.tileEntity) && unit.tileEntity instanceof ITickableTileEntity)
					tickableTileEntities.add(unit.tileEntity);
				if (!loadedTileEntityList.contains(unit.tileEntity)) loadedTileEntityList.add(unit.tileEntity);
			}
			
			if (unit.oldTE != null && !unit.oldTE.equals(unit.tileEntity))
				unit.oldTE.remove();
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
		
		blankProfiler.endTick();
		owner.getWorld().getProfiler().endSection();
		owner.getWorld().getProfiler().endSection();
	}
	
	@Override
	public DynamicRegistries func_241828_r() {
		return new DynamicRegistries.Impl();
	}
	
	@Override
	public void tickBlockEntities() {
//		IProfiler iprofiler = this.getProfiler();
//		iprofiler.startSection("blockEntities");
//
//		for (TileEntity tileentity : tickableTileEntities) {
//			if (!tileentity.isRemoved() && tileentity.hasWorld()) {
//				net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackStart(tileentity);
//				iprofiler.startSection(() -> {
//					return String.valueOf(tileentity.getType().getRegistryName());
//				});
//				if (tileentity.getType().isValidBlock(this.getBlockState(tileentity.getPos()).getBlock())) {
//					((ITickableTileEntity) tileentity).tick();
//				} else {
//					tileentity.warnInvalidBlock();
//				}
//				iprofiler.endSection();
//				net.minecraftforge.server.timings.TimeTracker.TILE_ENTITY_UPDATE.trackEnd(tileentity);
//			}
//		}
//
//		iprofiler.endSection();
	}
	
	@Override
	public void guardEntityTick(Consumer<Entity> consumerEntity, Entity entityIn) {
//		super.guardEntityTick(consumerEntity, entityIn);
	}
	
	@Override
	public void updateEntity(Entity entityIn) {
//		super.updateEntity(entityIn);
	}
	
	@Nullable
	@Override
	public BlockRayTraceResult rayTraceBlocks(Vector3d startVec, Vector3d endVec, BlockPos pos, VoxelShape shape, BlockState state) {
		return result;
//		return super.rayTraceBlocks(startVec, endVec, pos, shape, state);
	}
	
	@Override
	public void markAndNotifyBlock(BlockPos pos, @Nullable Chunk chunk, BlockState blockstate, BlockState state, int flags, int recursionLeft) {
		owner.markDirty();
		owner.getWorld().notifyBlockUpdate(owner.getPos(), owner.getBlockState(), owner.getBlockState(), 3);
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
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		owner.markDirty();
		owner.getWorld().notifyBlockUpdate(owner.getPos(), owner.getBlockState(), owner.getBlockState(), 3);
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
		return new FakeChunk(this, new ChunkPos(chunkX, chunkZ), new BiomeContainer(new ObjectIntIdentityMap<>()), this);
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
	
	@Nonnull
	@Override
	public MinecraftServer getServer() {
		return owner.getWorld().getServer();
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
	
	@Override
	public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
		if (lightTypeIn.equals(LightType.BLOCK)) {
			ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, blockPosIn);
			if (context.posInRealWorld != null && context.posInFakeWorld != null) {
				if (context.stateInRealWorld != null) {
					if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
						if (!context.posInRealWorld.equals(this.owner.getPos())) {
							if (context.teInRealWorld != null) {
								return ((UnitTileEntity) context.teInRealWorld).worldServer.getLightFor(lightTypeIn, context.posInFakeWorld);
							}
						}
					}
				}
			}
		}
		
		if (lightTypeIn.equals(LightType.BLOCK)) {
			return Math.max(
					((FakeServerLightingManager) lightManager).getBlockLight(blockPosIn.offset(Direction.DOWN, 64)),
					owner.getWorld().getLightFor(lightTypeIn, owner.getPos())
			);
		} else {
			return Math.max(
//					lightManager.getLightEngine(lightTypeIn).getLightFor(blockPosIn),
					0,
					owner.getWorld().getLightFor(lightTypeIn, owner.getPos())
			);
		}
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.posInRealWorld != null && context.posInFakeWorld != null) {
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (context.teInRealWorld != null) {
							return ((UnitTileEntity) context.teInRealWorld).worldServer.getTileEntity(context.posInFakeWorld);
						}
					}
				} else {
					for (Direction value : Direction.values()) {
						if (context.posInRealWorld.equals(owner.getPos().offset(value))) {
							if (!(context.teInRealWorld instanceof UnitTileEntity)) {
								return owner.getWorld().getTileEntity(context.posInRealWorld);
							}
						}
					}
				}
			}
		}
		TileEntity te = blockMap.getOrDefault(pos.toLong(), new SmallUnit(pos, Blocks.AIR.getDefaultState())).tileEntity;
		return te;
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
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		isErrored = false;
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (recursionLeft < 0) return false;
		if (context.posInRealWorld != null && context.posInFakeWorld != null) {
			if (context.stateInRealWorld != null) {
				if (!context.posInRealWorld.equals(owner.getPos())) {
					if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
						if (!context.posInRealWorld.equals(this.owner.getPos())) {
							return ((UnitTileEntity) context.teInRealWorld).worldServer.setBlockState(context.posInFakeWorld, state, flags, recursionLeft - 1);
						}
						return false;
					} else if (context.stateInRealWorld.isAir(owner.getWorld(), context.posInRealWorld)) {
//					owner.getWorld().setBlockState(context.posInRealWorld, Deferred.UNIT.get().getDefaultState());
//					UnitTileEntity tileEntity = new UnitTileEntity();
//					owner.getWorld().setTileEntity(context.posInRealWorld, tileEntity);
//					tileEntity.unitsPerBlock = this.owner.unitsPerBlock;
					} else {
						return false;
					}
				}
			}
		}
		if (World.isOutsideBuildHeight(context.posInRealWorld)) {
			return false;
		}
		
		owner.markDirty();
		owner.getWorld().notifyBlockUpdate(owner.getPos(), owner.getBlockState(), owner.getBlockState(), 3);
		
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
							TileEntity te = ((ITileEntityProvider) statePlace.getBlock()).createNewTileEntity(tileEntity.worldServer);
							tileEntity.worldServer.setTileEntity(pos, te);
						} else if (statePlace.getBlock().hasTileEntity(statePlace)) {
							TileEntity te = statePlace.getBlock().createTileEntity(statePlace, tileEntity.worldServer);
							tileEntity.worldServer.setTileEntity(pos, te);
						}
					}
				}
//				}
				
				if (this.getTileEntity(pos) != null)
					this.getTileEntity(pos).markDirty();
				
				this.markAndNotifyBlock(pos, chunk, blockstate, state, flags, recursionLeft);
				
				if (state.equals(Blocks.AIR.getDefaultState())) {
					try {
						if (this.getTileEntity(pos) != null) this.getTileEntity(pos).remove();
					} catch (Throwable ignored) {
					}
//					this.blockMap.remove(pos.toLong());
				}
				
				try {
					int newLight = state.getLightValue(this, pos);
					lightManager.blockLight.storage.updateSourceLevel(pos.toLong(), newLight, oldLight > newLight);
				} catch (Throwable ignored) {
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
						for (Direction value : Direction.values()) {
							BlockPos pos1 = pos.offset(value);
							BlockState state1 = this.getBlockState(pos1);
							ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos1);
							if (context.teInRealWorld instanceof UnitTileEntity) {
								state1.updatePostPlacement(value.getOpposite(), state, ((UnitTileEntity) context.teInRealWorld).worldServer, pos1, pos);
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
//		return owner == null ? blankProfiler : owner.getWorld().getProfiler();
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
	public Chunk getChunkAt(BlockPos pos) {
		return super.getChunkAt(pos);
	}
	
	@Override
	public void playBroadcastSound(int id, BlockPos pos, int data) {
		//TODO: tiny-ify this
		owner.getWorld().playBroadcastSound(id, owner.getPos(), data);
	}
	
	@Override
	public boolean addEntity(Entity entityIn) {
		if (!(entityIn instanceof ArmorStandEntity) && (entityIn instanceof LivingEntity || entityIn instanceof ItemEntity || entityIn instanceof ExperienceOrbEntity || entityIn instanceof PotionEntity && entityIn instanceof Comparable) && !(entityIn instanceof PlayerEntity)) {
//		if (false) {
			entityIn.teleportKeepLoaded(
					owner.getPos().getX() + entityIn.getPositionVec().getX() / owner.unitsPerBlock,
					owner.getPos().getY() + ((entityIn.getPositionVec().getY() - 64) / owner.unitsPerBlock),
					owner.getPos().getZ() + entityIn.getPositionVec().getZ() / owner.unitsPerBlock
			);
			entityIn.setWorld(owner.getWorld());
			ResizingUtils.resizeForUnit(entityIn, 1f / owner.unitsPerBlock);
			return owner.getWorld().addEntity(entityIn);
		} else {
			entitiesToAddArrayList.add(entityIn);
			return true;
		}
	}
	
	public boolean containsEntityWithUUID(UUID uuid) {
		if (entitiesById == null) return false;
		for (Entity value : entitiesById.values()) {
			if (value.getUniqueID().equals(uuid)) return true;
		}
		return false;
	}
	
	@Override
	public void removeEntity(Entity entityIn) {
		entitiesToRemove.add(entityIn);
	}
	
	@Override
	public ServerScoreboard getScoreboard() {
		if (owner.getWorld().getScoreboard() instanceof ServerScoreboard)
			return (ServerScoreboard) owner.getWorld().getScoreboard();
		else return new ServerScoreboard(owner.getWorld().getServer());
	}
	
	@Override
	public boolean isBlockModifiable(PlayerEntity player, BlockPos pos) {
		return owner.getWorld().isBlockModifiable(player, owner.getPos());
	}
	
	@Override
	public boolean isPlayerWithin(double x, double y, double z, double distance) {
		Vector3d realPos = getRealPos(x, y, z);
		return owner.getWorld().isPlayerWithin(realPos.x, realPos.y, realPos.z, distance / owner.unitsPerBlock);
	}
	
	@Override
	public Stream<BlockState> getStatesInArea(AxisAlignedBB aabb) {
		ArrayList<BlockState> states = new ArrayList<>();
		for (int x = (int) aabb.minX; x < aabb.maxX; x++) {
			for (int y = (int) aabb.minY; y < aabb.maxY; y++) {
				for (int z = (int) aabb.minZ; z < aabb.maxZ; z++) {
					states.add(getBlockState(new BlockPos(x, y, z)));
				}
			}
		}
		return Stream.of(states.toArray(new BlockState[0]));
	}
	
	@Override
	public Stream<VoxelShape> getCollisionShapes(@Nullable Entity entity, AxisAlignedBB aabb) {
		ArrayList<VoxelShape> shapes = new ArrayList<>();
		float padding = 0.5f;
		aabb = aabb.expand(padding, 0, padding).offset(-padding / 2, 0, -padding / 2);
		VoxelShape aabbShape = VoxelShapes.create(aabb);
		for (int x = (int) aabb.minX; x < aabb.maxX; x++) {
			for (int y = (int) aabb.minY; y < aabb.maxY; y++) {
				for (int z = (int) aabb.minZ; z < aabb.maxZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					VoxelShape shape = getBlockState(pos).getCollisionShape(this, pos, entity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entity));
					shape = shape.withOffset(pos.getX(), pos.getY(), pos.getZ());
					shape = VoxelShapes.combine(shape, aabbShape, IBooleanFunction.AND);
					shapes.add(shape);
				}
			}
		}
		return Stream.of(shapes.toArray(new VoxelShape[0]));
	}
	
	@Override
	public boolean hasNoCollisions(AxisAlignedBB aabb) {
		for (Entity value : entitiesById.values()) {
			if (value.getBoundingBox().equals(aabb)) {
				continue;
			}
			if (value.getBoundingBox().intersects(aabb)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
		return hasNoCollisions(shape.getBoundingBox());
	}
	
	@Override
	public boolean checkNoEntityCollision(Entity entity) {
		return hasNoCollisions(entity.getBoundingBox());
	}
	
	public Vector3d getRealPos(double x, double y, double z) {
		return new Vector3d(owner.getPos().getX() + x / owner.unitsPerBlock,
				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
				owner.getPos().getZ() + z / owner.unitsPerBlock);
	}
}
