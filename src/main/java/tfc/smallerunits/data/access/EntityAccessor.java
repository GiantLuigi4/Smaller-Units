package tfc.smallerunits.data.access;

import net.minecraft.world.level.portal.PortalInfo;

public interface EntityAccessor {
	void setPosRawNoUpdate(double pX, double pY, double pZ);
	void setMotionScalar(float scl);
	void setPortalInfo(PortalInfo trg);
}
