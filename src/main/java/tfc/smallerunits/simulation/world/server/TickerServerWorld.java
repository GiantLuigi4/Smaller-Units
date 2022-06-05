package tfc.smallerunits.simulation.world.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.world.EntityManager;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.simulation.world.SUTickList;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class TickerServerWorld extends ServerLevel implements ITickerWorld {
	private static final NoStorageSource src = NoStorageSource.make();
	private static final LevelStorageSource.LevelStorageAccess noAccess;
	
	static {
		try {
			noAccess = src.createAccess("no");
		} catch (IOException e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	ArrayList<Entity> entitiesAdded = new ArrayList<>();
	ArrayList<Entity> entitiesRemoved = new ArrayList<>();
	
	@Override
	public Level getParent() {
		return parent;
	}
	
	@Override
	public Region getRegion() {
		return region;
	}
	
	@Override
	public ParentLookup getLookup() {
		return lookup;
	}
	
	public TickerServerWorld(MinecraftServer server, ServerLevelData data, ResourceKey<Level> p_8575_, DimensionType dimType, ChunkProgressListener progressListener, ChunkGenerator generator, boolean p_8579_, long p_8580_, List<CustomSpawner> spawners, boolean p_8582_, Level parent, int upb, Region region) throws IOException {
		super(
				server,
				Util.backgroundExecutor(),
				noAccess,
				data,
				p_8575_,
				Holder.direct(dimType),
				progressListener,
				generator,
				p_8579_,
				p_8580_,
				spawners,
				p_8582_
		);
//		this.parentU = parentU;
		this.parent = parent;
		this.upb = upb;
		this.chunkSource = new TickerChunkCache(
				this, noAccess,
				null, getStructureManager(),
				Util.backgroundExecutor(),
				generator,
				0, 0,
				true,
				progressListener, (pPos, pStatus) -> {
		}, () -> null,
				upb
		);
		this.region = region;
		this.blockTicks = new SUTickList<>(null, null);
		this.fluidTicks = new SUTickList<>(null, null);
		lookup = pos -> lookupTemp.getState(pos);
		lookupTemp = pos -> Blocks.VOID_AIR.defaultBlockState();
		this.entityManager = new EntityManager<>(this, Entity.class, new EntityCallbacks(), new EntityStorage(this, noAccess.getDimensionPath(p_8575_).resolve("entities"), server.getFixerUpper(), server.forceSynchronousWrites(), server));
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(this));
	}
	
	@Override
	protected void finalize() throws Throwable {
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(this));
		super.finalize();
	}
	
	@Override
	public void SU$removeEntity(Entity pEntity) {
		if (!entitiesRemoved.contains(pEntity)) entitiesRemoved.add(pEntity);
	}
	
	public final Level parent;
	//	public final UnitSpace parentU;
	public final Region region;
	int upb;
	public final Map<BlockPos, BlockState> cache = new Object2ObjectLinkedOpenHashMap<>();
	public ParentLookup lookup;
	public ParentLookup lookupTemp;
	ArrayList<Entity> entities = new ArrayList<>();
	
	@Override
	public void SU$removeEntity(UUID uuid) {
		SU$removeEntity(getEntity(uuid));
	}
	
	@Nullable
	@Override
	public Entity getEntity(UUID pUniqueId) {
		for (Entity entity : entities) {
			if (entity.getUUID().equals(pUniqueId)) { // TODO: make this smarter
				return entity;
			}
		}
		return null;
	}
	
	@Override
	public float getShade(Direction pDirection, boolean pShade) {
		return parent.getShade(pDirection, pShade);
	}
	
	@Override
	public boolean isOutsideBuildHeight(int pY) {
		return false;
	}
	
	@Override
	public LevelChunk getChunkAt(BlockPos pPos) {
		int pX = SectionPos.blockToSectionCoord(pPos.getX());
//		int pY = SectionPos.blockToSectionCoord(pPos.getY());
		int pY = 0;
		int pZ = SectionPos.blockToSectionCoord(pPos.getZ());
		ChunkAccess chunkaccess = ((TickerChunkCache) this.getChunkSource()).getChunk(pX, pY, pZ, ChunkStatus.FULL, true);
		return (LevelChunk) chunkaccess;
	}
	
	@Override
	public BlockState getBlockState(BlockPos pPos) {
		return getChunkAt(pPos).getBlockState(new BlockPos(pPos.getX(), pPos.getY(), pPos.getZ()));
	}
	
	@Override
	public LevelChunk getChunk(int pChunkX, int pChunkZ) {
		return super.getChunk(pChunkX, pChunkZ);
	}
	
	@Nullable
	@Override
	public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull) {
		return super.getChunk(pX, pZ, pRequiredStatus, pNonnull);
	}
	
	@Override
	public void setBlockEntity(BlockEntity pBlockEntity) {
		super.setBlockEntity(pBlockEntity);
	}
	
	@Override
	public void addFreshBlockEntities(Collection<BlockEntity> beList) {
		super.addFreshBlockEntities(beList);
	}
	
	boolean isLoaded = false;
	
	@Override
	public int getUPB() {
		return upb;
	}
	
	HashMap<Entity, ServerEntity> serverEntityHashMap = new HashMap<>();
	
	int nextId = 0;
	
	@Override
	public boolean addFreshEntity(Entity pEntity) {
//		int firstOpen = -1;
//		int prev = -1;
//		for (Entity entity : entities) {
//			if (firstOpen != prev) {
//				break;
//			}
//			firstOpen++;
//			prev = entity.getId();
//		}
//		if (firstOpen != -1) pEntity.setId(firstOpen + 1);
//		else pEntity.setId(0);
		pEntity.setId(nextId++);
		
		entities.add(pEntity);
		
		return super.addFreshEntity(pEntity);
	}
	
	public boolean hasChunksAt(int pFromX, int pFromZ, int pToX, int pToZ) {
		// TODO
		return true;
	}
	
	@Nullable
	@Override
	public Entity getEntity(int pId) {
		for (Entity entity : entities) {
			if (entity.getId() == pId) return entity;
		}
		return null;
	}
	
	@Override
	public void removeEntityComplete(Entity p_8865_, boolean keepData) {
		if (entities.contains(p_8865_)) entities.remove(p_8865_);
		if (!entitiesRemoved.contains(p_8865_)) entitiesRemoved.add(p_8865_);
		super.removeEntityComplete(p_8865_, keepData);
	}
	
	@Override
	public void removeEntity(Entity p_8868_, boolean keepData) {
		if (entities.contains(p_8868_)) entities.remove(p_8868_);
		if (!entitiesRemoved.contains(p_8868_)) entitiesRemoved.add(p_8868_);
		super.removeEntity(p_8868_, keepData);
	}
	
	@Override
	public LevelEntityGetter<Entity> getEntities() {
		return new LevelEntityGetter<Entity>() {
			public Entity get(int p_156931_) {
				for (Entity entity : entities) {
					if (entity.getId() == p_156931_) return entity; // TODO: be not dumb
				}
				return null;
			}
			
			@javax.annotation.Nullable
			public Entity get(UUID pUuid) {
				for (Entity entity : entities) {
					if (entity.getUUID().equals(pUuid)) return entity; // TODO: be not dumb
				}
				return null;
			}
			
			public Iterable<Entity> getAll() {
				return entities;
			}
			
			public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156935_, Consumer<U> p_156936_) {
				for (Entity entity : entities) {
					if (p_156935_.getBaseClass().isInstance(entity)) {
						p_156936_.accept((U) entity);
					}
				}
			}
			
			public void get(AABB p_156937_, Consumer<Entity> p_156938_) {
				for (Entity entity : entities) {
					if (p_156937_.intersects(entity.getBoundingBox())) {
						p_156938_.accept(entity); // this seems slow, but ok mojang
					}
				}
			}
			
			public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156932_, AABB p_156933_, Consumer<U> p_156934_) {
				// ?
				for (Entity entity : entities) {
					if (p_156933_.intersects(p_156933_)) {
						if (p_156932_.getBaseClass().isInstance(entity)) {
							p_156934_.accept((U) entity);
						}
					}
				}
			}
		};
	}
	
	@Override
	public Iterable<Entity> getAllEntities() {
		return entities;
	}
	
	// ???
	private void tickSUBlock(BlockPos pos) {
		getBlockState(pos).tick(this, pos, this.random);
	}
	
	public void setFromSync(ChunkPos cp, int cy, int x, int y, int z, BlockState state, HashMap<ChunkPos, ChunkAccess> accessHashMap, ArrayList<BlockPos> positions) {
		BlockPos rp = region.pos.toBlockPos();
		int xo = ((cp.x * 16) / upb) + (x / upb);
		int yo = ((cy * 16) / upb) + (y / upb);
		int zo = ((cp.z * 16) / upb) + (z / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac;
		// vertical lookups shouldn't be too expensive
		if (!accessHashMap.containsKey(new ChunkPos(parentPos))) {
			ac = parent.getChunkAt(parentPos);
			accessHashMap.put(new ChunkPos(parentPos), ac);
			if (!positions.contains(parentPos)) {
				ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
				positions.add(parentPos);
			}
		} else ac = accessHashMap.get(new ChunkPos(parentPos));
		
		ISUCapability cap = SUCapabilityManager.getCapability((LevelChunk) ac);
		UnitSpace space = cap.getUnit(parentPos);
		if (space == null) {
			space = cap.getOrMakeUnit(parentPos);
			space.setUpb(upb);
		}
		BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(cp.getWorldPosition());
		vc = vc.getSubChunk(cy);
		vc.setBlockFast(new BlockPos(x, y, z), state);
		
		((SUCapableChunk) ac).SU$markDirty(parentPos);
	}
	
	public CompoundTag getTicksIn(BlockPos myPosInTheLevel, BlockPos offset) {
		CompoundTag tag = new CompoundTag();
		AABB box = new AABB(myPosInTheLevel, offset);
		{
			CompoundTag blockTicks = new CompoundTag();
			ArrayList<ScheduledTick<Block>> ticks = ((SUTickList) this.blockTicks).getTicksInArea(box);
			Registry<Block> blockRegistry = parent.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
			for (ScheduledTick<Block> tick : ticks) {
				CompoundTag tag1 = new CompoundTag();
				tag1.putLong("ttime", tick.triggerTick() - getGameTime());
				tag1.putString("ttype", blockRegistry.getKey(tick.type()).toString());
				tag1.putByte("tpriority", (byte) tick.priority().ordinal());
				tag1.putLong("tsub", (byte) tick.subTickOrder());
				blockTicks.put(tick.pos().toShortString().replace(" ", ""), tag1);
			}
			tag.put("blocks", blockTicks);
		}
		{
			CompoundTag blockTicks = new CompoundTag();
			ArrayList<ScheduledTick<Fluid>> ticks = ((SUTickList) this.fluidTicks).getTicksInArea(box);
			Registry<Fluid> fluidRegistry = parent.registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
			for (ScheduledTick<Fluid> tick : ticks) {
				CompoundTag tag1 = new CompoundTag();
				tag1.putLong("ttime", tick.triggerTick() - getGameTime());
				tag1.putString("ttype", fluidRegistry.getKey(tick.type()).toString());
				tag1.putByte("tpriority", (byte) tick.priority().ordinal());
				tag1.putByte("tsub", (byte) tick.subTickOrder());
				blockTicks.put(tick.pos().toShortString().replace(" ", ""), tag1);
			}
			tag.put("fluids", blockTicks);
		}
//		((SUTickList) blockTicks).clearBox(box);
//		((SUTickList) fluidTicks).clearBox(box);
		return tag;
	}
	
	public void loadTicks(CompoundTag tag) {
		Registry<Block> blockRegistry = parent.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
		Registry<Fluid> fluidRegistry = parent.registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
		CompoundTag blocks = tag.getCompound("blocks");
		for (String allKey : blocks.getAllKeys()) {
			CompoundTag tick = blocks.getCompound(allKey);
			long time = tick.getLong("ttime" + getGameTime());
			ResourceLocation regName = new ResourceLocation(tick.getString("ttype"));
			Block type = blockRegistry.get(regName);
			int priority = tick.getByte("tpriority");
			long sub = tick.getLong("tsub");
			String[] pos = allKey.split(",");
			int x = Integer.parseInt(pos[0]);
			int y = Integer.parseInt(pos[1]);
			int z = Integer.parseInt(pos[2]);
			blockTicks.schedule(new ScheduledTick<>(
					type, new BlockPos(x, y, z),
					time, TickPriority.values()[priority], sub
			));
		}
		CompoundTag fluids = tag.getCompound("blocks");
		for (String allKey : fluids.getAllKeys()) {
			CompoundTag tick = fluids.getCompound(allKey);
			long time = tick.getLong("ttime" + getGameTime());
			ResourceLocation regName = new ResourceLocation(tick.getString("ttype"));
			Fluid type = fluidRegistry.get(regName);
			int priority = tick.getByte("tpriority");
			long sub = tick.getLong("tsub");
			String[] pos = allKey.split(",");
			int x = Integer.parseInt(pos[0]);
			int y = Integer.parseInt(pos[1]);
			int z = Integer.parseInt(pos[2]);
			fluidTicks.schedule(new ScheduledTick<>(
					type, new BlockPos(x, y, z),
					time, TickPriority.values()[priority], sub
			));
		}
	}
	
	@Override
	public long getGameTime() {
		return parent.getGameTime();
	}
	
	@Override
	public RegistryAccess registryAccess() {
		if (parent == null) return super.registryAccess();
		return parent.registryAccess();
	}
	
	public void clear(BlockPos myPosInTheLevel, BlockPos offset) {
		for (int x = myPosInTheLevel.getX(); x < offset.getX(); x++) {
			for (int y = myPosInTheLevel.getY(); y < offset.getY(); y++) {
				for (int z = myPosInTheLevel.getZ(); z < offset.getZ(); z++) {
					BlockPos pz = new BlockPos(x, y, z);
					BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(pz);
					vc.setBlockFast(new BlockPos(x, pz.getY(), z), null);
				}
			}
		}
	}
	
	@Override
	public void handleRemoval() {
		for (Entity entity : entities.toArray(new Entity[0])) {
			if (entity.isRemoved()) {
				entities.remove(entity);
			}
		}
	}
	
	@Override
	public void removeEntity(Entity entity) {
		entities.remove(entity);
		super.removeEntity(entity);
	}
	
	@Override
	public void blockEntityChanged(BlockPos pPos) {
		BasicVerticalChunk vc = (BasicVerticalChunk) getChunk(pPos);
		vc.beChanges.add(vc.getBlockEntity(pPos));
	}
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft) {
		if (upb == 0) return;
		
		if (!getServer().isReady()) return;
		if (!isLoaded) return;
		
		NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(region.pos, upb));
		
		resetEmptyTime();
		super.tick(pHasTimeLeft);
		getChunkSource().pollTask();
