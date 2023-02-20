package tfc.smallerunits.mixin.dangit;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.access.DimensionDataStorageAccessor;

import java.util.Map;

@Mixin(DimensionDataStorage.class)
public class DimensionDataStorageMixin implements DimensionDataStorageAccessor {
	@Shadow @Final private Map<String, SavedData> cache;
	
	public Map<String, SavedData> getStorage() {
		return cache;
	}
}
