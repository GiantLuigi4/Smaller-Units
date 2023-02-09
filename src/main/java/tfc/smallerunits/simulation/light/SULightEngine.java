package tfc.smallerunits.simulation.light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightSectionStorage;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.simulation.level.ITickerChunkCache;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.lighting.LightOffset;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SULightEngine extends LayerLightEngine<BlockLightSectionStorage.BlockDataLayerStorageMap, BlockLightSectionStorage> {
	private static final Direction[] directions = Direction.values();
	private static LightOffset[] kernel;
	
	static {
		int size = 16;
		ArrayList<LightOffset> positions = new ArrayList<>();
		for (int xOff = -size; xOff <= size; xOff++) {
			for (int yOff = -size; yOff <= size; yOff++) {
				for (int zOff = -size; zOff <= size; zOff++) {
					if (xOff == 0 && yOff == 0 && zOff == 0) continue;
					int dist = (Math.abs(xOff) + Math.abs(yOff) + Math.abs(zOff));
					if (dist > 15) continue;
					positions.add(new LightOffset(xOff, yOff, zOff, dist));
				}
			}
		}
		positions.sort((self, other) -> {
			int left = self.dist;
			int right = other.dist;
			return Integer.compare(left, right);
		});
		kernel = positions.toArray(new LightOffset[0]);
	}
	
	Set<BlockPos> positionsToUpdate = new HashSet<>();
	HashMap<SectionPos, LightSection> lightSectionHashMap = new HashMap<>();
	SectionPos lastSection = null;
	LightSection section = null;
	
	ITickerLevel level;
	
	public SULightEngine(LightChunkGetter p_75640_, LightLayer p_75641_, BlockLightSectionStorage p_75642_) {
		super(p_75640_, p_75641_, p_75642_);
		if (p_75640_ instanceof ITickerChunkCache chunkCache)
			this.level = chunkCache.tickerLevel();
	}
	
	@Override
	public int queuedUpdateSize() {
		return positionsToUpdate.size();
	}
	
	// getLightBlockInto
	
	@Override
	protected void checkNeighborsAfterUpdate(long pPos, int pLevel, boolean pIsDecreasing) {
	
	}
	
	@Override
	public int runUpdates(int max, boolean block, boolean sky) {
		if (!block) return 0;
		if (positionsToUpdate.isEmpty()) return 0;
		
		int countDone = 0;
		while (countDone < max) {
			if (positionsToUpdate.isEmpty()) return countDone;
			
			Set<BlockPos> toRemove = new HashSet<>();
			Set<BlockPos> toAdd = new HashSet<>();
			for (BlockPos blockPos : positionsToUpdate) {
				toRemove.add(blockPos);
				countDone++;
				
				for (Direction value : directions) {
					BlockPos updating = blockPos.relative(value);
					if (updating.getX() < 0) continue;
					if (updating.getY() < 0) continue;
					if (updating.getZ() < 0) continue;
					int lv = calcLightValue(updating);
					if (lv > getLightValue(updating) && lv != 0) {
						toAdd.add(updating);
						setLight(updating, (byte) lv);
					}
				}
				
				if (countDone >= max) break;
			}
			if (toRemove.size() == positionsToUpdate.size()) {
				positionsToUpdate = toAdd;
			} else {
				positionsToUpdate.removeAll(toRemove);
				positionsToUpdate.addAll(toAdd);
			}
			
			countDone++;
		}
//		positionsToUpdate.clear();
		return countDone;
	}
	
	public int calcLightValue(BlockPos pPos) {
		BlockGetter level = chunkSource.getLevel();
		BlockState state = level.getBlockState(pPos); // TODO: optimize?
		int neighbor = state.getLightEmission(level, pPos);
		VoxelShape shape = state.getShape(level, pPos);
		int block = state.getLightBlock(level, pPos);
		// TODO: make this be more proper
		for (Direction value : directions) {
			BlockPos pos = pPos.relative(value);
			int lightVal = getLightValue(pos);
			if (state.useShapeForLightOcclusion()) {
				if (shape.getFaceShape(value).isEmpty()) {
					lightVal -= block;
				}
			} else if (!shape.isEmpty()) {
				lightVal -= block;
			}
			neighbor = Math.max(lightVal - 1, neighbor);
		}
		return Math.max(Math.min(neighbor - block, 15), 0);
	}
	
	protected int getStateAndOpacity(BlockPos pPos, BlockState state, Level level) {
		BlockState blockstate = level.getBlockState(pPos); // TODO: optimize?
		boolean flag = blockstate.canOcclude() && blockstate.useShapeForLightOcclusion();
		return blockstate.getLightBlock(this.chunkSource.getLevel(), this.pos);
	}
	
	@Override
	public int getLightValue(BlockPos pLevelPos) {
		// TODO: do this properly
		if (pLevelPos.getX() < 0) return 0;
		if (pLevelPos.getY() < 0) return 0;
		if (pLevelPos.getZ() < 0) return 0;
		if (pLevelPos.getX() >= level.getUPB() * 32 * 16) return 0;
		if (pLevelPos.getY() >= level.getUPB() * 32 * 16) return 0;
		if (pLevelPos.getZ() >= level.getUPB() * 32 * 16) return 0;
		return getSection(pLevelPos).get(pLevelPos.getX() & 15, pLevelPos.getY() & 15, pLevelPos.getZ() & 15);
	}
	
	public void setLight(BlockPos pLevelPos, byte value) {
		if (pLevelPos.getX() < 0) return;
		if (pLevelPos.getY() < 0) return;
		if (pLevelPos.getZ() < 0) return;
		if (pLevelPos.getX() >= level.getUPB() * 32 * 16) return;
		if (pLevelPos.getY() >= level.getUPB() * 32 * 16) return;
		if (pLevelPos.getZ() >= level.getUPB() * 32 * 16) return;
		getSection(pLevelPos).set(pLevelPos.getX() & 15, pLevelPos.getY() & 15, pLevelPos.getZ() & 15, value);
		level.markRenderDirty(pLevelPos);
		// TODO: mark render dirty
	}
	
	public LightSection getSection(BlockPos pos) {
		SectionPos sectionPos = SectionPos.of(pos);
		if (lastSection == null || !lastSection.equals(sectionPos)) {
			LightSection section = lightSectionHashMap.getOrDefault(sectionPos, null);
			if (section == null) lightSectionHashMap.put(sectionPos, section = new LightSection());
			this.section = section;
			lastSection = sectionPos;
		}
		return section;
	}
	
	@Override
	public void checkBlock(BlockPos p_75686_) {
		int oldValue = getLightValue(p_75686_);
		int newValue = chunkSource.getLevel().getLightEmission(p_75686_);
		
		if (oldValue < newValue) {
			positionsToUpdate.add(p_75686_.immutable());
			setLight(p_75686_, (byte) newValue);
		} else if (oldValue > newValue) {
			setLight(p_75686_, (byte) newValue);
			
			for (LightOffset lightOffset : kernel) {
				int d = lightOffset.dist;
				if (d > (oldValue + 1)) break; // no point in continuing past the light source's range
				
				pos.set(p_75686_.getX() + lightOffset.x, p_75686_.getY() + lightOffset.y, p_75686_.getZ() + lightOffset.z);
				
				// mark lights at the edge of the range as needing to be propagated again
				if (d == (oldValue + 1)) {
					if (getLightValue(pos) != 0)
						positionsToUpdate.add(pos.immutable());
//					setLight(pos, (byte) 0);
					continue;
				}
				
				// mark light sources as needing to be propagated again
				int light;
				setLight(pos, (byte) (light = chunkSource.getLevel().getLightEmission(pos)));
				if (light != 0) positionsToUpdate.add(pos.immutable());
			}
			
			if (newValue != 0) positionsToUpdate.add(p_75686_.immutable());
		} else {
			setLight(p_75686_, (byte) calcLightValue(p_75686_));
			positionsToUpdate.add(p_75686_.immutable());
		}
	}
	
	@Override
	public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
	}
	
	@Override
	public void enableLightSources(ChunkPos p_75676_, boolean p_75677_) {
	}
	
	@Override
	public void retainData(ChunkPos pPos, boolean pRetain) {
	}
	
	protected void queueSectionData(long pSectionPos, @Nullable DataLayer pArray, boolean p_75663_) {
	}
	
	@Nullable
	public DataLayer getDataLayerData(SectionPos p_75690_) {
		return null;
	}
	
	public boolean hasLightWork() {
		return !positionsToUpdate.isEmpty();
	}
}
