package tfc.smallerunits.simulation.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

// TODO: figure out why I can't get mojang's one to work
public class NotThreadedSULightManager extends ThreadedLevelLightEngine {
	public NotThreadedSULightManager(LightChunkGetter level, ChunkMap map, boolean sky) {
		super(level, map, sky, null, null);
		this.blockEngine = new SULightEngine(level, LightLayer.BLOCK, null);
	}
	
	@Override
	public void updateSectionStatus(BlockPos p_75835_, boolean pIsEmpty) {
		this.updateSectionStatus(SectionPos.of(p_75835_), pIsEmpty);
	}
	
	public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
		if (this.blockEngine != null) {
			this.blockEngine.updateSectionStatus(pPos, pIsEmpty);
		}
		
		if (this.skyEngine != null) {
			this.skyEngine.updateSectionStatus(pPos, pIsEmpty);
		}
	}
	
	@Override
	public void onBlockEmissionIncrease(BlockPos p_75824_, int p_75825_) {
		if (this.blockEngine != null) {
			this.blockEngine.onBlockEmissionIncrease(p_75824_, p_75825_);
		}
	}
	
	@Override
	public boolean hasLightWork() {
		if (this.skyEngine != null && this.skyEngine.hasLightWork()) {
			return true;
		} else {
			return this.blockEngine != null && this.blockEngine.hasLightWork();
		}
	}
	
	@Override
	public int runUpdates(int maxUpdates, boolean runBlock, boolean runSky) {
		if (this.blockEngine != null && this.skyEngine != null) {
			int i = maxUpdates / 2;
			int j = this.blockEngine.runUpdates(i, runBlock, runSky);
			int k = maxUpdates - i + j;
			int l = this.skyEngine.runUpdates(k, runBlock, runSky);
			return j == 0 && l > 0 ? this.blockEngine.runUpdates(l, runBlock, runSky) : l;
		} else if (this.blockEngine != null) {
			return this.blockEngine.runUpdates(maxUpdates, runBlock, runSky);
		} else {
			return this.skyEngine != null ? this.skyEngine.runUpdates(maxUpdates, runBlock, runSky) : maxUpdates;
		}
	}
	
	@Override
	public void checkBlock(BlockPos pos) {
		if (this.blockEngine != null) this.blockEngine.checkBlock(pos);
		if (this.skyEngine != null) this.skyEngine.checkBlock(pos);
	}
	
	@Override
	public void tryScheduleUpdate() {
	}
}
