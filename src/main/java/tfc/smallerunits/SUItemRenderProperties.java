package tfc.smallerunits;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class SUItemRenderProperties
		//#if FORGE
		implements IClientItemExtensions
		//#endif
{
	private static final SUItemRenderer renderer = new SUItemRenderer();
	
	//#if FABRIC
	//public static void init() {
	//	// amazingly pointless wrapper class
	//	net.fabricmc.fabric.impl.client.rendering.BuiltinItemRendererRegistryImpl.INSTANCE.register(Registry.UNIT_SPACE_ITEM.get(), (stack, mode, matrices, vertexConsumers, light, overlay) -> renderer.renderByItem(stack, mode, matrices, vertexConsumers, light, overlay));
	//}
	//#else
	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return renderer;
	}
	//#endif
}
