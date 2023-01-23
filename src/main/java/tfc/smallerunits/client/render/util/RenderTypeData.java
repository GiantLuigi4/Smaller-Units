package tfc.smallerunits.client.render.util;

import net.minecraft.client.renderer.RenderType;

public class RenderTypeData {
	public static boolean isSortable(RenderType type) {
		//noinspection RedundantIfStatement
		if (type.equals(RenderType.translucent())) return true;
		// may need mod compat stuff here?
		return false;
	}
}
