package tfc.smallerunits.plat.util;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientInitContext {
	Consumer<IClientItemExtensions> consumer;
	
	BlockEntityWithoutLevelRenderer renderer;
	
	public ClientInitContext(Consumer<IClientItemExtensions> consumer) {
		this.consumer = consumer;
	}
	
	public void registerRenderer(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
		this.renderer = renderer.get();
	}
	
	void finish() {
		if (renderer != null) {
			consumer.accept(new IClientItemExtensions() {
				@Override
				public BlockEntityWithoutLevelRenderer getCustomRenderer() {
					return renderer;
				}
			});
		}
	}
}
