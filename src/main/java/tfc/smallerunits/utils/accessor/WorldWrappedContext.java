package tfc.smallerunits.utils.accessor;

import net.minecraft.world.World;

public interface WorldWrappedContext {
	World SmallerUnits_getParentWorld();
	
	void SmallerUnits_setParentWorld(World wld);
}
