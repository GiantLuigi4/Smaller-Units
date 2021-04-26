package com.tfc.smallerunits.utils.world.client;

import com.tfc.smallerunits.SmallerUnitsTESR;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.ExternalUnitInteractionContext;
import com.tfc.smallerunits.utils.SmallUnit;
import com.tfc.smallerunits.utils.world.common.FakeChunk;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DimensionType;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FakeClientWorld extends ClientWorld {
	public final ArrayList<Particle> particles = new ArrayList<>();
	public Long2ObjectLinkedOpenHashMap<SmallUnit> blockMap;
	public BlockRayTraceResult result;
	public UnitTileEntity owner;
	int maxID = 0;
	public FakeClientLightingManager lightManager = new FakeClientLightingManager(this.getChunkProvider(), true, true, this);
	
	@Override
	public WorldLightManager getLightManager() {
		return lightManager;
	}
	
	public FakeClientWorld(ClientPlayNetHandler p_i242067_1_, ClientWorldInfo p_i242067_2_, RegistryKey<World> p_i242067_3_, DimensionType p_i242067_4_, int p_i242067_5_, Supplier<IProfiler> p_i242067_6_, WorldRenderer p_i242067_7_, boolean p_i242067_8_, long p_i242067_9_) {
		super(p_i242067_1_, p_i242067_2_, p_i242067_3_, p_i242067_4_, p_i242067_5_, p_i242067_6_, p_i242067_7_, p_i242067_8_, p_i242067_9_);
		FakeClientWorld world = this;
		this.field_239129_E_ = new ClientChunkProvider(this, 1) {
			@Nullable
			@Override
			public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
				return new FakeChunk(world, new ChunkPos(chunkX, chunkZ), new BiomeContainer(new ObjectIntIdentityMap<>()), world);
			}
		};
	}
	
	@Override
	public Chunk getChunk(int chunkX, int chunkZ) {
		return new FakeChunk(this, new ChunkPos(chunkX, chunkZ), new BiomeContainer(new ObjectIntIdentityMap<>()), this);
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
								return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().getLightFor(lightTypeIn, context.posInFakeWorld);
							}
						}
					}
				}
			}
		}
		
		if (lightTypeIn.equals(LightType.BLOCK)) {
			return Math.max(
					((FakeClientLightingManager) lightManager).getBlockLight(blockPosIn.offset(Direction.DOWN, 64)),
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
	public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
		return result == null ? super.rayTraceBlocks(context) : result;
	}
	
	public DimensionRenderInfo func_239132_a_() {
		if (owner.getWorld() == null) {
			return new DimensionRenderInfo.Overworld();
		}
		DimensionRenderInfo info = ((ClientWorld) this.owner.getWorld()).func_239132_a_();
		return info == null ? new DimensionRenderInfo.Overworld() : info;
	}
	
	@Nullable
	@Override
	public BlockRayTraceResult rayTraceBlocks(Vector3d startVec, Vector3d endVec, BlockPos pos, VoxelShape shape, BlockState state) {
		return result == null ? super.rayTraceBlocks(startVec, endVec, pos, shape, state) : result;
	}
	
	@Override
	public boolean addEntity(Entity entityIn) {
		entityIn.world = this;
		addEntity(maxID, entityIn);
		return true;
	}
	
	@Override
	public void addEntity(int entityIdIn, Entity entityToSpawn) {
		boolean setId = false;
		for (int i = 0; i < entitiesById.size(); i++) {
			if (!entitiesById.containsKey(i)) {
				entityToSpawn.setEntityId(i);
				setId = true;
				break;
			}
		}
		if (!setId) {
			entityToSpawn.setEntityId(maxID);
		}
		entitiesById.put(entityToSpawn.getEntityId(), entityToSpawn);
		if (!setId) {
			maxID = Math.max(maxID, entityIdIn + 1);
		}
	}
	
	@Override
	public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		if (Minecraft.getInstance().gameSettings.particles.equals(ParticleStatus.ALL) || Minecraft.getInstance().gameSettings.particles.equals(ParticleStatus.DECREASED)) {
			Particle particle = Minecraft.getInstance().particles.makeParticle(particleData,
					owner.getPos().getX() + (x / owner.unitsPerBlock),
					owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
					owner.getPos().getZ() + (z / owner.unitsPerBlock),
					xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
			);
			if (particle == null) return;
			particle = particle.multiplyParticleScaleBy(1f / owner.unitsPerBlock);
			Minecraft.getInstance().particles.addEffect(particle);
		}
//		owner.getWorld().addParticle(particleData,
//				owner.getPos().getX() + (x / owner.unitsPerBlock),
//				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
//				owner.getPos().getZ() + (z / owner.unitsPerBlock),
//				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
//		);
	}
	
	@Override
	public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
		return true;
	}
	
	@Override
	public boolean hasNoCollisions(AxisAlignedBB aabb) {
		return true;
	}
	
	@Override
	public boolean hasNoCollisions(Entity entity) {
		return true;
	}
	
	@Override
	public boolean hasNoCollisions(Entity entity, AxisAlignedBB aabb) {
		return true;
	}
	
	@Override
	public boolean hasNoCollisions(@Nullable Entity entity, AxisAlignedBB aabb, Predicate<Entity> entityPredicate) {
		return true;
	}
	
	@Override
	public void addParticle(IParticleData particleData, boolean forceAlwaysRender, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
//		owner.getWorld().addParticle(particleData, forceAlwaysRender,
//				owner.getPos().getX() + (x / owner.unitsPerBlock),
//				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
//				owner.getPos().getZ() + (z / owner.unitsPerBlock),
//				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
//		);
	}
	
	@Override
	public void addOptionalParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		if (Minecraft.getInstance().gameSettings.particles.equals(ParticleStatus.ALL)) {
			Particle particle = Minecraft.getInstance().particles.makeParticle(particleData,
					owner.getPos().getX() + (x / owner.unitsPerBlock),
					owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
					owner.getPos().getZ() + (z / owner.unitsPerBlock),
					xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
			);
			if (particle == null) return;
			particle = particle.multiplyParticleScaleBy(1f / owner.unitsPerBlock);
			Minecraft.getInstance().particles.addEffect(particle);
		}
