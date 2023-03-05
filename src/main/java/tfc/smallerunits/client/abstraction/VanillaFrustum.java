package tfc.smallerunits.client.abstraction;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class VanillaFrustum extends IFrustum {
	Frustum frustum;
	
	@Override
	public boolean test(AABB box) {
		return frustum.isVisible(box);
	}
	
	public void set(Frustum frustum) {
		this.frustum = frustum;
	}
}
