package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.*;
import tfc.smallerunits.data.access.EntityManagerAccessor;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;
import tfc.smallerunits.simulation.level.server.saving.SUSaveWorld;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.UUID;
import java.util.stream.Stream;

//m_157562_(J)V
//m_157568_(J)Z
//m_157577_()V
//m_157582_()V
//m_157587_()Lit/unimi/dsi/fastutil/longs/LongSet;
//m_157554_()V
public class EntityManager<T extends EntityAccess> extends PersistentEntitySectionManager<T> {
	SUSaveWorld world;
	Level level;
	
	public EntityManager(ITickerLevel wld, Class<T> p_157503_, LevelCallback<T> p_157504_, EntityPersistentStorage<T> p_157505_) {
		super(p_157503_, new LevelCallback<T>() {
			@Override
			public void onCreated(T pEntity) {
				p_157504_.onCreated(pEntity);
			}
			
			@Override
			public void onDestroyed(T pEntity) {
				wld.SU$removeEntity((Entity) pEntity);
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
		if (wld instanceof TickerServerLevel tkLvl)
			this.world = tkLvl.saveWorld;
		this.level = (Level) wld;
	}
	
	public Stream<Entity> collectFromFile(File entityFile) {
		try {
			CompoundTag tag = NbtIo.readCompressed(entityFile);
			return EntityType.loadEntitiesRecursive(tag.getList("ents", Tag.TAG_COMPOUND), level);
		} catch (Throwable ignored) {
		}
		return null;
	}
	
	public void addEnt(T ent) {
		long i = SectionPos.asLong(ent.blockPosition());
		//noinspection unchecked
		EntitySection<T> entitysection = ((EntityManagerAccessor<T>) this).getSections().getSection(i);
		if (entitysection == null) {
			entitysection = ((EntityManagerAccessor<T>) this).getSections().getOrCreateSection(i);
		}
		entitysection.updateChunkStatus(Visibility.TICKING);
	}
	
	@Override
	public boolean addNewEntity(T pEntity) {
		addEnt(pEntity);
		return super.addNewEntity(pEntity);
	}
	
	@Override
	public boolean addNewEntityWithoutEvent(T entity) {
		addEnt(entity);
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
	
	protected void saveChunk(EntitySection<T> section, long pos) throws IOException {
		SectionPos sPos = SectionPos.of(pos);
		
		CompoundTag tag = new CompoundTag();
		ListTag ents = new ListTag();
		section.getEntities().forEach((ent) -> {
			if (ent instanceof Entity) {
				ents.add(((Entity) ent).serializeNBT());
			} else {
				throw new RuntimeException("Idk what to do with " + ent.getClass());
			}
		});
		
		tag.put("ents", ents);
		File fl1 = world.getEntityFile(sPos);
		if (!fl1.exists()) fl1.createNewFile();
		NbtIo.writeCompressed(tag, fl1);
	}
	
	@Override
	public void autoSave() {
		for (Long aLong : ((EntityManagerAccessor) this).$getAllChunksToSave()) {
			try {
				saveChunk(((EntityManagerAccessor<T>) this).getSections().getOrCreateSection(aLong), aLong);
			} catch (Throwable ignored) {
				ignored.printStackTrace();
			}
		}
	}
	
	@Override
	public void saveAll() {
		autoSave();
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
