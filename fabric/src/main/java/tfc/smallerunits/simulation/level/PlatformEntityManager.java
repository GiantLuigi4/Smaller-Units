package tfc.smallerunits.simulation.level;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

public abstract class PlatformEntityManager<T extends EntityAccess> extends PersistentEntitySectionManager<T> {
	public PlatformEntityManager(Class<T> p_157503_, LevelCallback<T> p_157504_, EntityPersistentStorage<T> p_157505_) {
		super(p_157503_, p_157504_, p_157505_);
	}
	
	public abstract void addEnt(T ent);
}
