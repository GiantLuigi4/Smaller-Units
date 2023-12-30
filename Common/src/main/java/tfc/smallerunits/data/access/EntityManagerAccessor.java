package tfc.smallerunits.data.access;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySectionStorage;

public interface EntityManagerAccessor<T extends EntityAccess> {
	EntitySectionStorage<T> getSections();
	LongSet $getAllChunksToSave();
}
