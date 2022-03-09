package tfc.smallerunits;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import tfc.smallerunits.data.storage.UnitPallet;

public class UnitSpace {
	private static final Logger LOGGER = LogUtils.getLogger();
	
	// TODO: migrate to chunk class
	private final BlockState[] states = new BlockState[16 * 16 * 16];
	private int unitsPerBlock;
	
	
	public static UnitSpace fromNBT(CompoundTag tag) {
		UnitSpace space = new UnitSpace();
		space.unitsPerBlock = tag.getInt("upb");
		UnitPallet pallet = UnitPallet.fromNBT(tag.getCompound("blocks"));
		pallet.acceptStates(space.states);
		return space;
	}
	
	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("upb", unitsPerBlock);
		UnitPallet pallet = new UnitPallet(this);
		tag.put("blocks", pallet.toNBT());
		return tag;
	}
	
	public BlockState[] getBlocks() {
		return states;
	}
}
