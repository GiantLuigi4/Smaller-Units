package tfc.smallerunits.utils.world.client;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import tfc.smallerunits.SmallerUnitsTESR;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.ExternalUnitInteractionContext;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.world.common.FakeChunk;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FakeClientWorld extends ClientWorld {
	public Map<Long, SmallUnit> blockMap;
	public BlockRayTraceResult result;
	public UnitTileEntity owner;
	int maxID = 0;
	int oldLight = 0;
	public FakeClientLightingManager lightManager = new FakeClientLightingManager(this.getChunkProvider(), true, true, this);
	
	@Override
	public WorldLightManager getLightManager() {
		return lightManager;
	}
	
	private static final Biome[] BIOMES = Util.make(new Biome[BiomeContainer.BIOMES_SIZE], (biomes) -> {
		Arrays.fill(biomes, BiomeRegistry.PLAINS);
	});
	Chunk thisChunk;
	
	public FakeClientWorld(ClientPlayNetHandler p_i242067_1_, ClientWorldInfo p_i242067_2_, RegistryKey<World> p_i242067_3_, DimensionType p_i242067_4_, int p_i242067_5_, Supplier<IProfiler> p_i242067_6_, WorldRenderer p_i242067_7_, boolean p_i242067_8_, long p_i242067_9_) {
		super(p_i242067_1_, p_i242067_2_, p_i242067_3_, p_i242067_4_, p_i242067_5_, p_i242067_6_, p_i242067_7_, p_i242067_8_, p_i242067_9_);
		FakeClientWorld world = this;
		this.field_239129_E_ = new ClientChunkProvider(this, 1) {
			@Nullable
			@Override
			public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
				return thisChunk;
			}
		};
		blockMap = new HashMap<>();
//		this.addedTileEntityList = new ArrayList<>();
//		this.loadedTileEntityList = new ArrayList<>();
//		this.tickableTileEntities = new ArrayList<>();
	}
	
	@Override
	public Chunk getChunk(int chunkX, int chunkZ) {
		return thisChunk;
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
								if (((UnitTileEntity) context.teInRealWorld).worldClient != null && ((UnitTileEntity) context.teInRealWorld).worldClient.get().lightManager != null) {
									return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().getLightFor(lightTypeIn, context.posInFakeWorld);
								}
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
		for (AxisAlignedBB axisAlignedBB : shape.toBoundingBoxList()) {
			for (Entity entity : this.getEntitiesWithinAABBExcludingEntity(entityIn, axisAlignedBB)) {
				if (!entity.removed && entity.preventEntitySpawning) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB aabb) {
		List<Entity> entities = super.getEntitiesWithinAABBExcludingEntity(entityIn, aabb);
		AxisAlignedBB aabb1 = new AxisAlignedBB(0, 0, 0, aabb.getXSize() / owner.unitsPerBlock, aabb.getYSize() / owner.unitsPerBlock, aabb.getZSize() / owner.unitsPerBlock);
		AxisAlignedBB bb = aabb1.offset(
				aabb.minX / owner.unitsPerBlock,
				(aabb.minY - 64) / owner.unitsPerBlock,
				aabb.minZ / owner.unitsPerBlock
		).offset(owner.getPos().getX(), owner.getPos().getY(), owner.getPos().getZ());
		entities.addAll(owner.getWorld().getEntitiesWithinAABBExcludingEntity(entityIn, bb));
		return entities;
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
	
	private final ArrayList<BlockPos> removedTileEntities = new ArrayList<>();
	
	@Override
	public boolean addTileEntity(TileEntity tile) {
		if (!(tile.getPos() instanceof UnitPos)) tile.setPos(SmallerUnitsAPI.createPos(tile.getPos(), owner));
		setTileEntity(tile.getPos(), tile);
		return true;
	}
	
	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
		for (TileEntity tileEntity : tileEntityCollection) addTileEntity(tileEntity);
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
				pos = new UnitPos(pos, owner.getPos(), owner.unitsPerBlock).adjustRealPosition();
				return owner.getWorld().getBiome(((UnitPos) pos).realPos);
			}
			
			@Override
			public Biome getBiomeAtPosition(int x, int y, int z) {
				return getBiomeAtPosition(new BlockPos(x, y, z));
			}
		};
	}
	
	@Override
	public Biome getBiome(BlockPos pos) {
		pos = new UnitPos(pos, owner.getPos(), owner.unitsPerBlock).adjustRealPosition();
		return owner.getWorld().getBiome(((UnitPos) pos).realPos);
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
		owner.needsRefresh(true);
	}
	
	@Override
	public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn) {
		if (removedTileEntities.contains(pos)) removedTileEntities.remove(pos);
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
		SmallUnit unit = blockMap.getOrDefault(pos.toLong(), new SmallUnit(SmallerUnitsAPI.createPos(pos, owner), Blocks.AIR.getDefaultState()));
		
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
	public void playEvent(int type, BlockPos pos, int data) {
		playEvent(null, type, pos, data);
	}
	
	@Override
	public RecipeManager getRecipeManager() {
		return owner.getWorld().getRecipeManager();
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
		return blockMap.getOrDefault(pos.toLong(), new SmallUnit(SmallerUnitsAPI.createPos(pos, owner), Blocks.AIR.getDefaultState())).state;
	}
	
	public void init(UnitTileEntity owner) {
		this.owner = owner;
		thisChunk = new FakeChunk(this, new ChunkPos(0, 0), new BiomeContainer(
				owner.getWorld().func_241828_r().getRegistry(Registry.BIOME_KEY), BIOMES),
				this
		);
		IProfiler profiler = new Profiler(() -> 0, () -> 0, false);
		this.profiler = () -> profiler;
	}
	
	@Override
	public boolean isBlockPresent(BlockPos pos) {
		return blockMap.containsKey(pos.offset(Direction.DOWN, 64).toLong());
	}
	
	@Override
	public void tick(BooleanSupplier hasTimeLeft) {
		int newLight = LightTexture.packLight(owner.getWorld().getLightFor(LightType.BLOCK, owner.getPos()), owner.getWorld().getLightFor(LightType.SKY, owner.getPos()));
		if (newLight != oldLight) owner.needsRefresh(true);
		oldLight = newLight;
		this.worldRenderer = Minecraft.getInstance().worldRenderer;
		this.getProfiler().startTick();
		super.tick(hasTimeLeft);
		this.getProfiler().endTick();
		
		ArrayList<Integer> toRemove = new ArrayList<>();
		for (Integer integer : entitiesById.keySet()) if (entitiesById.get(integer).removed) toRemove.add(integer);
		toRemove.forEach(entitiesById::remove);
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
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
		owner.getWorld().playSound(player, owner.getPos().getX() + (x / (float) owner.unitsPerBlock), owner.getPos().getY() + ((y - 64) / (float) owner.unitsPerBlock), owner.getPos().getZ() + (z / (float) owner.unitsPerBlock), soundIn, category, (volume / owner.unitsPerBlock), pitch);
	}
	
	/**
	 * {@link net.minecraft.client.renderer.WorldRenderer#playEvent}
	 */
	@Override
	public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
		getBlockState(pos).receiveBlockEvent(this, pos, type, data);
		this.isRemote = owner.getWorld().isRemote;
		Random random = rand;
		// TODO: move this to client world
		switch (type) {
			case 1000:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0F, 1.2F);
				break;
			case 1001:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.2F);
				break;
			case 1002:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_DISPENSER_LAUNCH, SoundCategory.BLOCKS, 1.0F, 1.2F);
				break;
			case 1003:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 1.0F, 1.2F);
				break;
			case 1004:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.2F);
				break;
			case 1005:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1006:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1007:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1008:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_FENCE_GATE_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1009:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
				break;
			//TODO:1010
			case 1011:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1012:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1013:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1014:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1015:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1016:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1017:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1018:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1019:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1020:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1021:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1022:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1024:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1025:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.NEUTRAL, 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1026:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1027:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1029:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1030:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1031:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.3F, this.rand.nextFloat() * 0.1F + 0.9F);
				break;
			case 1032:
				// TODO
