package com.tfc.smallerunits;

import com.tfc.smallerunits.client.TickHandler;
import com.tfc.smallerunits.registry.Deferred;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class Smallerunits {
	
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	
	public Smallerunits() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);
		
		Deferred.BLOCKS.register(bus);
		Deferred.TILE_ENTITIES.register(bus);
		Deferred.ITEMS.register(bus);
		
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.addListener(TickHandler::onTick);
		}
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	}
	
	private void doClientStuff(final FMLClientSetupEvent event) {
		ClientEventHandler.doStuff();
//		LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
	}
}