//		owner.getWorld().addOptionalParticle(particleData,
//				owner.getPos().getX() + (x / owner.unitsPerBlock),
//				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
//				owner.getPos().getZ() + (z / owner.unitsPerBlock),
//				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
//		);
	}
	
	@Override
	public void addOptionalParticle(IParticleData particleData, boolean ignoreRange, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		addOptionalParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
//		owner.getWorld().addOptionalParticle(particleData, ignoreRange,
//				owner.getPos().getX() + (x / owner.unitsPerBlock),
//				owner.getPos().getY() + ((y - 64) / owner.unitsPerBlock),
//				owner.getPos().getZ() + (z / owner.unitsPerBlock),
//				xSpeed / owner.unitsPerBlock, ySpeed / owner.unitsPerBlock, zSpeed / owner.unitsPerBlock
//		);
	}
	
	@Override
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.posInRealWorld != null) {
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (context.teInRealWorld != null) {
							((UnitTileEntity) context.teInRealWorld).getFakeWorld().setTileEntity(context.posInFakeWorld, tileEntityIn);
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

//		tileEntityChanges.add(unit);
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
	public BiomeManager getBiomeManager() {
		World world = this;
		return new BiomeManager(this, 0, (seed, x, y, z, biomeReader) -> world.getBiome(new BlockPos(x, y, z))) {
			@Override
			public Biome getBiomeAtPosition(double x, double y, double z) {
				return getBiomeAtPosition(new BlockPos(x, y, z));
			}
			
			@Override
			public Biome getBiomeAtPosition(BlockPos pos) {
				return owner.getWorld().getBiome(owner.getPos());
			}
			
			@Override
			public Biome getBiomeAtPosition(int x, int y, int z) {
				return getBiomeAtPosition(new BlockPos(x, y, z));
			}
		};
	}
	
	@Override
	public Biome getBiome(BlockPos pos) {
		return owner.getWorld().getBiome(owner.getPos());
	}
	
	public BlockPos getPos() {
		return owner.getPos();
	}
	
	@Override
	public void markBlockRangeForRenderUpdate(BlockPos blockPosIn, BlockState oldState, BlockState newState) {
		notifyBlockUpdate(null, null, null, 0);
	}
	
	@Override
	public void markSurroundingsForRerender(int sectionX, int sectionY, int sectionZ) {
		notifyBlockUpdate(null, null, null, 0);
	}
	
	@Override
	public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
//		for (Direction dir : Direction.values()) {
//			if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos().offset(dir))) {
//				SmallerUnitsTESR.bufferCache.get(this.getPos().offset(dir)).getSecond().dispose();
//			}
//
//			SmallerUnitsTESR.bufferCache.remove(this.getPos().offset(dir));
//		}
//
//		if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos())) {
//			SmallerUnitsTESR.bufferCache.get(this.getPos()).getSecond().dispose();
//		}
//
//		SmallerUnitsTESR.bufferCache.remove(this.getPos());
		
		if (SmallerUnitsTESR.bufferCache.containsKey(this.getPos())) {
			SmallerUnitsTESR.bufferCache.get(this.getPos()).getSecond().isDirty = true;
		}
	}
	
	public BlockState getBlockState(BlockPos pos) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.posInRealWorld != null && context.posInFakeWorld != null) {
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (context.teInRealWorld != null) {
							if (((UnitTileEntity) context.teInRealWorld).getFakeWorld() != null) {
								return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().getBlockState(context.posInFakeWorld);
							}
						}
					}
				} else if (true) {
					for (Direction value : Direction.values()) {
						if (context.posInRealWorld.equals(owner.getPos().offset(value))) {
							if (!(context.teInRealWorld instanceof UnitTileEntity)) {
								BlockState state = owner.getWorld().getBlockState(context.posInRealWorld);
								return state;
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
	
	public void init(UnitTileEntity owner) {
		this.owner = owner;
		IProfiler profiler = new Profiler(() -> 0, () -> 0, false);
		this.profiler = () -> profiler;
	}
	
	@Override
	public boolean isBlockPresent(BlockPos pos) {
		return blockMap.containsKey(pos.offset(Direction.DOWN, 64).toLong());
	}
	
	@Override
	public void tick(BooleanSupplier hasTimeLeft) {
		this.worldRenderer = Minecraft.getInstance().worldRenderer;
		this.getProfiler().startTick();
		super.tick(hasTimeLeft);
		this.getProfiler().endTick();
	}
	
	@Override
	public IProfiler getProfiler() {
		if (this == Minecraft.getInstance().world) return Minecraft.getInstance().getProfiler();
		else return profiler.get();
	}
	
	@Override
	public Supplier<IProfiler> getWorldProfiler() {
		if (this == Minecraft.getInstance().world) return () -> Minecraft.getInstance().getProfiler();
		else return profiler;
	}
	
	@Override
	public DynamicRegistries func_241828_r() {
		return owner == null ? Minecraft.getInstance().world.func_241828_r() : owner.getWorld().func_241828_r();
	}
	
	public Map<Integer, Entity> getEntitiesById() {
		return entitiesById;
	}
	
	public boolean containsEntityWithUUID(UUID uuid) {
		for (Entity value : entitiesById.values()) {
			if (value.getUniqueID().equals(uuid)) return true;
		}
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}
	
	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (context.posInRealWorld != null) {
			if (context.stateInRealWorld != null) {
				if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
					if (!context.posInRealWorld.equals(this.owner.getPos())) {
						if (context.teInRealWorld != null) {
							return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().getTileEntity(context.posInFakeWorld);
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
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(this, pos);
		if (recursionLeft < 0) return false;
		if (context.posInRealWorld != null) {
			if (context.stateInRealWorld != null) {
				if (!context.posInRealWorld.equals(owner.getPos())) {
					if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
						if (!context.posInRealWorld.equals(this.owner.getPos())) {
							if (((UnitTileEntity) context.teInRealWorld).getFakeWorld() == null)
								return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().setBlockState(context.posInFakeWorld, state, flags, recursionLeft - 1);
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
			IChunk chunk = getChunk(0, 0);
			
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
//					lightManager.checkBlock(pos);
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
							TileEntity te = ((ITileEntityProvider) statePlace.getBlock()).createNewTileEntity(tileEntity.getFakeWorld());
							tileEntity.getFakeWorld().setTileEntity(pos, te);
						} else if (statePlace.getBlock().hasTileEntity(statePlace)) {
							TileEntity te = statePlace.getBlock().createTileEntity(statePlace, tileEntity.getFakeWorld());
							tileEntity.getFakeWorld().setTileEntity(pos, te);
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
								state1.updatePostPlacement(value.getOpposite(), state, ((UnitTileEntity) context.teInRealWorld).getFakeWorld(), pos1, pos);
							}
						}
					}
					
					this.onBlockStateChange(pos, blockstate, blockstate1);
				}
			}
		}
	}
}
