package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.world.WorldEvent;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.api.PositionUtils;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.data.access.EntityAccessor;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.EntityManager;
import tfc.smallerunits.simulation.level.ITickerChunkCache;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.SUTickList;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.scale.ResizingUtils;
import tfc.smallerunits.utils.storage.GroupMap;
import tfc.smallerunits.utils.storage.VecMap;
import tfc.smallerunits.utils.threading.ThreadLocals;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.*;

@SuppressWarnings("removal")
public class TickerServerLevel extends ServerLevel implements ITickerLevel {
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
	
	public final GroupMap<Pair<BlockState, VecMap<VoxelShape>>> cache = new GroupMap<>(2);
	
	@Override
	public Level getParent() {
		return parent.get();
	}
	
	@Override
	public Region getRegion() {
		return region;
	}
	
	@Override
	public ParentLookup getLookup() {
		return lookup;
	}
	
	//	ArrayList<Entity> entitiesAdded = new ArrayList<>();
	ArrayList<Entity> entitiesRemoved = new ArrayList<>();
	
	@Override
	protected void finalize() throws Throwable {
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(this));
		super.finalize();
	}
	
	public final WeakReference<Level> parent;
	//	public final UnitSpace parentU;
	public final Region region;
	private final ArrayList<Runnable> completeOnTick = new ArrayList<>();
	int upb;
	
	public TickerServerLevel(MinecraftServer server, ServerLevelData data, ResourceKey<Level> p_8575_, DimensionType dimType, ChunkProgressListener progressListener, ChunkGenerator generator, boolean p_8579_, long p_8580_, List<CustomSpawner> spawners, boolean p_8582_, Level parent, int upb, Region region) {
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
		this.parent = new WeakReference<>(parent);
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
		
		ThreadLocal<WeakReference<LevelChunk>> lastChunk = new ThreadLocal<>();
		lookup = (pos) -> {
			if (cache.containsKey(pos)) {
//				BlockState state = cache.get(bp).getFirst();
//				VoxelShape shape = state.getCollisionShape(parent, bp);
				// TODO: empty shape check
				return cache.get(pos).getFirst();
			}
			
			if (!getServer().isReady())
				return Blocks.VOID_AIR.defaultBlockState();
			if (!this.parent.get().isLoaded(pos))
				return Blocks.VOID_AIR.defaultBlockState();
			
			ChunkPos ckPos = new ChunkPos(pos);
			WeakReference<LevelChunk> chunkRef = lastChunk.get();
			LevelChunk ck;
			if (chunkRef == null || (ck = chunkRef.get()) == null)
				lastChunk.set(new WeakReference<>(ck = this.parent.get().getChunkAt(pos)));
			else if (!chunkRef.get().getPos().equals(ckPos))
				lastChunk.set(new WeakReference<>(ck = this.parent.get().getChunkAt(pos)));
			
			BlockState state = ck.getBlockState(pos);
			cache.put(pos, Pair.of(state, new VecMap<>(2)));
			return state;
		};
		isLoaded = true;
		this.entityManager = new EntityManager<>(this, Entity.class, new EntityCallbacks(), new EntityStorage(this, noAccess.getDimensionPath(p_8575_).resolve("entities"), server.getFixerUpper(), server.forceSynchronousWrites(), server));
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(this));
	}
	
	int randomTickCount = Integer.MIN_VALUE;
	
	@Override
	public void playSound(@Nullable Player pPlayer, Entity pEntity, SoundEvent pEvent, SoundSource pCategory, float pVolume, float pPitch) {
		this.playSound(pPlayer, pEntity.getX(), pEntity.getY(), pEntity.getZ(), pEvent, pCategory, pVolume, pPitch);
	}
	
	@Override
	public void playSound(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
		double scl = 1f / upb;
		BlockPos pos = getRegion().pos.toBlockPos();
		pX *= scl;
		pY *= scl;
		pZ *= scl;
		pX += pos.getX();
		pY += pos.getY();
		pZ += pos.getZ();
		double finalPX = pX;
		double finalPY = pY;
		double finalPZ = pZ;
		if (ResizingUtils.isResizingModPresent() && pPlayer != null) // TODO: I probably need to manually send sounds to each player
			scl *= 1 / ResizingUtils.getSize(pPlayer);
		if (scl > 1) scl = 1 / scl;
		double finalScl = scl;
		completeOnTick.add(() -> {
			Level lvl = parent.get();
			if (lvl == null) return;
			for (Player player : lvl.players()) {
				if (player == pPlayer) continue;
				
				double fScl = finalScl;
				fScl *= 1 / ResizingUtils.getSize(player);
				sendSoundEvent(pPlayer, finalPX, finalPY, finalPZ, pSound, pCategory, (float) (pVolume * fScl), pPitch);
			}
		});
	}
	
	public void sendSoundEvent(@javax.annotation.Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
		net.minecraftforge.event.entity.PlaySoundAtEntityEvent event = net.minecraftforge.event.ForgeEventFactory.onPlaySoundAtEntity(pPlayer, pSound, pCategory, pVolume, pPitch);
		if (event.isCanceled() || event.getSound() == null) return;
		pSound = event.getSound();
		pCategory = event.getCategory();
		pVolume = event.getVolume();
		broadcastTo(pPlayer, pX, pY, pZ, pVolume > 1.0F ? (double) (16.0F * pVolume) : 16.0D, this.dimension(), new ClientboundSoundPacket(pSound, pCategory, pX, pY, pZ, pVolume, pPitch));
	}
	
	public void broadcastTo(@javax.annotation.Nullable Player pExcept, double pX, double pY, double pZ, double pRadius, ResourceKey<Level> pDimension, Packet<?> pPacket) {
		Level lvl = parent.get();
		if (lvl == null) return;
		for (int i = 0; i < lvl.players().size(); ++i) {
			ServerPlayer serverplayer = (ServerPlayer) lvl.players().get(i);
			if (serverplayer != pExcept && serverplayer.level.dimension() == pDimension) {
				double d0 = pX - serverplayer.getX();
				double d1 = pY - serverplayer.getY();
				double d2 = pZ - serverplayer.getZ();
				if (d0 * d0 + d1 * d1 + d2 * d2 < pRadius * pRadius) {
					serverplayer.connection.send(pPacket);
				}
			}
		}
	}
	
	public <T extends ParticleOptions> int sendParticles(T pType, double pPosX, double pPosY, double pPosZ, int pParticleCount, double pXOffset, double pYOffset, double pZOffset, double pSpeed) {
		ClientboundLevelParticlesPacket clientboundlevelparticlespacket = new ClientboundLevelParticlesPacket(pType, false, pPosX, pPosY, pPosZ, (float) pXOffset, (float) pYOffset, (float) pZOffset, (float) pSpeed, pParticleCount);
		int i = 0;
		
		Level lvl = parent.get();
		if (lvl == null) return 0;
		
		for (int j = 0; j < lvl.players().size(); ++j) {
			ServerPlayer serverplayer = (ServerPlayer) lvl.players().get(j);
			if (this.sendParticles(serverplayer, false, pPosX, pPosY, pPosZ, clientboundlevelparticlespacket)) {
				++i;
			}
		}
		
		return i;
	}
	
	@Override
	public boolean sendParticles(ServerPlayer pPlayer, boolean pLongDistance, double pPosX, double pPosY, double pPosZ, Packet<?> pPacket) {
		Level lvl = parent.get();
		if (lvl == null) return false;
		if (pPlayer.getLevel() == lvl || pPlayer.level == this) {
			BlockPos blockpos = pPlayer.blockPosition();
			
			double scl = 1f / upb;
			BlockPos pos = getRegion().pos.toBlockPos();
			pPosX *= scl;
			pPosY *= scl;
			pPosZ *= scl;
			pPosX += pos.getX();
			pPosY += pos.getY();
			pPosZ += pos.getZ();
			
			if (blockpos.closerToCenterThan(new Vec3(pPosX, pPosY, pPosZ), pLongDistance ? (512.0D * scl) : (32.0D * scl))) {
				pPlayer.connection.send(pPacket);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	@Override
	public void SU$removeEntity(Entity pEntity) {
		if (!entitiesRemoved.contains(pEntity)) entitiesRemoved.add(pEntity);
	}
	
	@Override
	public Holder<Biome> getBiome(BlockPos pos) {
		BlockPos bp = region.pos.toBlockPos().offset(
				// TODO: double check this
				Math.floor(pos.getX() / (double) upb),
				Math.floor(pos.getY() / (double) upb),
				Math.floor(pos.getZ() / (double) upb)
		);
		return parent.get().getBiome(bp.offset(region.pos.toBlockPos()));
	}
	
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
		return parent.get().getShade(pDirection, pShade);
	}
	
	@Override
	public boolean isOutsideBuildHeight(int pY) {
		return false;
	}
	
	@Override
	public int getMinBuildHeight() {
		return -32;
	}
	
	@Override
	public int getMaxBuildHeight() {
		return upb * 512 + 32;
	}
	
	@Override
	public int getSectionsCount() {
		return getMaxSection() - getMinSection();
	}
	
	@Override
	public int getMinSection() {
		return 0;
	}
	
	@Override
	public int getSectionIndexFromSectionY(int pSectionIndex) {
		return pSectionIndex;
	}
	
	@Override
	public int getMaxSection() {
		return upb * 512;
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
	
	public LevelChunk getChunkAtNoLoad(BlockPos pPos) {
		int pX = SectionPos.blockToSectionCoord(pPos.getX());
//		int pY = SectionPos.blockToSectionCoord(pPos.getY());
		int pY = 0;
		int pZ = SectionPos.blockToSectionCoord(pPos.getZ());
		ChunkAccess chunkaccess = ((TickerChunkCache) this.getChunkSource()).getChunk(pX, pY, pZ, ChunkStatus.FULL, false);
		return (LevelChunk) chunkaccess;
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
//		pEntity.setId(nextId++);

//		entities.add(pEntity);
		
		Level lvl = parent.get();
		if (lvl == null) return false;
		
		NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
		NetworkingHacks.setPos(null);
		
		Entity entity = pEntity.changeDimension((ServerLevel) lvl, new ITeleporter() {
			@Override
			public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
				PortalInfo portalinfo = getPortalInfo(entity, destWorld, null);
				
				currentWorld.getProfiler().popPush("reloading");
				Entity newEntity = entity.getType().create(destWorld);
				if (newEntity != null) {
					ResizingUtils.resizeForUnit(entity, 1f / upb);
					
					newEntity.restoreFrom(entity);
					newEntity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, newEntity.getXRot());
					newEntity.setDeltaMovement(portalinfo.speed);
					destWorld.addFreshEntity(newEntity);
				}
				
				return newEntity;
			}
			
			@Nullable
			@Override
			public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
				Vec3 pos = entity.getPosition(1);
				BlockPos bp = region.pos.toBlockPos();
				pos = pos.scale(1d / upb);
				pos = pos.add(bp.getX(), bp.getY(), bp.getZ());
				return new PortalInfo(
						pos,
						entity.getDeltaMovement(),
						entity.getYRot(),
						entity.getXRot()
				);
			}
			
			@Override
			public boolean isVanilla() {
				return false;
			}
			
			@Override
			public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
				return false;
			}
		});
		
		NetworkingHacks.setPos(descriptor);
		
		return entity != null;
