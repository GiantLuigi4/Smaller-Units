package tfc.smallerunits.simulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.level.ITickerChunkCache;
import tfc.smallerunits.simulation.level.ITickerLevel;

import java.lang.ref.WeakReference;

public class WorldStitcher {
	public static LevelChunk getChunk(int pChunkX, int pChunkY, int pChunkZ, ITickerLevel level, ITickerChunkCache chunkCache, int upb, ChunkStatus pRequiredStatus, boolean pLoad, WeakReference<Level>[] neighbors) {
		if (pChunkX >= (upb * 32) || pChunkZ >= (upb * 32) || pChunkZ < 0 || pChunkX < 0 || pChunkY < 0 || pChunkY >= (upb * 32)) {
			RegionPos regionPos = level.getRegion().pos;
			
			if (level.getParent() instanceof RegionalAttachments attachments) {
				int oX = offset(pChunkX, upb);
				int oY = offset(pChunkY, upb);
				int oZ = offset(pChunkZ, upb);
				
				int xO = chunkRelative(pChunkX, upb);
				int yO = chunkRelative(pChunkY, upb);
				int zO = chunkRelative(pChunkZ, upb);
				
				int ord = -1;
				if (oX == 1) {
					ord = 0;
					yO = 0;
					zO = 0;
				} else if (oX == -1) {
					ord = 1;
					yO = 0;
					zO = 0;
				} else if (oY == 1) {
					ord = 2;
					xO = 0;
					zO = 0;
				} else if (oY == -1) {
					ord = 3;
					xO = 0;
					zO = 0;
				} else if (oZ == 1) {
					ord = 4;
					xO = 0;
					yO = 0;
				} else if (oZ == -1) {
					ord = 5;
					xO = 0;
					yO = 0;
				}
				
				Level lvl = null;
				if (ord != -1) {
					if (neighbors[ord] == null) lvl = null;
					else lvl = neighbors[ord].get();
				}
				if (lvl == null) {
					RegionPos rPos = new RegionPos(regionPos.x + oX, regionPos.y + oY, regionPos.z + oZ);
					Region r = attachments.SU$getRegion(rPos);
					if (r == null) {
						if (!pLoad) {
							return null;
						}
//						if (!FMLEnvironment.production)
//							Loggers.CHUNK_CACHE.warn("Region@" + rPos + " was null");
						return chunkCache.getEmpty();
					}
					
					if (level instanceof ServerLevel) {
						lvl = r.getServerWorld(level.getParent().getServer(), (ServerLevel) level.getParent(), upb);
					} else {
						lvl = r.getClientWorld(level.getParent(), upb);
					}
				}
				if (ord != -1) {
					if (lvl != null && (neighbors[ord] == null || neighbors[ord].get() == null)) {
						neighbors[ord] = new WeakReference<>(lvl);
					}
				}
				
				if (lvl != null) {
					ChunkAccess chunk = ((ITickerLevel) lvl).getChunk(pChunkX + xO, pChunkY + yO, pChunkZ + zO, pRequiredStatus, pLoad);
					return (LevelChunk) chunk;
				}
			}
		}
		
		return null;
	}
	
	public static int offset(int ckX, int upb) {
		// TODO: do this properly
		if (ckX >= (upb * 32)) return 1;
		if (ckX < 0) return -1;
		return 0;
	}
	
	public static int chunkRelative(int ckX, int upb) {
		// TODO: do this properly
		int o = 0;
		if (ckX >= (upb * 32)) o = -(upb * 32);
		if (ckX < 0) o = upb * 32;
		return o;
	}
}
