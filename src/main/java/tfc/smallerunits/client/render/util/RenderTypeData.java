package tfc.smallerunits.client.render.util;

import net.minecraft.client.renderer.RenderType;

public class RenderTypeData {
	public static boolean isSortable(RenderType type) {
		if (type.equals(RenderType.translucent())) return true;
		else //noinspection RedundantIfStatement
			if (type.equals(RenderType.translucentMovingBlock())) return true;
		// may need mod compat stuff here?
		return false;
	}
}
