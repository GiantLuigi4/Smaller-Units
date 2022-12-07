package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;

// TODO: figure out why I can't get mojang's one to work
public class SULightManager extends LevelLightEngine {
	public SULightManager(LightChunkGetter level, boolean block, boolean sky) {
		super(level, block, sky);
		this.blockEngine = new SULightEngine(level, LightLayer.BLOCK, null);
	}
	
	@Override
	public void updateSectionStatus(BlockPos pos, boolean empty) {
		this.updateSectionStatus(SectionPos.of(pos), empty);
		
	}
	
	@Override
	public void onBlockEmissionIncrease(BlockPos pos, int value) {
		super.onBlockEmissionIncrease(pos, value);
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
