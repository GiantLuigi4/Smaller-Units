package tfc.smallerunits;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import tfc.smallerunits.data.storage.UnitPallet;

public class UnitSpace {
	private static final Logger LOGGER = LogUtils.getLogger();
	
	// TODO: migrate to chunk class
	private final BlockState[] states = new BlockState[16 * 16 * 16];
	public final BlockPos pos;
	private int unitsPerBlock;
	
	public UnitSpace(BlockPos pos) {
		this.pos = pos;
	}
	
	public static UnitSpace fromNBT(CompoundTag tag) {
		UnitSpace space = new UnitSpace(
				new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))
		);
		space.unitsPerBlock = tag.getInt("upb");
		UnitPallet pallet = UnitPallet.fromNBT(tag.getCompound("blocks"));
		pallet.acceptStates(space.states);
		return space;
	}
	
	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("x", pos.getX());
		tag.putInt("y", pos.getY());
		tag.putInt("z", pos.getZ());
		tag.putInt("upb", unitsPerBlock);
		states[0] = Blocks.STONE.defaultBlockState();
		states[16] = Blocks.DIRT.defaultBlockState();
		states[32] = Blocks.DIRT.defaultBlockState();
		states[48] = Blocks.GRASS_BLOCK.defaultBlockState();
		UnitPallet pallet = new UnitPallet(this);
		tag.put("blocks", pallet.toNBT());
		return tag;
	}
	
	public BlockState[] getBlocks() {
		return states;
	}
	
	public UnitPallet getPallet() {
		return new UnitPallet(this);
	}
	
	public void loadPallet(UnitPallet pallet) {
		pallet.acceptStates(states);
	}
}
