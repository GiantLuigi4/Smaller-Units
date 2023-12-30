package tfc.smallerunits.simulation.level.server.saving;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.File;

public class SUDimStorage extends DimensionDataStorage {
	public SUDimStorage(File p_78149_, DataFixer p_78150_) {
		super(p_78149_, p_78150_);
	}
}
