package tfc.smallerunits;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import tfc.smallerunits.registry.Deferred;

public class ClientEventHandler {
	public static void doStuff() {
		RenderTypeLookup.setRenderLayer(Deferred.UNIT.get(), RenderType.getCutout());
		
		TileEntityRendererDispatcher.instance.setSpecialRendererInternal(Deferred.UNIT_TE.get(), new SmallerUnitsTESR(TileEntityRendererDispatcher.instance));
	}
}
