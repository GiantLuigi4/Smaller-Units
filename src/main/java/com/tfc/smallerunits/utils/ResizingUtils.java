package com.tfc.smallerunits.utils;

import com.tfc.smallerunits.utils.threecore.SUResizeType;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.threetag.threecore.capability.CapabilitySizeChanging;

public class ResizingUtils {
	public static void resize(Entity entity, int amt) {
		//TODO: chiseled me integration
		if (ModList.get().isLoaded("threecore")) {
			entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent((cap) -> {
				if (amt > 0) {
					if (1f / cap.getScale() <= 4) {
						cap.startSizeChange(SUResizeType.SU_CHANGE_TYPE.get(), Math.max(cap.getScale() - (amt / 8f), 1f / 8));
					}
				} else {
					if (cap.getScale() <= 2) {
						cap.startSizeChange(SUResizeType.SU_CHANGE_TYPE.get(), Math.min(cap.getScale() - (amt / 2f), 2));
					}
				}
			});
		}
	}
	
	public static void resizeForUnit(Entity entity, float amt) {
		//TODO: chiseled me integration
		if (ModList.get().isLoaded("threecore")) {
			entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent((cap) -> {
				cap.setSizeDirectly(SUResizeType.SU_CHANGE_TYPE.get(), amt);
			});
		}
	}
}
