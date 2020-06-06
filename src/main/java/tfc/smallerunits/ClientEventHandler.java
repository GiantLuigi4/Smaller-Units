package tfc.smallerunits;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import tfc.smallerunits.Registry.Deferred;

public class ClientEventHandler {
	public static void doStuff() {
		ClientRegistry.bindTileEntityRenderer(Deferred.TILE_ENTITY.get(), SmallerUnitTESR::new);
		RenderTypeLookup.setRenderLayer(Deferred.UNIT.get(), RenderType.getTranslucent());
	}
}
