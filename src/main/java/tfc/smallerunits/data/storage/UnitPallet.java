package tfc.smallerunits.data.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfc.smallerunits.UnitSpace;

import java.util.HashMap;

// mojang's one is weird, it seems?
public class UnitPallet {
	private static final Logger LOGGER = LoggerFactory.getLogger("SU:UnitPallet");
	HashMap<Integer, Integer> indexToIdMap;
	HashMap<BlockState, Integer> stateToIdMap;
	HashMap<Integer, BlockState> idToStateMap = new HashMap<>();
	int countFull;
	
	public UnitPallet() {
		indexToIdMap = new HashMap<>();
		stateToIdMap = new HashMap<>();
		countFull = 0;
	}
	
	public UnitPallet(UnitSpace space) {
		countFull = 0;
		BlockState[] states = space.getBlocks();
		indexToIdMap = new HashMap<>();
		stateToIdMap = new HashMap<>();
		int id = 0;
		for (int i = 0; i < states.length; i++) {
			if (states[i] == null || states[i].isAir()) continue;
			if (!stateToIdMap.containsKey(states[i])) stateToIdMap.put(states[i], id++);
			indexToIdMap.put(i, stateToIdMap.get(states[i]));
			countFull++;
		}
		populateIdToStateMap();
	}
	
	public static UnitPallet fromNBT(CompoundTag tag) {
		UnitPallet pallet = new UnitPallet();
		/* read map of state pallet index to block state string */
		CompoundTag indices = tag.getCompound("indices");
		for (String allKey : indices.getAllKeys())
			pallet.indexToIdMap.put(Integer.parseInt(allKey), indices.getInt(allKey));
		/* read map of index to state pallet index */
		CompoundTag states = tag.getCompound("states");
		for (String allKey : states.getAllKeys()) {
			JsonObject element = new JsonObject();
			element.addProperty("Name", allKey);
			BlockState state = BlockState.CODEC
					.decode(JsonOps.INSTANCE, element)
					.getOrThrow(false, LOGGER::error)
					.getFirst();
			pallet.stateToIdMap.put(state, states.getInt(allKey));
		}
		pallet.populateIdToStateMap();
		return pallet;
	}
	
	protected void populateIdToStateMap() {
		for (BlockState state : stateToIdMap.keySet())
			idToStateMap.put(stateToIdMap.get(state), state);
	}
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		/* write map of state pallet index to block state string */
		CompoundTag indices = new CompoundTag();
		for (Integer integer : indexToIdMap.keySet()) indices.putInt(integer.toString(), indexToIdMap.get(integer));
		tag.put("indices", indices);
		/* write map of index to state pallet index */
		CompoundTag states = new CompoundTag();
		for (BlockState state : stateToIdMap.keySet()) {
			int id = stateToIdMap.get(state);
			JsonElement obj = BlockState.CODEC
					.encode(state, JsonOps.INSTANCE, new JsonObject())
					.getOrThrow(false, LOGGER::error);
			String dat = obj.getAsJsonObject().get("Name").getAsJsonPrimitive().getAsString();
			states.putInt(dat, id);
		}
		tag.put("states", states);
		return tag;
	}
	
	public void acceptStates(BlockState[] states) {
		for (int i = 0; i < states.length; i++) {
			if (indexToIdMap.containsKey(i)) {
				BlockState state = idToStateMap.get(indexToIdMap.get(i));
				states[i] = state;
			} else states[i] = Blocks.AIR.defaultBlockState();
		}
	}
}
