package com.tfc.smallerunits;

//import com.tfc.smallerunits.client.TickHandler;

import com.tfc.smallerunits.client.RenderingHandler;
import com.tfc.smallerunits.crafting.CraftingRegistry;
import com.tfc.smallerunits.helpers.PacketHacksHelper;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.threecore.SUResizeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import com.tfc.smallerunits.mixins.SimpleChannelAccessor;
//import com.tfc.smallerunits.networking.SUWorldDirectingPacket;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class Smallerunits {
	
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

//	public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
//			new ResourceLocation("smaller_units", "main"),
//			() -> "1",
//			"1"::equals,
//			"1"::equals
//	);
	
	public Smallerunits() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);

//		NETWORK_INSTANCE.registerMessage(
//				0,
//				SUWorldDirectingPacket.class,
//				SUWorldDirectingPacket::writePacketData,
//				SUWorldDirectingPacket::new,
//				(packet,context)->{
//					ICustomPacket<?> packet1 = context.get().getDirection().buildPacket(
//							Pair.of(new PacketBuffer(packet.buffer.copy()),Integer.MIN_VALUE),packet.network_instance
//					);
//					try {
//						//TODO:
//						packet1.getThis().readPacketData(packet.buffer);
//					} catch (Throwable ignored) {
//					}
//				}
//		);
		
		Deferred.BLOCKS.register(bus);
		Deferred.TILE_ENTITIES.register(bus);
		Deferred.ITEMS.register(bus);
		CraftingRegistry.recipeSerializers.register(bus);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SmallerUnitsConfig.serverSpec);
		
		if (FMLEnvironment.dist.isClient()) {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SmallerUnitsConfig.clientSpec);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onDrawSelectionBox);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onChangeDimensions);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onLeaveWorld);
		}
		
		if (ModList.get().isLoaded("threecore")) {
			SUResizeType.suSizeChangeTypes.register(bus);
		}
		
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onSneakClick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerInteract);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	}
	
	public static void onNetworkEvent(NetworkEvent.ServerCustomPayloadEvent event) {
		event.getPayload().writeBoolean(PacketHacksHelper.unitPos != null);
		if (PacketHacksHelper.unitPos != null) {
			event.getPayload().writeBlockPos(PacketHacksHelper.unitPos);
		}
	}

//	public static void onNetworkEvent(NetworkEvent.ClientCustomPayloadEvent event) {
//		event.getSource().get().getDirection().
//		event.getPayload().writeBoolean(PacketHacksHelper.unitPos != null);
//		if (PacketHacksHelper.unitPos != null) {
//			event.getPayload().writeBlockPos(PacketHacksHelper.unitPos);
//		}
//	}
	
	private void doClientStuff(final FMLClientSetupEvent event) {
		ClientEventHandler.doStuff();
//		LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
	}
}
