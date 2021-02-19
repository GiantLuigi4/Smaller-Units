package com.tfc.smallerunits.utils.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraftforge.fml.ModList;

public class RenderTypeHelper {
	public static RenderType getType(RenderType input) {
		if (input.equals(RenderType.getTranslucent()))
			input = (Minecraft.getInstance().gameSettings.graphicFanciness.equals(GraphicsFanciness.FABULOUS) || ModList.get().isLoaded("optifine")) ?
					RenderType.getTranslucentMovingBlock() :
					RenderType.getTranslucentNoCrumbling();
		return input;
	}
}
