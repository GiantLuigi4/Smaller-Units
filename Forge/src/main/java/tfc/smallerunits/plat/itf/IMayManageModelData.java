package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelDataManager;
import net.minecraftforge.common.extensions.IForgeBlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface IMayManageModelData extends IForgeBlockGetter {
	Level getActual();
	
	@Nullable
	@Override
	default ModelDataManager getModelDataManager() {
		return getActual().getModelDataManager();
	}
	
	default Object getModelData(BlockPos offsetPos) {
		ModelData modelData = Objects.requireNonNull(getModelDataManager()).getAt(offsetPos);
		if (modelData == null) modelData = ModelData.EMPTY;
		return modelData;
	}
}
