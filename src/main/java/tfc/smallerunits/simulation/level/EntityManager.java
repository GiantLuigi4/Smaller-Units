package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.*;

import java.io.IOException;
import java.io.Writer;
import java.util.UUID;
import java.util.stream.Stream;

public class EntityManager<T extends EntityAccess> extends PersistentEntitySectionManager<T> {
	public EntityManager(ITickerLevel wld, Class<T> p_157503_, LevelCallback<T> p_157504_, EntityPersistentStorage<T> p_157505_) {
		super(p_157503_, new LevelCallback<T>() {
			@Override
			public void onCreated(T pEntity) {
				p_157504_.onCreated(pEntity);
			}
			
			@Override
			public void onDestroyed(T pEntity) {
				wld.SU$removeEntity(pEntity.getUUID());
				p_157504_.onDestroyed(pEntity);
			}
			
			@Override
			public void onTickingStart(T pEntity) {
				p_157504_.onTickingStart(pEntity);
			}
			
			@Override
			public void onTickingEnd(T pEntity) {
				p_157504_.onTickingEnd(pEntity);
			}
			
			@Override
			public void onTrackingStart(T pEntity) {
				try {
					p_157504_.onTrackingStart(pEntity);
				} catch (Throwable ignored) {
				}
			}
			
			@Override
			public void onTrackingEnd(T pEntity) {
				p_157504_.onTickingEnd(pEntity);
			}
		}, p_157505_);
	}
	
	@Override
	public boolean addNewEntity(T pEntity) {
		return super.addNewEntity(pEntity);
	}
	
	@Override
	public boolean addNewEntityWithoutEvent(T entity) {
		return super.addNewEntityWithoutEvent(entity);
	}
	
	@Override
	public void addLegacyChunkEntities(Stream<T> pEntities) {
		super.addLegacyChunkEntities(pEntities);
	}
	
	@Override
	public void addWorldGenChunkEntities(Stream<T> pEntities) {
		super.addWorldGenChunkEntities(pEntities);
	}
	
	@Override
	public void updateChunkStatus(ChunkPos pPos, ChunkHolder.FullChunkStatus pStatus) {
		super.updateChunkStatus(pPos, pStatus);
	}
	
	@Override
	public void updateChunkStatus(ChunkPos pPos, Visibility pVisibility) {
		super.updateChunkStatus(pPos, pVisibility);
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	@Override
	public void autoSave() {
		super.autoSave();
	}
	
	@Override
	public void saveAll() {
		super.saveAll();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
	}
	
	@Override
	public boolean isLoaded(UUID p_157551_) {
		// TODO
		return true;
	}
	
	@Override
	public LevelEntityGetter<T> getEntityGetter() {
		return super.getEntityGetter();
	}
	
	@Override
	public boolean canPositionTick(BlockPos p_202168_) {
		return true;
	}
	
	@Override
	public boolean canPositionTick(ChunkPos p_202166_) {
		return true;
	}
	
	@Override
	public boolean areEntitiesLoaded(long p_157508_) {
		// TODO
		return true;
	}
	
	@Override
	public void dumpSections(Writer p_157549_) throws IOException {
		super.dumpSections(p_157549_);
	}
	
	@Override
	public String gatherStats() {
		return super.gatherStats();
	}
}
