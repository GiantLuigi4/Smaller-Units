package tfc.smallerunits.data.access;

import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;

public interface DimensionDataStorageAccessor {
	Map<String, SavedData> getStorage();
}
