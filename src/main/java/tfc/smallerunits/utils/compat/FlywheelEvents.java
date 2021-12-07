package tfc.smallerunits.utils.compat;

import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import tfc.smallerunits.SmallerUnitsTESR;

public class FlywheelEvents {
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		SmallerUnitsTESR.closeRenderables((renderable) -> false);
	}
}
