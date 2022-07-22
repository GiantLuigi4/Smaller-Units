package tfc.smallerunits.simulation.world.client;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.chunk.VChunkLookup;
import tfc.smallerunits.simulation.world.ITickerChunkCache;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.utils.IHateTheDistCleaner;

import java.util.function.BooleanSupplier;

public class TickerClientChunkCache extends ClientChunkCache implements ITickerChunkCache {
	public final BasicVerticalChunk[][] columns;
	int upb;
	Level level;
	
	EmptyLevelChunk empty;
	
	public TickerClientChunkCache(ClientLevel pLevel, int pViewDistance, int upb) {
		super(pLevel, pViewDistance);
		level = pLevel;
		this.upb = upb;
		columns = new BasicVerticalChunk[33 * 33 * upb * upb][];
//		this.chunkMap = new UnitChunkMap(p_184009_, p_184010_, p_184011_, p_184012_, p_184013_, this.mainThreadProcessor, this, p_184014_, p_184018_, p_184019_, p_184020_, p_184015_, p_184017_);
		empty = new EmptyLevelChunk(this.level, new ChunkPos(0, 0), Holder.Reference.createStandAlone(this.level.registryAccess().registry(Registry.BIOME_REGISTRY).get(), Biomes.THE_VOID));
	}

//	@Override
//	public void removeEntity(Entity pEntity) {
//		super.removeEntity(pEntity);
//		((ITickerWorld) level).SU$removeEntity(pEntity);
//	}
	
	@Override
	public LevelChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		return getChunk(pChunkX, 0, pChunkZ, pRequiredStatus, pLoad);
	}
	
	@Override
	public void tick(BooleanSupplier pHasTimeLeft, boolean p_201914_ /* what? */) {
		super.tick(pHasTimeLeft, p_201914_);
//		for (BasicVerticalChunk[] column : columns) {
//			if (column == null) continue;
//			for (BasicVerticalChunk basicVerticalChunk : column) {
//				if (basicVerticalChunk == null) continue;
////				level.tickChunk(basicVerticalChunk, 100);
//				basicVerticalChunk.randomTick();
//			}
//		}
	}
	
	public ParentLookup getLookup() {
		return ((ITickerWorld) level).getLookup();
	}
	
	int isRecurring = 0;
	
	public LevelChunk getChunk(int pChunkX, int pChunkY, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
		if (upb == 0) return empty;
		if (pChunkX >= (upb * 32) || pChunkZ >= (upb * 32) || pChunkZ < 0 || pChunkX < 0 || pChunkY < 0 || pChunkY > upb) {
			Region r = ((ITickerWorld) level).getRegion();
			RegionPos pos = r.pos;
			
			int x = pChunkX < 0 ? -1 : ((pChunkX > upb) ? 1 : 0);
			int y = pChunkY < 0 ? -1 : ((pChunkY > upb) ? 1 : 0);
			int z = pChunkZ < 0 ? -1 : ((pChunkZ > upb) ? 1 : 0);
			pos = new RegionPos(
					pos.x + x,
					pos.y + y,
					pos.z + z
			);
			
			if (isRecurring > 5) {
				System.out.println("-- INFO --");
				System.out.println(" cyxz: " + pChunkX + ", " + pChunkY + ", " + pChunkZ);
				System.out.println("  xyz: " + x + ", " + y + ", " + z);
				pChunkX = ((pChunkX < 0) ? pChunkX + upb : ((pChunkX > upb) ? (pChunkX - (upb * 32)) : pChunkX));
				pChunkY = ((pChunkY < 0) ? pChunkY + upb : ((pChunkY > upb) ? (pChunkX - (upb * 32)) : pChunkY));
				pChunkZ = ((pChunkZ < 0) ? pChunkZ + upb : ((pChunkZ > upb) ? (pChunkX - (upb * 32)) : pChunkZ));
				System.out.println("acyxz: " + pChunkX + ", " + pChunkY + ", " + pChunkZ);
				System.out.println("upb: " + upb);
				System.out.println(pos);
				throw new RuntimeException();
			}
			
			pChunkX = ((pChunkX < 0) ? pChunkX + upb : ((pChunkX > upb) ? (pChunkX - (upb * 32)) : pChunkX));
			pChunkY = ((pChunkY < 0) ? pChunkY + upb : ((pChunkY > upb) ? (pChunkX - (upb * 32)) : pChunkY));
			pChunkZ = ((pChunkZ < 0) ? pChunkZ + upb : ((pChunkZ > upb) ? (pChunkX - (upb * 32)) : pChunkZ));
			
			Level parent = ((ITickerWorld) level).getParent();
			Region otherRegion = null;
			Level level = null;
			
			otherRegion = ((RegionalAttachments) ((ITickerWorld) this.level).getParent()).SU$getRegion(pos);
			if (otherRegion != null && IHateTheDistCleaner.isClientLevel(parent)) {
				level = otherRegion.getClientWorld(parent, upb);
			} else {
//				EmptyLevelChunk chunk = new EmptyLevelChunk(this.level, new ChunkPos(pChunkX, pChunkZ), Holder.Reference.createStandAlone(this.level.registryAccess().registry(Registry.BIOME_REGISTRY).get(), Biomes.THE_VOID));
				EmptyLevelChunk chunk = empty;
				return chunk;
			}
			
			isRecurring++;
			LevelChunk chunk = level.getChunk(pChunkX, pChunkZ);
			isRecurring--;
			return chunk;
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
				ck[pChunkY].setClientLightReady(true);
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
		ck[i].setClientLightReady(true);
		
		return ck[i];
	}
}