//		if (getLightEngine() instanceof ThreadedLevelLightEngine) {
//			((ThreadedLevelLightEngine) getLightEngine()).tryScheduleUpdate();
//		} else {
//			// TODO: ?
//			getLightEngine().runUpdates(2, true, true);
//		}
		
		for (Entity entity : entitiesRemoved) {
			removeEntity(entity);
		}
		entitiesRemoved.clear();
		
		for (BasicVerticalChunk[] column : ((TickerChunkCache) chunkSource).columns) {
			List<ServerPlayer> players = null;
			if (column == null) continue;
			for (BasicVerticalChunk basicVerticalChunk : column) {
				if (basicVerticalChunk == null) continue;
				if (players == null) {
					players = getChunkSource().chunkMap.getPlayers(basicVerticalChunk.getPos(), false);
					for (ServerPlayer player : players) {
						// TODO: do this properly
						try {
							getChunkSource().chunkMap.move(player);
						} catch (Throwable ignored) {
						}
//						if (!basicVerticalChunk.isTrackedBy(player)) {
////							basicVerticalChunk.setTracked(player);
////							((UnitChunkMap) getChunkSource().chunkMap).updateChunkTracking(
////									player,
////									basicVerticalChunk.getPos(),
////									new MutableObject<>(),
////									false, true
////							);
//							getChunkSource().chunkMap.move(player);
//						} else {
////							basicVerticalChunk.setTracked(player);
////							((UnitChunkMap) getChunkSource().chunkMap).updateChunkTracking(
////									player,
////									basicVerticalChunk.getPos(),
////									new MutableObject<>(),
////									true, true
////							);
//							getChunkSource().chunkMap.move(player);
//						}
					}
//					for (ServerPlayer player : basicVerticalChunk.getPlayersTracking()) {
////						((UnitChunkMap) getChunkSource().chunkMap).updateChunkTracking(
////								player,
////								basicVerticalChunk.getPos(),
////								new MutableObject<>(),
////								true, false
////						);
//					}
//					basicVerticalChunk.swapTracks();
				}
				
				NetworkingHacks.unitPos.remove();

//				for (BlockEntity beChange : basicVerticalChunk.beChanges) {
//					beChange.setRemoved();
//				}
				for (BlockPos pos : basicVerticalChunk.besRemoved) {
					BlockEntity be = basicVerticalChunk.getBlockEntity(pos);
					if (be != null && !be.isRemoved())
						be.setRemoved();
				}
				basicVerticalChunk.beChanges.clear();
			}
		}


