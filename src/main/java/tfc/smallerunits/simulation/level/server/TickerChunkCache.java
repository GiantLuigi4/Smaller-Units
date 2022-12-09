package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.WorldStitcher;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.chunk.VChunkLookup;
import tfc.smallerunits.simulation.level.ITickerChunkCache;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.UnitChunkHolder;
import tfc.smallerunits.simulation.light.NotThreadedSULightManager;

import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class TickerChunkCache extends ServerChunkCache implements ITickerChunkCache {
	public final BasicVerticalChunk[][] columns;
	private final EmptyLevelChunk empty;
	// TODO: make this not needed
	int upb;
	
	public TickerChunkCache(ServerLevel p_184009_, LevelStorageSource.LevelStorageAccess p_184010_, DataFixer p_184011_, StructureManager p_184012_, Executor p_184013_, ChunkGenerator p_184014_, int p_184015_, int p_184016_, boolean p_184017_, ChunkProgressListener p_184018_, ChunkStatusUpdateListener p_184019_, Supplier<DimensionDataStorage> p_184020_, int upb) {
		super(p_184009_, p_184010_, p_184011_, p_184012_, p_184013_, p_184014_, p_184015_, p_184016_, p_184017_, p_184018_, p_184019_, p_184020_);
		this.chunkMap = new UnitChunkMap(p_184009_, p_184010_, p_184011_, p_184012_, p_184013_, this.mainThreadProcessor, this, p_184014_, p_184018_, p_184019_, p_184020_, p_184015_, p_184017_);
		this.upb = upb;
		columns = new BasicVerticalChunk[33 * 33 * upb * upb][];
		empty = new EmptyLevelChunk(this.level, new ChunkPos(0, 0), Holder.Reference.createStandAlone(this.level.registryAccess().registry(Registry.BIOME_REGISTRY).get(), Biomes.THE_VOID));
		lightEngine = new NotThreadedSULightManager(this, this.chunkMap, true);
	}
	
	private final ObjectOpenHashBigSet<ChunkHolder> holders = new ObjectOpenHashBigSet<>();
	
	@Override
	public boolean hasChunk(int pX, int pZ) {
		// TODO:
		return (!(getChunk(pX, pZ, ChunkStatus.FULL, false) instanceof EmptyLevelChunk));
	}
	
	@Override
	public void broadcastAndSend(Entity pEntity, Packet<?> pPacket) {
		NetworkingHacks.LevelDescriptor descriptor = maybeRemoveUnitPos(pEntity, pPacket);
		super.broadcastAndSend(pEntity, pPacket);
		NetworkingHacks.unitPos.set(descriptor);
	}
	
	public NetworkingHacks.LevelDescriptor maybeRemoveUnitPos(Entity pEntity, Packet<?> pPacket) {
		NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
		if (pPacket instanceof ClientboundAnimatePacket) {
			// TODO: check if the player should actually be in the unit space or not
			if (pEntity instanceof ServerPlayer) {
				NetworkingHacks.unitPos.remove();
			}
		}
		return descriptor;
	}
	
	@Override
	public void broadcast(Entity pEntity, Packet<?> pPacket) {
		NetworkingHacks.LevelDescriptor descriptor = maybeRemoveUnitPos(pEntity, pPacket);
		super.broadcast(pEntity, pPacket);
		NetworkingHacks.unitPos.set(descriptor);
	}
	
	@Nullable
	@Override
	public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		return getChunk(pChunkX, 0, pChunkZ, pRequiredStatus, pLoad);
	}
	
	private final ObjectOpenHashBigSet<LevelChunk> allChunks = new ObjectOpenHashBigSet<>();
	
	@Override
	public void removeEntity(Entity pEntity) {
		super.removeEntity(pEntity);
		((ITickerLevel) level).SU$removeEntity(pEntity);
	}
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft, boolean p_201914_ /* what? */) {
		int tickCount = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		synchronized (allChunks) {
//			for (int i = 0; i < allChunks.size(); i++) {
//				LevelChunk chunk = allChunks.get(i);
//				if (chunk != null) {
//					level.tickChunk(chunk, tickCount);
//					((BasicVerticalChunk) chunk).randomTick();
//				} else {
//					LOGGER.warn("A chunk was null");
//				}
//			}
			for (LevelChunk allChunk : allChunks) {
				level.tickChunk(allChunk, tickCount);
				((BasicVerticalChunk) allChunk).randomTick();
			}
		}
		
		super.tick(pHasTimeLeft, false);
		
		synchronized (holders) {
			for (ChunkHolder holder : holders) {
				LevelChunk chunk = holder.getTickingChunk();
				if (chunk != null)
					holder.broadcastChanges(chunk);
			}
		}

//		for (BasicVerticalChunk[] column : columns) {
//			if (column == null) continue;
//			for (BasicVerticalChunk basicVerticalChunk : column) {
//				if (basicVerticalChunk == null) continue;
////				level.tickChunk(basicVerticalChunk, 100);
//				basicVerticalChunk.randomTick();
//			}
//		}
	}
	
	public Iterable<ChunkHolder> getChunks() {
		return holders;
	}
	
	public ParentLookup getLookup() {
		return ((TickerServerLevel) level).lookup;
	}
	
	public ChunkAccess getChunk(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		if (pChunkX >= (upb * 32) || pChunkZ >= (upb * 32) || pChunkZ < 0 || pChunkX < 0 || pChunkY < 0 || pChunkY >= (upb * 32)) {
			// TODO: fix
//			if (pChunkY < 0) {
//				RegionPos regionPos = ((ITickerWorld) level).getRegion().pos;
//
//				ITickerWorld tickerLevel = (ITickerWorld) level;
//				if (tickerLevel.getParent() instanceof RegionalAttachments attachments) {
//					// TODO: do this more properly
//					RegionPos rPos = new RegionPos(regionPos.x, regionPos.y - 1, regionPos.z);
//					Region r = attachments.SU$getRegion(rPos);
//					if (r == null) {
//						LOGGER.warn("Region@" + rPos + " was null");
//						return empty;
//					}
//					Level lvl;
//					if (tickerLevel.getParent() instanceof ServerLevel)
//						lvl = r.getServerWorld(level.getServer(), level, upb);
//					else
//						lvl = r.getClientWorld(level, upb);
//					return lvl.getChunk(pChunkX, pChunkZ, pRequiredStatus);
//				}
//			}
			
			LevelChunk neighborChunk = WorldStitcher.getChunk(pChunkX, pChunkY, pChunkZ, (ITickerLevel) level, this, upb, pRequiredStatus, pLoad);
			if (neighborChunk != null) return neighborChunk;
			
			Region r = ((TickerServerLevel) level).region;
			RegionPos pos = r.pos;
			
			int x = pChunkX < 0 ? -1 : ((pChunkX > upb) ? 1 : 0);
			int y = pChunkY < 0 ? -1 : ((pChunkY > upb) ? 1 : 0);
			int z = pChunkZ < 0 ? -1 : ((pChunkZ > upb) ? 1 : 0);
			pos = new RegionPos(
					pos.x + x,
					pos.y + y,
					pos.z + z
			);
			
			pChunkX = ((pChunkX < 0) ? pChunkX + upb : ((pChunkX > upb) ? (pChunkX - (upb * 32)) : pChunkX));
			pChunkY = ((pChunkY < 0) ? pChunkY + upb : ((pChunkY > upb) ? (pChunkX - (upb * 32)) : pChunkY));
			pChunkZ = ((pChunkZ < 0) ? pChunkZ + upb : ((pChunkZ > upb) ? (pChunkX - (upb * 32)) : pChunkZ));
			
			Level parent = ((TickerServerLevel) level).parent;
			Region otherRegion = null;
			Level level = null;
			otherRegion = ((RegionalAttachments) ((ServerChunkCache) parent.getChunkSource()).chunkMap).SU$getRegion(pos);
			if (otherRegion != null)
				level = otherRegion.getServerWorld(this.level.getServer(), (ServerLevel) parent, upb);
			else {
				EmptyLevelChunk chunk = empty;
				return chunk;
			}
			return level.getChunk(pChunkX, pChunkZ);
//			return new EmptyLevelChunk(level, new ChunkPos(pChunkX, pChunkZ));
		}
		if (!pLoad) {
			BasicVerticalChunk[] chunks = columns[pChunkX * (33 * upb) + pChunkZ];
			if (chunks == null) return null;
			return chunks[pChunkY];
		} else {
			BasicVerticalChunk[] ck = columns[pChunkX * (33 * upb) + pChunkZ];
//			if (ck == null) ck = columns[pChunkX * (33 * upb) + pChunkZ] = new BasicVerticalChunk[33 * upb];
			if (ck == null || ck[pChunkY] == null) {
				BasicVerticalChunk vc = createChunk(pChunkY, new ChunkPos(pChunkX, pChunkZ));
				if (ck == null) {
					ck = columns[pChunkX * (33 * upb) + pChunkZ];
					ck[pChunkY] = vc;
				}
//				ck[pChunkY] = new BasicVerticalChunk(
//						level, new ChunkPos(pChunkX, pChunkZ), pChunkY,
//						new VChunkLookup(
//								this, pChunkY, ck,
//								new ChunkPos(pChunkX, pChunkZ)
//						), getLookup(), upb
//				);
//				synchronized (allChunks) {
//					allChunks.add(ck[pChunkY]);
//				}
//				UnitChunkHolder holder = new UnitChunkHolder(ck[pChunkY].getPos(), 0, level, level.getLightEngine(), (a, b, c, d) -> {
//				}, chunkMap, ck[pChunkY]);
//				synchronized (holders) {
//					holders.add(holder);
//				}
//				ck[pChunkY].holder = holder;
			}
			return ck[pChunkY];
		}
	}
	
	@Override
	public BasicVerticalChunk createChunk(int yPos, ChunkPos ckPos) {
		int pChunkX = ckPos.x;
		int pChunkZ = ckPos.z;
		BasicVerticalChunk[] ck = columns[pChunkX * (33 * upb) + pChunkZ];
		if (ck == null) ck = columns[pChunkX * (33 * upb) + pChunkZ] = new BasicVerticalChunk[33 * upb];
		ck[yPos] = new BasicVerticalChunk(
				level, new ChunkPos(pChunkX, pChunkZ), yPos,
				new VChunkLookup(
						this, yPos, ck,
						new ChunkPos(pChunkX, pChunkZ), upb * 32
				), getLookup(), upb
		);
		synchronized (allChunks) {
			allChunks.add(ck[yPos]);
		}
		UnitChunkHolder holder = new UnitChunkHolder(ck[yPos].getPos(), 0, level, level.getLightEngine(), (a, b, c, d) -> {
		}, chunkMap, ck[yPos], yPos);
		synchronized (holders) {
			holders.add(holder);
		}
		ck[yPos].holder = holder;
		return ck[yPos];
	}
	
	@Override
	public EmptyLevelChunk getEmpty() {
		return empty;
	}
	
	@Override
	public ITickerLevel tickerLevel() {
		return (ITickerLevel) level;
	}
}
