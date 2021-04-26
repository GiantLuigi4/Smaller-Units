package com.tfc.smallerunits;

import com.tfc.smallerunits.client.RenderingHandler;
import com.tfc.smallerunits.registry.Deferred;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.common.MinecraftForge;

public class ClientEventHandler {
	public static void doStuff() {
		RenderTypeLookup.setRenderLayer(Deferred.UNIT.get(), RenderType.getCutout());

//		if (ModList.get().isLoaded("optifine"))
//		if (!SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get())
		TileEntityRendererDispatcher.instance.setSpecialRendererInternal(Deferred.UNIT_TE.get(), new SmallerUnitsTESR(TileEntityRendererDispatcher.instance));
//		else
//			new SmallerUnitsTESR(TileEntityRendererDispatcher.instance);

//		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get())
		MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onRenderWorldLastNew);
	}
}
