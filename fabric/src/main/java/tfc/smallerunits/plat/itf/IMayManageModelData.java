package tfc.smallerunits.plat.itf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IMayManageModelData {
	Level getActual();
	
	default Object getModelData(BlockPos offsetPos) {
		return null;
	}
}
