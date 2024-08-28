package tfc.smallerunits.client.abstraction;

import me.jellysquid.mods.sodium.client.render.viewport.frustum.Frustum;
import net.minecraft.world.phys.AABB;

public class SodiumFrustum extends IFrustum {
	Frustum frustum;
	
	@Override
	public boolean test(AABB box) {
//		return frustum.testAab(
//				(float) box.minX, (float) box.minY, (float) box.minZ,
//				(float) box.maxX, (float) box.maxY, (float) box.maxZ
//		);
		return true; // TODO
	}
	
	public void set(Frustum frustum) {
		this.frustum = frustum;
	}
}