//		return super.addFreshEntity(pEntity);
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
		if (p_8865_ == null) {
			Loggers.WORLD_LOGGER.warn("Removing a null entity, this should not happen");
			return;
			// TODO: log stacktrace
		}
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
					if (p_156933_.intersects(entity.getBoundingBox())) {
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
	
	@Override
	public void setFromSync(ChunkPos cp, int cy, int x, int y, int z, BlockState state, ArrayList<BlockPos> positions, HashMap<SectionPos, ChunkAccess> chunkCache) {
		BlockPos rp = region.pos.toBlockPos();
		int xo = ((cp.x * 16) / upb) + (x / upb);
		int yo = ((cy * 16) / upb) + (y / upb);
		int zo = ((cp.z * 16) / upb) + (z / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac;
		SectionPos pos = SectionPos.of(parentPos);
		// vertical lookups shouldn't be too expensive
		if (!chunkCache.containsKey(pos)) {
			ac = parent.get().getChunkAt(parentPos);
			chunkCache.put(pos, ac);
			if (!positions.contains(parentPos)) {
				ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
				positions.add(parentPos);
			}
		} else ac = chunkCache.get(pos);
		
		ISUCapability cap = SUCapabilityManager.getCapability((LevelChunk) ac);
		UnitSpace space = cap.getUnit(parentPos);
		if (space == null) {
			space = cap.getOrMakeUnit(parentPos);
			space.setUpb(upb);
		}
		BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(cp.getWorldPosition());
		vc = vc.getSubChunk(cy);
		vc.setBlockFast(new BlockPos(x, y, z), state, chunkCache);
		
		((SUCapableChunk) ac).SU$markDirty(parentPos);
	}
	
	public CompoundTag getTicksIn(BlockPos myPosInTheLevel, BlockPos offset) {
		CompoundTag tag = new CompoundTag();
		AABB box = new AABB(myPosInTheLevel, offset);
		{
			CompoundTag blockTicks = new CompoundTag();
			ArrayList<ScheduledTick<Block>> ticks = ((SUTickList) this.blockTicks).getTicksInArea(box);
			Registry<Block> blockRegistry = parent.get().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
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
			Registry<Fluid> fluidRegistry = parent.get().registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
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
		Registry<Block> blockRegistry = parent.get().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY);
		Registry<Fluid> fluidRegistry = parent.get().registryAccess().registryOrThrow(Registry.FLUID_REGISTRY);
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
		return parent.get().getGameTime();
	}
	
	@Override
	public RegistryAccess registryAccess() {
		if (parent == null) return super.registryAccess();
		return parent.get().registryAccess();
	}
	
	public void clear(BlockPos myPosInTheLevel, BlockPos offset) {
		HashMap<SectionPos, ChunkAccess> cache = new HashMap<>();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		for (int x = myPosInTheLevel.getX(); x < offset.getX(); x++) {
			for (int z = myPosInTheLevel.getZ(); z < offset.getZ(); z++) {
				int pX = SectionPos.blockToSectionCoord(x);
				int pZ = SectionPos.blockToSectionCoord(z);
				BasicVerticalChunk vc = (BasicVerticalChunk) getChunk(pX, pZ, ChunkStatus.FULL, false);
				if (vc == null) continue;
				
				for (int y = myPosInTheLevel.getY(); y < offset.getY(); y++) {
					mutableBlockPos.set(x, y, z);
					vc.setBlockFast(mutableBlockPos, null, cache);
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
		BlockEntity be = vc.getBlockEntity(pPos);
		if (be == null) return;
		vc.beChanges.add(be);
		BlockPos parentPos = PositionUtils.getParentPosPrecise(pPos, vc);
		LevelChunk ac = getParent().getChunkAt(parentPos);
		ac.setUnsaved(true);
	}
	
	@Override
	public void tickChunk(LevelChunk pChunk, int pRandomTickSpeed) {
//		ChunkPos chunkpos = pChunk.getPos();
//		boolean flag = this.isRaining();
//		int i = chunkpos.getMinBlockX();
//		int j = chunkpos.getMinBlockZ();
//		ProfilerFiller profilerfiller = this.getProfiler();
//
//		profilerfiller.push("iceandsnow");
//		if (this.random.nextInt(16) == 0) {
//			BlockPos blockpos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(i, 0, j, 15));
//			BlockPos blockpos3 = blockpos2.below();
//			Biome biome = this.getBiome(blockpos2).value();
//			if (this.isAreaLoaded(blockpos2, 1)) // Forge: check area to avoid loading neighbors in unloaded chunks
//				if (biome.shouldFreeze(this, blockpos3)) {
//					this.setBlockAndUpdate(blockpos3, Blocks.ICE.defaultBlockState());
//				}
//
//			if (flag) {
//				if (biome.shouldSnow(this, blockpos2)) {
//					this.setBlockAndUpdate(blockpos2, Blocks.SNOW.defaultBlockState());
//				}
//
//				BlockState blockstate1 = this.getBlockState(blockpos3);
//				Biome.Precipitation biome$precipitation = biome.getPrecipitation();
//				if (biome$precipitation == Biome.Precipitation.RAIN && biome.coldEnoughToSnow(blockpos3)) {
//					biome$precipitation = Biome.Precipitation.SNOW;
//				}
//
//				blockstate1.getBlock().handlePrecipitation(blockstate1, this, blockpos3, biome$precipitation);
//			}
//		}
//		profilerfiller.pop();
	}
	
	@Override
	public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
		// TODO: does this even exist on server?
//		super.playLocalSound(pX, pY, pZ, pSound, pCategory, pVolume, pPitch, pDistanceDelay);
		double scl = 1f / upb;
		BlockPos pos = getRegion().pos.toBlockPos();
		pX *= scl;
		pY *= scl;
		pZ *= scl;
		pX += pos.getX();
		pY += pos.getY();
		pZ += pos.getZ();
		double finalPX = pX;
		double finalPY = pY;
		double finalPZ = pZ;
//		if (ResizingUtils.isResizingModPresent() && pPlayer != null) // TODO: I probably need to manually send sounds to each player
//			scl *= 1 / ResizingUtils.getSize(pPlayer);
		if (scl > 1) scl = 1 / scl;
		double finalScl = scl;
		completeOnTick.add(() -> {
			parent.get().playLocalSound(finalPX, finalPY, finalPZ, pSound, pCategory, (float) (pVolume * finalScl), pPitch, pDistanceDelay);
		});
	}
	
	public BlockHitResult collectShape(Vec3 start, Vec3 end, Function<AABB, Boolean> simpleChecker, BiFunction<BlockPos, BlockState, BlockHitResult> boxFiller, int upbInt) {
		BlockHitResult closest = null;
		double d = Double.POSITIVE_INFINITY;
		
		int minX = (int) Math.floor(Math.min(start.x, end.x)) - 1;
		int minY = (int) Math.floor(Math.min(start.y, end.y)) - 1;
		int minZ = (int) Math.floor(Math.min(start.z, end.z)) - 1;
		int maxX = (int) Math.ceil(Math.max(start.x, end.x)) + 1;
		int maxY = (int) Math.ceil(Math.max(start.y, end.y)) + 1;
		int maxZ = (int) Math.ceil(Math.max(start.z, end.z)) + 1;
		
		// TODO: there are better ways to do this
		for (int x = minX; x < maxX; x += 16) {
			for (int z = minZ; z < maxZ; z += 16) {
				for (int y = minY; y < maxY; y += 16) {
					AABB box = new AABB(
							x, y, z,
							x + 16, y + 16, z + 16
					);
					if (simpleChecker.apply(box)) {
						for (int x0 = 0; x0 < 16; x0++) {
							int x1 = x + x0;
							for (int z0 = 0; z0 < 16; z0++) {
								int z1 = z + z0;
								
								int pX = SectionPos.blockToSectionCoord(x1);
								int pZ = SectionPos.blockToSectionCoord(z1);
								ChunkAccess chunk = getChunk(pX, pZ, ChunkStatus.FULL, false);
								if (chunk == null) continue;
								
								for (int y0 = 0; y0 < 16; y0++) {
									int y1 = y + y0;
									box = new AABB(
											x1, y1, z1,
											x1 + 1, y1 + 1, z1 + 1
									);
									if (simpleChecker.apply(box)) {
										BlockPos pos = new BlockPos(x1, y1, z1);
										BlockState state = chunk.getBlockState(pos);
										if (state.isAir()) continue;
										BlockHitResult result = boxFiller.apply(pos, state);
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
					}
				}
			}
		}
		
		if (closest == null) return BlockHitResult.miss(end, Direction.UP, new BlockPos(end)); // TODO
		return closest;
	}
	
	protected BlockHitResult runTrace(VoxelShape sp, ClipContext pContext, BlockPos pos) {
		BlockHitResult result = sp.clip(pContext.getFrom(), pContext.getTo(), pos);
		if (result == null) return result;
		if (!result.getType().equals(HitResult.Type.MISS)) {
			Vec3 off = pContext.getFrom().subtract(pContext.getTo());
			off = off.normalize().scale(0.1);
			Vec3 st = result.getLocation().add(off);
			Vec3 ed = result.getLocation().subtract(off);
//						return sp.clip(st, pContext.getTo(), pos);
			return sp.clip(st, ed, pos);
		}
		return result;
	}
	
	@Override
	public BlockHitResult clip(ClipContext pContext) {
		// I prefer this method over vanilla's method
		Vec3 fStartVec = pContext.getFrom();
		Vec3 endVec = pContext.getTo();
		return collectShape(
				pContext.getFrom(),
				pContext.getTo(),
				(box) -> {
					return box.contains(fStartVec) || box.clip(fStartVec, endVec).isPresent();
				}, (pos, state) -> {
					VoxelShape sp = switch (pContext.block) {
						case VISUAL -> state.getVisualShape(this, pos, pContext.collisionContext);
						case COLLIDER -> state.getCollisionShape(this, pos, pContext.collisionContext);
						case OUTLINE -> state.getShape(this, pos, pContext.collisionContext);
						default -> state.getCollisionShape(this, pos, pContext.collisionContext); // TODO
					};
					BlockHitResult result = runTrace(sp, pContext, pos);
					if (result != null && result.getType() != HitResult.Type.MISS) return result;
					if (pContext.fluid.canPick(state.getFluidState()))
						result = runTrace(state.getFluidState().getShape(this, pos), pContext, pos);
					return result;
				},
				upb
		);
	}
	
	public void setLoaded() {
//		isLoaded = true;
//		lookupTemp = pos -> {
//			BlockPos bp = region.pos.toBlockPos().offset(
//					// TODO: double check this
//					Math.floor(pos.getX() / (double) upb),
//					Math.floor(pos.getY() / (double) upb),
//					Math.floor(pos.getZ() / (double) upb)
//			);
//			if (cache.containsKey(bp)) {
////				BlockState state = cache.get(bp).getFirst();
////				VoxelShape shape = state.getCollisionShape(parent, bp);
//				// TODO: empty shape check
//				return cache.get(bp).getFirst();
//			}
////			if (!parent.get().isLoaded(bp)) // TODO: check if there's a way to do this which doesn't cripple the server
////				return Blocks.VOID_AIR.defaultBlockState();
////			ChunkPos cp = new ChunkPos(bp);
////			if (parent.get().getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null)
////				return Blocks.VOID_AIR.defaultBlockState();
//			if (!getServer().isReady())
//				return Blocks.VOID_AIR.defaultBlockState();
//			BlockState state = parent.get().getBlockState(bp);
////			if (state.equals(Blocks.VOID_AIR.defaultBlockState()))
////				return state;
//			cache.put(bp, Pair.of(state, new VecMap<>(2)));
//			return state;
//		};
	}
	
	@Override
	public void invalidateCache(BlockPos pos) {
		cache.remove(pos);
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
	
	@Override
	public String toString() {
		return "TickerServerLevel[" + ((ServerLevelData) this.getLevelData()).getLevelName() + "]@[" + region.pos.x + "," + region.pos.y + "," + region.pos.z + "]";
	}
	
	// TODO: try to optimize or shrink this?
	@Override
	public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft) {
		if (this.isOutsideBuildHeight(pPos)) {
			return false;
		} else if (!this.isClientSide && this.isDebug()) {
			return false;
		} else {
			LevelChunk levelchunk = this.getChunkAt(pPos);
			Block block = pState.getBlock();
			
			BlockPos actualPos = pPos;
			pPos = new BlockPos(pPos.getX() & 15, pPos.getY(), pPos.getZ() & 15);
			net.minecraftforge.common.util.BlockSnapshot blockSnapshot = null;
			if (this.captureBlockSnapshots && !this.isClientSide) {
				blockSnapshot = net.minecraftforge.common.util.BlockSnapshot.create(this.dimension(), this, actualPos, pFlags);
				this.capturedBlockSnapshots.add(blockSnapshot);
			}
			
			BlockState old = levelchunk.getBlockState(pPos);
			int oldLight = old.getLightEmission(this, actualPos);
			int oldOpacity = old.getLightBlock(this, actualPos);
			
			BlockState blockstate = levelchunk.setBlockState(pPos, pState, (pFlags & 64) != 0);
			if (blockstate == null) {
				if (blockSnapshot != null) this.capturedBlockSnapshots.remove(blockSnapshot);
				return false;
			} else {
				BlockState blockstate1 = levelchunk.getBlockState(pPos);
				if ((pFlags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getLightBlock(this, pPos) != oldOpacity || blockstate1.getLightEmission(this, pPos) != oldLight || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion())) {
					this.getProfiler().push("queueCheckLight");
					this.getChunkSource().getLightEngine().checkBlock(actualPos);
					this.getProfiler().pop();
				}
				
				if (blockSnapshot == null) // Don't notify clients or update physics while capturing blockstates
					this.markAndNotifyBlock(actualPos, levelchunk, blockstate, pState, pFlags, pRecursionLeft);
				
				return true;
			}
		}
	}
	
	@Override
	public BlockState getBlockState(BlockPos pPos) {
		LevelChunk chunk = getChunkAtNoLoad(pPos);
		if (chunk == null) {
			BlockPos parentPos = PositionUtils.getParentPos(pPos, this);
			BlockState parentState = lookup.getState(parentPos);
			if (parentState.isAir() || parentState.getBlock() instanceof UnitSpaceBlock) {
				return Blocks.VOID_AIR.defaultBlockState();
			}
			return tfc.smallerunits.Registry.UNIT_EDGE.get().defaultBlockState();
		}
		return chunk.getBlockState(new BlockPos(pPos.getX() & 15, pPos.getY(), pPos.getZ() & 15));
	}
	
	@Override
	public FluidState getFluidState(BlockPos pPos) {
//		return Blocks.AIR.defaultBlockState().getFluidState();
		LevelChunk chunk = getChunkAtNoLoad(pPos);
		if (chunk == null) return Fluids.EMPTY.defaultFluidState();
		return chunk.getFluidState(new BlockPos(pPos.getX() & 15, pPos.getY(), pPos.getZ() & 15));
	}
	
	@Override
	public void setBlockEntity(BlockEntity pBlockEntity) {
		LevelChunk chunk = this.getChunkAt(pBlockEntity.getBlockPos());
		pBlockEntity.worldPosition = chunk.getPos().getWorldPosition().offset(pBlockEntity.getBlockPos().getX() & 15, pBlockEntity.getBlockPos().getY(), pBlockEntity.getBlockPos().getZ() & 15);
		// TODO: figure out of deserialization and reserialization is necessary or not
		chunk.addAndRegisterBlockEntity(pBlockEntity);
	}
	
	@Override
	public void runBlockEvents() {
		this.blockEventsToReschedule.clear();
		
		while (!this.blockEvents.isEmpty()) {
			BlockEventData blockeventdata = this.blockEvents.removeFirst();
			if (this.shouldTickBlocksAt(ChunkPos.asLong(blockeventdata.pos()))) {
				if (this.doBlockEvent(blockeventdata)) {
					for (Player player : parent.get().players()) {
						BlockPos parentPos = PositionUtils.getParentPos(blockeventdata.pos(), this);
						if (
								player.distanceToSqr(parentPos.getX(), parentPos.getY(), parentPos.getZ()) <
//										((64 / Math.sqrt(upb)))
										(64) // TODO: scale this based off player scale and upb
						) {
							((ServerPlayer) player).connection.send(new ClientboundBlockEventPacket(blockeventdata.pos(), blockeventdata.block(), blockeventdata.paramA(), blockeventdata.paramB()));
						}
					}
				}
			} else {
				this.blockEventsToReschedule.add(blockeventdata);
			}
		}
		
		this.blockEvents.addAll(this.blockEventsToReschedule);
	}
	
	@Override
	public ChunkAccess getChunk(int x, int y, int z, ChunkStatus pRequiredStatus, boolean pLoad) {
		ITickerChunkCache chunkCache = (ITickerChunkCache) getChunkSource();
		return chunkCache.getChunk(x, y, z, pRequiredStatus, pLoad);
	}
	
	@Override
	// nothing to do
	public void markRenderDirty(BlockPos pLevelPos) {
	}
	
	@Override
	public int getBrightness(LightLayer pLightType, BlockPos pBlockPos) {
		BlockPos parentPos = PositionUtils.getParentPos(pBlockPos, this);
		int lt = parent.get().getBrightness(pLightType, parentPos);
		if (pLightType.equals(LightLayer.SKY)) return lt;
		return Math.max(lt, super.getBrightness(pLightType, pBlockPos));
	}
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft) {
		if (upb == 0) return;
		ThreadLocals.posLocal.enableForThread();
		
		randomTickCount = Integer.MIN_VALUE;
		
		if (!isLoaded) return;
		if (!getServer().isReady()) return;
		
		NetworkingHacks.setPos(new NetworkingHacks.LevelDescriptor(region.pos, upb));
		
		resetEmptyTime();
		super.tick(pHasTimeLeft);
		getChunkSource().pollTask();
		
		for (Entity entity : entitiesRemoved) {
			removeEntity(entity);
		}
		entitiesRemoved.clear();
		
		// TODO: optimize this
		for (ChunkHolder holder : ((TickerChunkCache) chunkSource).holders) {
			List<ServerPlayer> players = null;
			if (holder.getTickingChunk() instanceof BasicVerticalChunk basicVerticalChunk) {
				if (players == null) {
					players = getChunkSource().chunkMap.getPlayers(basicVerticalChunk.getPos(), false);
					for (ServerPlayer player : players) {
						// TODO: do this properly
						try {
							PositionalInfo info = new PositionalInfo(player);
							info.adjust(player, this, this.getUPB(), region.pos);
							getChunkSource().chunkMap.move(player);
							info.reset(player);
						} catch (Throwable ignored) {
						}
					}
				}
				
				for (BlockPos pos : basicVerticalChunk.besRemoved) {
					BlockEntity be = basicVerticalChunk.getBlockEntity(pos);
					if (be != null && !be.isRemoved())
						be.setRemoved();
				}
				basicVerticalChunk.beChanges.clear();
			}
		}
//		for (BasicVerticalChunk[] column : ((TickerChunkCache) chunkSource).columns) {
//			List<ServerPlayer> players = null;
//			if (column == null) continue;
//			for (BasicVerticalChunk basicVerticalChunk : column) {
//				if (basicVerticalChunk == null) continue;
//				if (players == null) {
//					players = getChunkSource().chunkMap.getPlayers(basicVerticalChunk.getPos(), false);
//					for (ServerPlayer player : players) {
//						// TODO: do this properly
//						try {
//							getChunkSource().chunkMap.move(player);
//						} catch (Throwable ignored) {
//						}
//					}
//				}
//
//				for (BlockPos pos : basicVerticalChunk.besRemoved) {
//					BlockEntity be = basicVerticalChunk.getBlockEntity(pos);
//					if (be != null && !be.isRemoved())
//						be.setRemoved();
//				}
//				basicVerticalChunk.beChanges.clear();
//			}
//		}
		
		NetworkingHacks.unitPos.remove();
		
		getLightEngine().runUpdates(10000, true, false);
		for (Runnable runnable : completeOnTick) runnable.run();
		completeOnTick.clear();
		
		for (List<Entity> entitiesGrabbedByBlock : entitiesGrabbedByBlocks)
			for (Entity entity : entitiesGrabbedByBlock)
				((EntityAccessor) entity).setMotionScalar(1);
	}
	
	@Override
	public int randomTickCount() {
		if (randomTickCount == Integer.MIN_VALUE)
			randomTickCount = parent.get().getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		return randomTickCount;
	}
	
	@Override
	public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ) {
		return getMaxBuildHeight(); // TODO: do this properly
	}
	
	ArrayList<List<Entity>> entitiesGrabbedByBlocks = new ArrayList<>();
	
	@Override
	public <T extends Entity> List<? extends T> getEntities(EntityTypeTest<Entity, T> p_143281_, Predicate<? super T> p_143282_) {
		// TODO
		return super.getEntities(p_143281_, p_143282_);
	}
	
	@Override
	public List<Entity> getEntities(@Nullable Entity pEntity, AABB pBoundingBox, Predicate<? super Entity> pPredicate) {
		// for simplicity
		return getEntities(EntityTypeTest.forClass(Entity.class), pBoundingBox, pPredicate);
	}
	
	@Override
	public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntityTypeTest, AABB aabb, Predicate<? super T> pPredicate) {
		Level owner = parent.get();
		if (owner != null) {
			List<T> entities = super.getEntities(pEntityTypeTest, aabb, pPredicate);
			
			double upb = this.upb;
			AABB aabb1 = new AABB(0, 0, 0, aabb.getXsize() / upb, aabb.getZsize() / upb, aabb.getZsize() / upb);
			AABB bb = aabb1.move(
					aabb.minX / upb,
					aabb.minY / upb,
					aabb.minZ / upb
			).move(region.pos.toBlockPos().getX(), region.pos.toBlockPos().getY(), region.pos.toBlockPos().getZ());
			List<T> parentEntities = owner.getEntities(pEntityTypeTest, bb, pPredicate);
			
			entitiesGrabbedByBlocks.add((List<Entity>) parentEntities);
			for (T parentEntity : parentEntities)
				((EntityAccessor) parentEntity).setMotionScalar((float) (1 / upb));
			
			entities.addAll(parentEntities);
			return entities;
		}
		
		return super.getEntities(pEntityTypeTest, aabb, pPredicate);
	}
}
