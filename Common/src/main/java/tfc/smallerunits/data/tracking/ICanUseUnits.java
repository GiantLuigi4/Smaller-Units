package tfc.smallerunits.data.tracking;

import net.minecraft.world.phys.HitResult;

public interface ICanUseUnits {
	HitResult actualResult();
	
	void setResult(HitResult result);
	
	void removeUnit();
}
