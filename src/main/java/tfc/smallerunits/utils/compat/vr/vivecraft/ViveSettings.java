package tfc.smallerunits.utils.compat.vr.vivecraft;

import net.minecraft.client.Minecraft;
import org.vivecraft.settings.VRSettings;

public class ViveSettings {
	private static VRSettings settings;
	
	public static void init() throws NoSuchFieldException, IllegalAccessException {
		Minecraft mc = Minecraft.getInstance();
		//noinspection ConstantConditions
		if (mc.getClass().getField("vrSettings") != null)
			settings = (VRSettings) mc.getClass().getField("vrSettings").get(mc);
	}
	
	public static boolean isReverseHands() {
		return settings.vrReverseHands;
	}
}
