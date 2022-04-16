package tfc.smallerunits.client.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/* I should not need this */
public class SUModelDataManager {
	private static final Map<ChunkPos, Set<BlockPos>> needModelDataRefresh = new ConcurrentHashMap<>();
	private static final Map<ChunkPos, Map<BlockPos, IModelData>> modelDataCache = new ConcurrentHashMap<>();
	
	public SUModelDataManager() {
	}
	
	public void requestModelDataRefresh(BlockEntity te) {
		// TODO: make sure the TE is in this level's model data manager
		needModelDataRefresh.computeIfAbsent(new ChunkPos(te.getBlockPos()), $ -> Collections.synchronizedSet(new HashSet<>()))
				.add(te.getBlockPos());
	}
	
	public void refreshModelData(Level level, ChunkPos chunk) {
		Set<BlockPos> needUpdate = needModelDataRefresh.remove(chunk);
		
		if (needUpdate != null) {
			Map<BlockPos, IModelData> data = modelDataCache.computeIfAbsent(chunk, $ -> new ConcurrentHashMap<>());
			
			for (BlockPos pos : needUpdate) {
				BlockEntity toUpdate = level.getBlockEntity(pos);
				
				if (toUpdate != null && !toUpdate.isRemoved()) {
					data.put(pos, toUpdate.getModelData());
				} else {
					data.remove(pos);
				}
			}
		}
	}
	
	public void onChunkUnload(ChunkEvent.Unload event) {
		if (!event.getChunk().getWorldForge().isClientSide()) return;
		ChunkPos chunk = event.getChunk().getPos();
		needModelDataRefresh.remove(chunk);
		modelDataCache.remove(chunk);
	}
	
	public IModelData getModelData(Level level, BlockPos pos) {
		return getModelData(level, new ChunkPos(pos)).get(pos);
	}
	
	public Map<BlockPos, IModelData> getModelData(Level level, ChunkPos pos) {
		refreshModelData(level, pos);
		return modelDataCache.getOrDefault(pos, Collections.emptyMap());
	}
}