//		for (BasicVerticalChunk[] column : ((TickerChunkCache) chunkSource).columns) {
//			if (column == null) continue;
//			// TODO: check only dirty chunks
//			for (BasicVerticalChunk basicVerticalChunk : column) {
//				if (basicVerticalChunk == null) continue;
//				if (!basicVerticalChunk.updated.isEmpty()) {
//					// TODO: mark parent dirty
////					if (basicVerticalChunk.yPos == 2) {
//					ArrayList<Pair<BlockPos, BlockState>> updates = new ArrayList<>();
//					for (BlockPos pos : basicVerticalChunk.updated)
//						updates.add(Pair.of(new BlockPos(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), basicVerticalChunk.getBlockState(pos)));
//					basicVerticalChunk.updated.clear();
//					basicVerticalChunk.setUnsaved(false);
//					UpdateStatesS2C packet = new UpdateStatesS2C(
//							region.pos, updates,
//							upb, basicVerticalChunk.getPos(),
//							basicVerticalChunk.yPos
//					);
//					BlockPos rp = region.pos.toBlockPos();
//					double x = rp.getX() + basicVerticalChunk.getPos().getMinBlockX() + 8;
//					double y = rp.getY() + (basicVerticalChunk.yPos << 4) + 8;
//					double z = rp.getZ() + basicVerticalChunk.getPos().getMinBlockZ() + 8;
//					ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
//					LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
//					chunk.setUnsaved(true);
//					SUNetworkRegistry.NETWORK_INSTANCE.send(
////							PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
//							PacketDistributor.ALL.noArg(),
//							packet
//					);
////					}
//				} else {
//					if (!basicVerticalChunk.beChanges.isEmpty()) {
//						/* toArray helps to prevent CMEs */
//						for (BlockEntity beChange : basicVerticalChunk.beChanges.toArray(new BlockEntity[0])) {
//							if (beChange == null) continue;
//							CompoundTag tg = new CompoundTag();
//							tg.put("data", beChange.getUpdateTag());
//							tg.putString("id", beChange.getType().getRegistryName().toString());
//							SpawningBlockEntitiesS2C packet = new SpawningBlockEntitiesS2C(
//									region.pos, tg,
//									upb, basicVerticalChunk.getPos(),
//									basicVerticalChunk.yPos,
//									beChange.getBlockPos()
//							);
//							BlockPos rp = region.pos.toBlockPos();
//							double x = rp.getX() + basicVerticalChunk.getPos().getMinBlockX() + 8;
//							double y = rp.getY() + (basicVerticalChunk.yPos << 4) + 8;
//							double z = rp.getZ() + basicVerticalChunk.getPos().getMinBlockZ() + 8;
//							ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
//							LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
//							chunk.setUnsaved(true);
//							SUNetworkRegistry.NETWORK_INSTANCE.send(
////									PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
//									PacketDistributor.ALL.noArg(),
//									packet
//							);
//						}
//						basicVerticalChunk.beChanges.clear();
//					}
//					if (!basicVerticalChunk.besRemoved.isEmpty()) {
//						for (BlockPos beChange : basicVerticalChunk.besRemoved) {
//							DeleteBlockEntityS2C packet = new DeleteBlockEntityS2C(
//									region.pos,
//									upb, basicVerticalChunk.getPos(),
//									basicVerticalChunk.yPos,
//									beChange
//							);
//							BlockPos rp = region.pos.toBlockPos();
//							double x = rp.getX() + basicVerticalChunk.getPos().getMinBlockX() + 8;
//							double y = rp.getY() + (basicVerticalChunk.yPos << 4) + 8;
//							double z = rp.getZ() + basicVerticalChunk.getPos().getMinBlockZ() + 8;
//							ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
//							LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
//							chunk.setUnsaved(true);
//							SUNetworkRegistry.NETWORK_INSTANCE.send(
////									PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
//									PacketDistributor.ALL.noArg(),
//									packet
//							);
//						}
//						basicVerticalChunk.besRemoved.clear();
//					}
//				}
//			}
//		}
//		if (!entitiesAdded.isEmpty()) {
//			for (Entity entity : entitiesAdded) {
//				ChunkPos cp0 = new ChunkPos(new BlockPos(entity.getBlockX(), 0, entity.getBlockZ()));
//				int cy = SectionPos.blockToSectionCoord(entity.getBlockY());
//				CompoundTag tg = new CompoundTag();
//				tg.put("data", entity.serializeNBT());
//				tg.putInt("id", entity.getId());
//				SpawnEntityPacketS2C packet = new SpawnEntityPacketS2C(
//						region.pos, upb, cp0, cy,
//						tg // TODO: figure this out
//				);
//				BlockPos rp = region.pos.toBlockPos();
//				double x = rp.getX() + cp0.getMinBlockX() + 8;
//				double y = rp.getY() + (cy << 4) + 8;
//				double z = rp.getZ() + cp0.getMinBlockZ() + 8;
//				ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
//				LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
//				chunk.setUnsaved(true);
//				SUNetworkRegistry.NETWORK_INSTANCE.send(
////						PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
//						PacketDistributor.ALL.noArg(),
//						packet
//				);
//			}
//			entitiesAdded.clear();
//		}
//		if (!entitiesRemoved.isEmpty()) {
//			for (Entity entity : entitiesRemoved) {
//				ChunkPos cp0 = new ChunkPos(new BlockPos(entity.getBlockX(), 0, entity.getBlockZ()));
//				int cy = SectionPos.blockToSectionCoord(entity.getBlockY());
//				RemoveEntityPacketS2C packet = new RemoveEntityPacketS2C(
//						region.pos, upb, cp0, cy,
//						entity.getId()
//				);
//				BlockPos rp = region.pos.toBlockPos();
//				double x = rp.getX() + cp0.getMinBlockX() + 8;
//				double y = rp.getY() + (cy << 4) + 8;
//				double z = rp.getZ() + cp0.getMinBlockZ() + 8;
//				ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
//				LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
//				chunk.setUnsaved(true);
//				SUNetworkRegistry.NETWORK_INSTANCE.send(
////						PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
//						PacketDistributor.ALL.noArg(),
//						packet
//				);
//			}
//			entitiesRemoved.clear();
//		}
//		if (!entities.isEmpty()) {
//			for (Entity entity : entities) {
//				if (!entity.getEntityData().isDirty())
//					continue;
//				ChunkPos cp0 = new ChunkPos(new BlockPos(entity.getBlockX(), 0, entity.getBlockZ()));
//				int cy = SectionPos.blockToSectionCoord(entity.getBlockY());
//				SyncEntityPacketS2C packet = new SyncEntityPacketS2C(
//						region.pos, upb, cp0, cy,
//						entity.getId(), entity
//				);
//				BlockPos rp = region.pos.toBlockPos();
//				double x = rp.getX() + cp0.getMinBlockX() + 8;
//				double y = rp.getY() + (cy << 4) + 8;
//				double z = rp.getZ() + cp0.getMinBlockZ() + 8;
//				ChunkPos cp = new ChunkPos(new BlockPos(x, y, z));
//				LevelChunk chunk = parent.getChunkAt(cp.getWorldPosition());
//				chunk.setUnsaved(true);
//				SUNetworkRegistry.NETWORK_INSTANCE.send(
////						PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(x, y, z, 2560, parent.dimension())),
//						PacketDistributor.ALL.noArg(),
//						packet
//				);
//			}
//		}
	}
	
	public BlockHitResult collectShape(Vec3 start, Vec3
			end, Function<BlockPos, Boolean> simpleChecker, BiFunction<BlockPos, BlockState, BlockHitResult> boxFiller,
									   int upbInt) {
		BlockHitResult closest = null;
		double d = Double.POSITIVE_INFINITY;
		
		int minX = (int) Math.floor(Math.min(start.x, end.x)) - 1;
		int minY = (int) Math.floor(Math.min(start.y, end.y)) - 1;
		int minZ = (int) Math.floor(Math.min(start.z, end.z)) - 1;
		int maxX = (int) Math.ceil(Math.max(start.x, end.x)) + 1;
		int maxY = (int) Math.ceil(Math.max(start.y, end.y)) + 1;
		int maxZ = (int) Math.ceil(Math.max(start.z, end.z)) + 1;
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					BlockState state = getBlockState(new BlockPos(x, y, z));
					if (state.isAir()) continue;
					if (simpleChecker.apply(new BlockPos(x, y, z))) {
						BlockHitResult result = boxFiller.apply(new BlockPos(x, y, z), state);
						if (result != null) {
							double dd = result.getLocation().distanceTo(start);
							if (dd < d) {
								d = dd;
								closest = result;
							}
						}
					}
				}
			}
		}
		
		if (closest == null) return BlockHitResult.miss(end, Direction.UP, new BlockPos(end)); // TODO
		return closest;
	}
	
	@Override
	public BlockHitResult clip(ClipContext pContext) {
		// I prefer this method over vanilla's method
		Vec3 fStartVec = pContext.getFrom();
		Vec3 endVec = pContext.getTo();
		return collectShape(
				pContext.getFrom(),
				pContext.getTo(),
				(pos) -> {
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
					AABB box = new AABB(
//								x / upbDouble, y / upbDouble, z / upbDouble,
//								(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
							x, y, z,
							x + 1, y + 1, z + 1
					);
					return box.contains(fStartVec) || box.clip(fStartVec, endVec).isPresent();
				}, (pos, state) -> {
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
					VoxelShape sp = state.getShape(this, pos);
					return sp.clip(pContext.getFrom(), pContext.getTo(), pos);
//					for (AABB toAabb : sp.toAabbs()) {
//						toAabb = toAabb.move(x, y, z);
//						UnitBox b = new UnitBox(
//								toAabb.minX / upbDouble,
//								toAabb.minY / upbDouble,
//								toAabb.minZ / upbDouble,
//								toAabb.maxX / upbDouble,
//								toAabb.maxY / upbDouble,
//								toAabb.maxZ / upbDouble,
//								new BlockPos(x, y, z)
//						);
//					}
				},
				upb
		);
	}
	
	public void setLoaded() {
		isLoaded = true;
		lookupTemp = pos -> {
			BlockPos bp = region.pos.toBlockPos().offset(
					// TODO: double check this
					Math.floor(pos.getX() / (double) upb),
					Math.floor(pos.getY() / (double) upb),
					Math.floor(pos.getZ() / (double) upb)
			);
			if (cache.containsKey(bp))
				return cache.get(bp);
//			if (!parent.isLoaded(bp)) // TODO: check if there's a way to do this which doesn't cripple the server
//				return Blocks.VOID_AIR.defaultBlockState();
//			ChunkPos cp = new ChunkPos(bp);
//			if (parent.getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null)
//				return Blocks.VOID_AIR.defaultBlockState();
			if (!getServer().isReady())
				return Blocks.VOID_AIR.defaultBlockState();
			BlockState state = parent.getBlockState(bp);
//			if (state.equals(Blocks.VOID_AIR.defaultBlockState()))
//				return state;
			cache.put(bp, state);
			return state;
		};
	}
	
	public void invalidateCache() {
		cache.clear();
	}
	
	public int getUnitsPerBlock() {
		return upb;
	}
	
	// yes, this is necessary
	// no, I don't know why java is like this
	public class EntityCallbacks extends ServerLevel.EntityCallbacks {
		public EntityCallbacks() {
		}
	}
}
