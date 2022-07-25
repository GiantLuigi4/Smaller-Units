package tfc.smallerunits;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.IItemRenderProperties;

public class SUItemRenderProperties implements IItemRenderProperties {
	SUItemRenderer renderer = new SUItemRenderer();
	
	@Override
	public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
		return renderer;
	}
}
