package tfc.smallerunits;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
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
//		for (int x = 0; x < 8; x++) {
//			for (int z = 0; z < 8; z++) {
//				int y = 0;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.COBBLESTONE.defaultBlockState();
//			}
//		}
//		for (int x = 2; x < 6; x++) {
//			for (int y = 1; y < 5; y++) {
//				int z = 2;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int x = 2; x < 6; x++) {
//			for (int y = 1; y < 5; y++) {
//				int z = 5;
//				if (x >= 3 && x <= 4 && y >= 2 && y <= 3) continue;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int z = 2; z < 6; z++) {
//			for (int y = 1; y < 5; y++) {
//				int x = 2;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int z = 2; z < 6; z++) {
//			for (int y = 1; y < 5; y++) {
//				int x = 5;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.OAK_PLANKS.defaultBlockState();
//			}
//		}
//		for (int z = 3; z < 5; z++) {
//			for (int x = 3; x < 5; x++) {
//				int y = 1;
//				int indx = (((x * 16) + y) * 16) + z;
//				states[indx] = Blocks.POLISHED_ANDESITE.defaultBlockState();
//			}
//		}
//		for (int z = 1; z < 7; z++) {
//			if (z <= 5 && z >= 2) continue;
//			for (int x = 1; x < 7; x++) {
//				if (x <= 5 && x >= 2) continue;
//				for (int y = 0; y < 6; y++) {
//					int indx = (((x * 16) + y) * 16) + z;
//					states[indx] = Blocks.OAK_LOG.defaultBlockState();
//				}
//			}
//		}
		states[0] = Blocks.STONE.defaultBlockState();
		states[1] = Blocks.ANVIL.defaultBlockState();
		states[16] = Blocks.DIRT.defaultBlockState();
		states[32] = Blocks.DIRT.defaultBlockState();
		states[48] = Blocks.GRASS_BLOCK.defaultBlockState();
		unitsPerBlock = 8;
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
	
	public void setState(BlockPos relative, Block block) {
		int indx = (((relative.getX() * 16) + relative.getY()) * 16) + relative.getZ();
		states[indx] = block.defaultBlockState();
	}
}
