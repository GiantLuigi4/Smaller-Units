package tfc.smallerunits;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class SUItemRenderProperties implements IClientItemExtensions {
	private static final SUItemRenderer renderer = new SUItemRenderer();
	
	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return renderer;
	}
}
