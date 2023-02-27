package tfc.smallerunits;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.impl.client.rendering.BuiltinItemRendererRegistryImpl;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;

public class SUItemRenderProperties {
	private static final SUItemRenderer renderer = new SUItemRenderer();
	
	public static void init() {
		// amazingly pointless wrapper class
		BuiltinItemRendererRegistryImpl.INSTANCE.register(Registry.UNIT_SPACE_ITEM.get(), (stack, mode, matrices, vertexConsumers, light, overlay) -> renderer.renderByItem(stack, mode, matrices, vertexConsumers, light, overlay));
	}
}
