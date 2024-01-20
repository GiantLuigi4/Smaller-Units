package tfc.smallerunits.plat.util;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientInitContext {
	Consumer<IItemRenderProperties> consumer;
	
	BlockEntityWithoutLevelRenderer renderer;
	
	public ClientInitContext(Consumer<IItemRenderProperties> consumer) {
		this.consumer = consumer;
	}
	
	public void registerRenderer(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
		this.renderer = renderer.get();
	}
	
	void finish() {
		if (renderer != null) {
			consumer.accept(new IItemRenderProperties() {
				@Override
				public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
					return renderer;
				}
			});
		}
	}
}
