package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.plat.util.ver.SUModelDataManager;

import java.util.Objects;

public interface IMayManageModelData {
	Level getActual();
	
	@Nullable
	default SUModelDataManager getModelDataManager() {
		return ((IMayManageModelData) getActual()).getModelDataManager();
	}
	
	default Object getModelData(BlockPos offsetPos) {
		IModelData modelData = Objects.requireNonNull(getModelDataManager()).getModelData((Level) getActual(), offsetPos);
		if (modelData == null) modelData = EmptyModelData.INSTANCE;
		return modelData;
	}
}
