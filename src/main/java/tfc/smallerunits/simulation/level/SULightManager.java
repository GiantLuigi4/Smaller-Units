package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;

// TODO: figure out why I can't get mojang's one to work
public class SULightManager extends LevelLightEngine {
	public SULightManager(LightChunkGetter p_75805_, boolean p_75806_, boolean p_75807_) {
		super(p_75805_, p_75806_, p_75807_);
		this.blockEngine = new SULightEngine(p_75805_, LightLayer.BLOCK, null);
	}
	
	@Override
	public void updateSectionStatus(BlockPos p_75835_, boolean p_75836_) {
		super.updateSectionStatus(p_75835_, p_75836_);
	}
	
	@Override
	public void onBlockEmissionIncrease(BlockPos p_75824_, int p_75825_) {
		super.onBlockEmissionIncrease(p_75824_, p_75825_);
	}
	
	@Override
	public boolean hasLightWork() {
		return super.hasLightWork();
	}
	
	@Override
	public int runUpdates(int p_75809_, boolean p_75810_, boolean p_75811_) {
		return super.runUpdates(p_75809_, p_75810_, p_75811_);
	}
}
