package tfc.smallerunits.simulation.light;

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
	public int runUpdates(int maxUpdates, boolean runBlock, boolean runSky) {
		boolean block = blockEngine != null && blockEngine.hasLightWork();
		boolean sky = skyEngine != null && skyEngine.hasLightWork();
		if (block && sky) {
			int i = maxUpdates / 2;
			int j = this.blockEngine.runUpdates(i, runBlock, runSky);
			int k = maxUpdates - i + j;
			int l = this.skyEngine.runUpdates(k, runBlock, runSky);
			return j == 0 && l > 0 ? this.blockEngine.runUpdates(l, runBlock, runSky) : l;
		} else if (block) {
			return this.blockEngine.runUpdates(maxUpdates, runBlock, runSky);
		} else if (sky) {
			return this.skyEngine.runUpdates(maxUpdates, runBlock, runSky);
		}
		return maxUpdates;
	}
}
