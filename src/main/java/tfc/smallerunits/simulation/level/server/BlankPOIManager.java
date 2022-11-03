package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.LevelHeightAccessor;

import java.nio.file.Path;

public class BlankPOIManager extends PoiManager {
	public BlankPOIManager(Path p_196651_, DataFixer p_196652_, boolean p_196653_, LevelHeightAccessor p_196654_) {
		super(p_196651_, p_196652_, p_196653_, p_196654_);
	}
	
	@Override
	public void add(BlockPos pPos, PoiType pPoiType) {
	}
	
	@Override
	public void remove(BlockPos pPos) {
	}
}
