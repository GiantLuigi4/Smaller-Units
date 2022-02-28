package tfc.smallerunits.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

public class NBTStripper {
	public static CompoundNBT stripOfTEData(CompoundNBT input) {
		CompoundNBT output = new CompoundNBT();
		CompoundNBT units = input.getCompound("containedUnits");
		CompoundNBT containedUnits = new CompoundNBT();
		containedUnits.put("blocks", units.getCompound("blocks"));
		containedUnits.put("states", units.getCompound("states"));
		ListNBT units1 = units.getList("units", Constants.NBT.TAG_COMPOUND);
		ListNBT unitsCopy = new ListNBT();
		for (INBT inbt : units1) {
			CompoundNBT nbt1 = ((CompoundNBT) inbt).copy();
			nbt1.remove("tileNBT");
			unitsCopy.add(nbt1);
		}
		
		containedUnits.put("units", unitsCopy);
		
		for (String key : input.keySet()) {
			if (!key.equals("containedUnits")) {
				INBT nbt = input.get(key);
				if (nbt != null) output.put(key, nbt.copy());
			}
		}
		
		output.put("containedUnits", containedUnits);
		return output;
	}
}
