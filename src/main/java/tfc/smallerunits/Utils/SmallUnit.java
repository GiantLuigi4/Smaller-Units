package tfc.smallerunits.Utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class SmallUnit {
	public int x;
	public int y;
	public int z;
	public int upb; //units per block
	public BlockState s; //state
	public TileEntity te; //TileEntity
	
	public SmallUnit(int x, int y, int z, int upb, BlockState s) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.upb = upb;
		this.s = s;
	}
	
	public void createTileEntity(FakeWorld fakeWorld) {
		if (s instanceof ITileEntityProvider) {
			((ITileEntityProvider)s.getBlock()).createNewTileEntity(fakeWorld);
		}
		s.getBlock().createTileEntity(s,fakeWorld);
	}
	
	public static SmallUnit fromString(String s,int upb) {
		int num=0;
		int x=0;
		int y=0;
		int z=0;
		BlockState sta=Blocks.PISTON.getDefaultState();
		for (String s1:s.split(",")) {
			try {
				if (num==0) {
					x=Integer.parseInt(s1.replace(",",""));
				} else if (num==1) {
					y=Integer.parseInt(s1.replace(",",""));
				} else if (num==2) {
					z=Integer.parseInt(s1.replace(",",""));
				} else if (num == 3) {
					String blockS=s1.substring("Block{".length(),s1.indexOf("}"));
					Block block = Registry.BLOCK.getOrDefault(new ResourceLocation(blockS));
					try {
						String state=s1.substring(("Block{"+blockS+"}").length());
						for (Object obj:block.getStateContainer().getValidStates().toArray()) {
							if (obj.toString().equals((block)+(""+(state.replace('|',','))))) {
								sta=((BlockState)obj);
							}
						}
						if (sta.equals(Blocks.PISTON.getDefaultState())) {
							sta=block.getDefaultState();
						}
					} catch (Exception err) {
						sta=block.getDefaultState();
					}
				} else if (num>=4) {
				}
			} catch (Exception err) {}
			num++;
		}
		return new SmallUnit(x,y,z,upb,sta);
	}
	
	@Override
	public String toString() {
		try {
			try {
				return ""+(x+','+y+','+z+','+s.toString().replace(',','|')+','+te.serializeNBT());
			} catch (Exception err) {
				return ""+(x+","+y+","+z+","+s.toString().replace(',','|')+","+"\"\"");
			}
		} catch (Exception err) {
			return "null";
		}
	}
}