//				this.mc.getSoundHandler().play(SimpleSound.ambientWithoutAttenuation(SoundEvents.BLOCK_PORTAL_TRAVEL, random.nextFloat() * 0.4F + 0.8F, 0.25F));
				break;
			case 1033:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_CHORUS_FLOWER_GROW, SoundCategory.BLOCKS, 1.0F, 1.0F);
				break;
			case 1034:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_CHORUS_FLOWER_DEATH, SoundCategory.BLOCKS, 1.0F, 1.0F);
				break;
			case 1035:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0F, 1.0F);
				break;
			case 1036:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1037:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F);
				break;
			case 1039:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.HOSTILE, 0.3F, this.rand.nextFloat() * 0.1F + 0.9F);
				break;
			case 1040:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1041:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ENTITY_HUSK_CONVERTED_TO_ZOMBIE, SoundCategory.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
				break;
			case 1042:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.0F, this.rand.nextFloat() * 0.1F + 0.9F);
				break;
			case 1043:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1.0F, this.rand.nextFloat() * 0.1F + 0.9F);
				break;
			case 1044:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 1.0F, this.rand.nextFloat() * 0.1F + 0.9F);
				break;
			case 1500:
				ComposterBlock.playEvent(this, pos, data > 0);
				break;
			case 1502:
				this.playSound(Minecraft.getInstance().player, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
				
				for (int l1 = 0; l1 < 5; ++l1) {
					double d15 = (double) pos.getX() + random.nextDouble() * 0.6D + 0.2D;
					double d20 = (double) pos.getY() + random.nextDouble() * 0.6D + 0.2D;
					double d26 = (double) pos.getZ() + random.nextDouble() * 0.6D + 0.2D;
					this.addParticle(ParticleTypes.SMOKE, d15, d20, d26, 0.0D, 0.0D, 0.0D);
				}
				break;
			case 2001:
				BlockState state = Block.getStateById(data);
				if (!state.isAir(this, pos)) {
					SoundType soundtype = state.getSoundType(this, pos, null);
					this.playSound(pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, false);
				}
				
				if (!state.isAir(this, pos) && !state.addDestroyEffects(this, pos, Minecraft.getInstance().particles)) {
					VoxelShape voxelshape = state.getShape(this, pos);
					voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
						double d1 = Math.min(1.0D, x2 - x1);
						double d2 = Math.min(1.0D, y2 - y1);
						double d3 = Math.min(1.0D, z2 - z1);
						int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
						int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
						int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));
						
						for (int l = 0; l < i; ++l) {
							for (int i1 = 0; i1 < j; ++i1) {
								for (int j1 = 0; j1 < k; ++j1) {
									double d4 = ((double) l + 0.5D) / (double) i;
									double d5 = ((double) i1 + 0.5D) / (double) j;
									double d6 = ((double) j1 + 0.5D) / (double) k;
									double d7 = d4 * d1 + x1;
									double d8 = d5 * d2 + y1;
									double d9 = d6 * d3 + z1;
									Minecraft.getInstance().particles.addEffect(
											(new DiggingParticle((ClientWorld) owner.getWorld(),
													(((double) pos.getX() + d7) / owner.unitsPerBlock) + owner.getPos().getX(),
													(((double) pos.getY() + d8 - 64) / owner.unitsPerBlock) + owner.getPos().getY(),
													(((double) pos.getZ() + d9) / owner.unitsPerBlock) + owner.getPos().getZ(),
													(d4 - 0.5D),
													(d5 - 0.5D),
													(d6 - 0.5D),
													state
											)).setBlockPos(pos)
													.multiplyVelocity((1f / owner.unitsPerBlock) * 1.5f)
													.multiplyParticleScaleBy(1f / owner.unitsPerBlock)
									);
								}
							}
						}
						
					});
				}
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
//				if (!isRemote) {
//					owner.getWorld().getServer().getPlayerList()
//							.sendToAllNearExcept(
//									player,
//									(double) owner.getPos().getX() + (pos.getX() / (float) owner.unitsPerBlock),
//									(double) owner.getPos().getY() + (((pos.getY() - 64)) / (float) owner.unitsPerBlock),
//									(double) owner.getPos().getZ() + (pos.getZ() / (float) owner.unitsPerBlock),
//									(64.0D), owner.getWorld().getDimensionKey(),
//									new SPlaySoundEventPacket(type, owner.getPos(), data, false)
//							);
//				} else {
//					owner.getWorld().playEvent(player, type, owner.getPos(), data);
//				}
		}
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
							if (context.teInRealWorld instanceof UnitTileEntity) {
								if (((UnitTileEntity) context.teInRealWorld).getFakeWorld() != null) {
									return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().getTileEntity(context.posInFakeWorld);
								}
							}
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
//		for (TileEntity tileEntityChange : tileEntitiesToBeRemoved) {
//			if (tileEntityChange.equals(blockMap.getOrDefault(pos.toLong(), new SmallUnit(pos, Blocks.AIR.getDefaultState())).tileEntity)) {
//				return null;
//			}
//		}
//		boolean containsTE = false;
		if (removedTileEntities.contains(pos)) return null;
		for (TileEntity tileEntity : loadedTileEntityList) {
			if (tileEntity.getPos().equals(pos)) {
				TileEntity te = blockMap.getOrDefault(pos.toLong(), new SmallUnit(SmallerUnitsAPI.createPos(pos, owner), Blocks.AIR.getDefaultState())).tileEntity;
				return te;
//				containsTE = true;
			}
		}
		return null;
	}
	
	@Override
	public void removeTileEntity(BlockPos pos) {
		removedTileEntities.add(pos);
		super.removeTileEntity(pos);
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
							if (((UnitTileEntity) context.teInRealWorld).getFakeWorld() != null) {
								((UnitTileEntity) context.teInRealWorld).needsRefresh(true);
								return ((UnitTileEntity) context.teInRealWorld).getFakeWorld().setBlockState(context.posInFakeWorld, state, flags, recursionLeft - 1);
							}
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
		owner.needsRefresh(true);
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
//						state.onBlockAdded(this, pos, old, false);
//						super.setBlockState(pos, state, flags);
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
	
	public void unload() {
		{
			WorldEvent.Unload unload = new WorldEvent.Unload(this);
			MinecraftForge.EVENT_BUS.post(unload);
		}
		if (thisChunk != null) {
			{
				ChunkEvent.Unload unload = new ChunkEvent.Unload(this.getChunk(0, 0));
				MinecraftForge.EVENT_BUS.post(unload);
			}
			for (SmallUnit value : blockMap.values()) {
				if (value.tileEntity != null) {
					value.tileEntity.onChunkUnloaded();
				}
			}
		}
		try {
			close();
		} catch (Throwable ignored) {
		}
	}
}
