package tfc.smallerunits.simulation.world.server;

import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;
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
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.chunk.VChunkLookup;
import tfc.smallerunits.simulation.world.ITickerChunkCache;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.simulation.world.UnitChunkHolder;

import java.util.ArrayList;
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
	}
	
	@Override
	public void removeEntity(Entity pEntity) {
		super.removeEntity(pEntity);
		((ITickerWorld) level).SU$removeEntity(pEntity);
	}
	
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
	
	ArrayList<ChunkHolder> holders = new ArrayList<>();
	
	BigList<LevelChunk> allChunks = new ObjectBigArrayBigList<>();
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft, boolean p_201914_ /* what? */) {
		int tickCount = level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
		for (int i = 0; i < allChunks.size(); i++) {
			LevelChunk chunk = allChunks.get(i);
			level.tickChunk(chunk, tickCount);
			((BasicVerticalChunk) chunk).randomTick();
		}
		super.tick(pHasTimeLeft, false);
		
		for (ChunkHolder holder : holders) {
			LevelChunk chunk = holder.getTickingChunk();
			if (chunk != null)
				holder.broadcastChanges(chunk);
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
		return ((TickerServerWorld) level).lookup;
	}
	
	public ChunkAccess getChunk(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		if (pChunkX >= (upb * 32) || pChunkZ >= (upb * 32) || pChunkZ < 0 || pChunkX < 0 || pChunkY < 0 || pChunkY > upb) {
			Region r = ((TickerServerWorld) level).region;
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
			
			Level parent = ((TickerServerWorld) level).parent;
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
			if (ck == null) ck = columns[pChunkX * (33 * upb) + pChunkZ] = new BasicVerticalChunk[33 * upb];
			if (ck[pChunkY] == null) {
				ck[pChunkY] = new BasicVerticalChunk(
						level, new ChunkPos(pChunkX, pChunkZ), pChunkY,
						new VChunkLookup(
								this, pChunkY, ck,
								new ChunkPos(pChunkX, pChunkZ)
						), getLookup(), upb
				);
				allChunks.add(ck[pChunkY]);
				UnitChunkHolder holder = new UnitChunkHolder(ck[pChunkY].getPos(), 0, level, level.getLightEngine(), (a, b, c, d) -> {
				}, chunkMap, ck[pChunkY]);
				holders.add(holder);
				ck[pChunkY].holder = holder;
			}
			return ck[pChunkY];
		}
	}
	
	@Override
	public BasicVerticalChunk createChunk(int i, ChunkPos ckPos) {
		int pChunkX = ckPos.x;
		int pChunkZ = ckPos.z;
		BasicVerticalChunk[] ck = columns[pChunkX * (33 * upb) + pChunkZ];
		ck[i] = new BasicVerticalChunk(
				level, new ChunkPos(pChunkX, pChunkZ), i,
				new VChunkLookup(
						this, i, ck,
						new ChunkPos(pChunkX, pChunkZ)
				), getLookup(), upb
		);
		allChunks.add(ck[i]);
		UnitChunkHolder holder = new UnitChunkHolder(ck[i].getPos(), 0, level, level.getLightEngine(), (a, b, c, d) -> {
		}, chunkMap, ck[i]);
		holders.add(holder);
		ck[i].holder = holder;
		return ck[i];
	}
}
