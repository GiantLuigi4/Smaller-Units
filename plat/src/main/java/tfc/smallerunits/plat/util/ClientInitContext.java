package tfc.smallerunits.plat.util;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;

import java.util.function.Supplier;

public class ClientInitContext {
	public void registerRenderer(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
		throw new RuntimeException();
	}
}
