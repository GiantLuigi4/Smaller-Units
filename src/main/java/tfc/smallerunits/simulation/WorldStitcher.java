package tfc.smallerunits.simulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.simulation.level.ITickerChunkCache;
import tfc.smallerunits.simulation.level.ITickerLevel;

public class WorldStitcher {
	public static LevelChunk getChunk(int pChunkX, int pChunkY, int pChunkZ, ITickerLevel level, ITickerChunkCache chunkCache, int upb, ChunkStatus pRequiredStatus, boolean pLoad) {
		if (pChunkX >= (upb * 32) || pChunkZ >= (upb * 32) || pChunkZ < 0 || pChunkX < 0 || pChunkY < 0 || pChunkY >= (upb * 32)) {
			RegionPos regionPos = level.getRegion().pos;
			
			if (level.getParent() instanceof RegionalAttachments attachments) {
				RegionPos rPos = new RegionPos(regionPos.x + offset(pChunkX, upb), regionPos.y + offset(pChunkY, upb), regionPos.z + offset(pChunkZ, upb));
				Region r = attachments.SU$getRegion(rPos);
				if (r == null) {
					if (!pLoad) {
						return null;
					}
					Loggers.CHUNK_CACHE.warn("Region@" + rPos + " was null");
					return chunkCache.getEmpty();
				}
				Level lvl;
				if (level instanceof ServerLevel) {
					lvl = r.getServerWorld(level.getParent().getServer(), (ServerLevel) level.getParent(), upb);
				} else {
					lvl = r.getClientWorld(level.getParent(), upb);
				}
				if (lvl != null) {
					int xO = chunkRelative(pChunkX, upb);
					int yO = chunkRelative(pChunkY, upb);
					int zO = chunkRelative(pChunkZ, upb);
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
