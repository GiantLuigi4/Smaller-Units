package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import tfc.smallerunits.data.storage.UnitPallet;

public class UnitSpace {
	// TODO: migrate to chunk class
	private final BlockState[] states = new BlockState[16 * 16 * 16];
	public final BlockPos pos;
	public int unitsPerBlock = 16;
	
	public UnitSpace(BlockPos pos) {
		this.pos = pos;
		for (int i = 0; i < states.length; i++) states[i] = Blocks.AIR.defaultBlockState();
		states[0] = Blocks.STONE.defaultBlockState();
		states[1] = Blocks.ANVIL.defaultBlockState();
		states[16] = Blocks.DIRT.defaultBlockState();
		states[32] = Blocks.DIRT.defaultBlockState();
		states[48] = Blocks.GRASS_BLOCK.defaultBlockState();
		unitsPerBlock = 4;
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
//		if (unitsPerBlock == 16) for (int  i = 0; i < states.length; i++) states[i] = Blocks.LECTERN.defaultBlockState();
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
	
	public BlockState getBlock(int x, int y, int z) {
		int indx = (((x * 16) + y) * 16) + z;
		return states[indx];
	}
}
