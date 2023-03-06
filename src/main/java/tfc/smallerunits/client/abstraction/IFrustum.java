package tfc.smallerunits.client.abstraction;

import net.minecraft.world.phys.AABB;

public abstract class IFrustum {
	public abstract boolean test(AABB box);
}
