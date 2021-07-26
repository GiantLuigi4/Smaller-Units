package tfc.smallerunits.utils.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.settings.GraphicsFanciness;
import net.minecraftforge.fml.ModList;

public class RenderTypeHelper {
	public static RenderType getType(RenderType input) {
		if (
				input.equals(RenderType.getTranslucent()) ||
						input.equals(RenderType.getTripwire()) ||
						input.equals(RenderType.getTranslucentNoCrumbling()) ||
						input.equals(RenderType.getTranslucentMovingBlock())
		)
			input = (Minecraft.getInstance().gameSettings.graphicFanciness.equals(GraphicsFanciness.FABULOUS) || ModList.get().isLoaded("optifine")) ?
					RenderType.getTranslucentMovingBlock() :
					RenderType.getTranslucentNoCrumbling();
		return input;
	}
	
	public static String getTypeName(RenderType type) {
		if (type == RenderType.getCutout()) {
			return "Cutout";
		} else if (type == RenderType.getSolid()) {
			return "Solid";
		} else if (type == RenderType.getTranslucent()) {
			return "Translucent";
		} else if (type == RenderType.getTranslucentMovingBlock()) {
			return "TranslucentMoving";
		} else if (type == RenderType.getTranslucentNoCrumbling()) {
			return "TranslucentNoCrumbling";
		} else if (type == RenderType.getCutoutMipped()) {
			return "CutoutMipped";
		} else if (type == RenderType.getTripwire()) {
			return "Tripwire";
		} else {
			return type.toString();
		}
	}
	
	public static boolean isTransparent(RenderType type) {
		return
				type.equals(RenderType.getTranslucent()) ||
						type.equals(RenderType.getTranslucentMovingBlock()) ||
						type.equals(RenderType.getTranslucentNoCrumbling());
	}
}
