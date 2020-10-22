package tfc.smallerunits.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

public class SmallUnit {
	public int x;
	public int y;
	public int z;
	public int unitsPerBlock;
	public BlockState heldState;
	public TileEntity tileEntity;
	
	public SmallUnit(int x, int y, int z, int unitsPerBlock, BlockState heldState) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.unitsPerBlock = unitsPerBlock;
		this.heldState = heldState;
	}
	
	public void createTileEntity(FakeWorld fakeWorld) {
		if (heldState instanceof ITileEntityProvider) {
			((ITileEntityProvider) heldState.getBlock()).createNewTileEntity(fakeWorld);
		} else {
			heldState.getBlock().createTileEntity(heldState, fakeWorld);
		}
	}
	
	public static SmallUnit fromString(String s, int upb) {
		if (s.equals("")) {
			return null;
		}
		int num = 0;
		int x = 0;
		int y = 0;
		int z = 0;
		BlockState sta = Blocks.AIR.getDefaultState();
		for (String s1 : s.split(",")) {
			try {
				if (num == 0) {
					x = Integer.parseInt(s1.replace(",", ""));
				} else if (num == 1) {
					y = Integer.parseInt(s1.replace(",", ""));
				} else if (num == 2) {
					z = Integer.parseInt(s1.replace(",", ""));
				} else if (num == 3) {
					String blockS = s1.substring("Block{".length(), s1.indexOf("}"));
					Block block = Registry.BLOCK.getOrDefault(new ResourceLocation(blockS));
					try {
						String state = s1.substring(("Block{" + blockS + "}").length());
						for (Object obj : block.getStateContainer().getValidStates().toArray()) {
							if (obj.toString().equals((block) + ("" + (state.replace('|', ','))))) {
								sta = ((BlockState) obj);
							}
						}
						if (sta.equals(Blocks.AIR.getDefaultState())) {
							sta = block.getDefaultState();
						}
					} catch (Throwable err) {
						sta = block.getDefaultState();
					}
				}
			} catch (Throwable err) {
			}
			num++;
		}
		return new SmallUnit(x, y, z, upb, sta);
	}
	
	public TileEntity readTileEntity(CompoundNBT nbt) {
		TileEntity te = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(nbt.getString("id"))).create();
		te.read(nbt);
		this.tileEntity = te;
		return te;
	}
	
	@Override
	public String toString() {
		try {
//			try {
//				return ""+(x+','+y+','+z+','+s.toString().replace(',','|')+','+"\""+te.serializeNBT().toString()+"\"");
//			} catch (Exception err) {
			return "" + (x + "," + y + "," + z + "," + heldState.toString().replace(',', '|') + "," + "\"{}\"");
//			}
		} catch (Throwable err) {
			return "null";
		}
	}
}
