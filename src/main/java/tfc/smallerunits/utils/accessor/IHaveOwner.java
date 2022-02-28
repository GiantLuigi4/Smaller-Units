package tfc.smallerunits.utils.accessor;

import net.minecraft.tileentity.TileEntity;

public interface IHaveOwner {
	TileEntity SmallerUnits_getOwner();
	
	void SmallerUnits_setOwner(TileEntity te);
}
