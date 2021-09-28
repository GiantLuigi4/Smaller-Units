package tfc.smallerunits;

//import com.tfc.smallerunits.client.TickHandler;

import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.client.RenderingHandler;
import tfc.smallerunits.crafting.CraftingRegistry;
import tfc.smallerunits.helpers.PacketHacksHelper;
import tfc.smallerunits.networking.*;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.renderer.FlywheelProgram;
import tfc.smallerunits.utils.shapes.CollisionReversionShapeGetter;
import tfc.smallerunits.utils.threecore.SUResizeType;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleModifier;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

//import com.tfc.smallerunits.worldgen.WorldTypeRegistry;

//import com.tfc.smallerunits.mixins.SimpleChannelAccessor;
//import com.tfc.smallerunits.networking.SUWorldDirectingPacket;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class Smallerunits {
	
	public static final AtomicReference<ScaleModifier> SUScaleModifier = new AtomicReference<>();
	public static final AtomicReference<ScaleType> SUScaleType = new AtomicReference<>();
	
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("smaller_units", "main"),
			() -> "1",
			"1"::equals,
			"1"::equals
	);
	
	private static Smallerunits INSTANCE;
	private final boolean isVivecraftPresent;
	
	public Smallerunits() {
		INSTANCE = this;
		
		IEventBus suEventBus = SmallerUnitsAPI.EVENT_BUS;
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);
		
		{
			boolean vivecraftPresence = false;
			try {
				Class<?> clazz = Class.forName("org.vivecraft.api.VRData");
				if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
				Method m = clazz.getMethod("getController", int.class);
				if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new RuntimeException("disable");
				
				clazz = Class.forName("org.vivecraft.api.VRData$VRDevicePose");
				if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
				m = clazz.getMethod("getPosition");
				if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new RuntimeException("disable");
				m = clazz.getMethod("getDirection");
				if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new RuntimeException("disable");
				
				clazz = Class.forName("org.vivecraft.gameplay.VRPlayer");
				if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
				m = clazz.getMethod("get");
				if (!(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new RuntimeException("disable");
				Field f = clazz.getField("vrdata_world_render");
				if (!(Modifier.isPublic(f.getModifiers()))) throw new RuntimeException("disable");
				
				LOGGER.info("Vivecraft detected; enabling support");
				vivecraftPresence = true;
			} catch (Throwable ignored) {
				ignored.printStackTrace();
				LOGGER.info("Vivecraft not detected; carrying on as if it is not present");
			}
			isVivecraftPresent = vivecraftPresence;
		}
		
		if (ModList.get().isLoaded("pehkui")) {
			LOGGER.info("Pehkui detected; enabling support");
			
			ScaleModifier modifier = new ScaleModifier() {
				@Override
				public float modifyScale(ScaleData scaleData, float modifiedScale, float delta) {
					return SUScaleType.get().getScaleData(scaleData.getEntity()).getScale(delta) * modifiedScale;
				}
			};
			ScaleRegistries.SCALE_MODIFIERS.put(new ResourceLocation("smallerunits:su_resize"), modifier);
			SUScaleModifier.set(modifier);
			ScaleType type = ScaleType.Builder.create()
					.affectsDimensions()
					.addDependentModifier(SUScaleModifier.get())
					.build();
			ScaleRegistries.SCALE_TYPES.put(new ResourceLocation("smallerunits:su_resize"), type);
			ScaleType.BASE.getDefaultBaseValueModifiers().add(modifier);
			SUScaleType.set(type);
		}

//		WorldTypeRegistry.init();
		
		if (ModList.get().isLoaded("collision_reversion")) {
			// if I left the code of that method in the main class, the game would just crash
			LOGGER.info("Collision Reversion detected; enabling support");
			CollisionReversionShapeGetter.register();
		}
		
		if (!FMLEnvironment.production && false) {
			System.setProperty("java.awt.headless", "false");
			JFrame frame = new JFrame();
			frame.setSize(1000, 1000);
			frame.setTitle("memory: " + Runtime.getRuntime().freeMemory() + "/" + Runtime.getRuntime().maxMemory());
			Canvas canvas = new Canvas() {
				@Override
				public void paint(Graphics g) {
					super.paint(g);
//					IntegratedServer server = Minecraft.getInstance().getIntegratedServer();
//					if (server != null) {
//						ServerWorld world = server.getWorld(World.OVERWORLD);
//						for (TileEntity tileEntity : world.addedTileEntityList) {
//							if (tileEntity instanceof UnitTileEntity) {
//								if (((UnitTileEntity) tileEntity).worldServer != null) {
//								}
//							}
//						}
//					}
				}
			};
			frame.add(canvas);
			Thread td = new Thread(() -> {
				try {
					while (frame.isVisible()) {
						frame.setTitle("memory: " + (Runtime.getRuntime().freeMemory() / 10241024) + "MB/" + (Runtime.getRuntime().maxMemory() / 10241024) + "MB");
						Thread.sleep(1000);
					}
				} catch (Throwable err) {
					err.printStackTrace();
				}
			});
			frame.setVisible(true);
			td.start();
			System.setProperty("java.awt.headless", "true");
		}
		
		NETWORK_INSTANCE.registerMessage(0, SLittleBlockEventPacket.class,
				SLittleBlockEventPacket::writePacketData,
				SLittleBlockEventPacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.processPacket(null);
				}
		);
		NETWORK_INSTANCE.registerMessage(1, CLittleBlockInteractionPacket.class,
				CLittleBlockInteractionPacket::writePacketData,
				CLittleBlockInteractionPacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
		NETWORK_INSTANCE.registerMessage(2, SLittleEntityUpdatePacket.class,
				SLittleEntityUpdatePacket::writePacketData,
				SLittleEntityUpdatePacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
		NETWORK_INSTANCE.registerMessage(3, SLittleEntityStatusPacket.class,
				SLittleEntityStatusPacket::writePacketData,
				SLittleEntityStatusPacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
		NETWORK_INSTANCE.registerMessage(4, SLittleTileEntityUpdatePacket.class,
				SLittleTileEntityUpdatePacket::writePacketData,
				SLittleTileEntityUpdatePacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
		NETWORK_INSTANCE.registerMessage(5, SLittleBlockChangePacket.class,
				SLittleBlockChangePacket::writePacketData,
				SLittleBlockChangePacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);

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
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onRenderTick);
			
			if (ModList.get().isLoaded("flywheel")) {
				bus.addListener(FlywheelProgram::onFlywheelInit);
			}
		}
		
		if (ModList.get().isLoaded("threecore")) {
			LOGGER.info("ThreeCore detected; enabling support");
			
			SUResizeType.suSizeChangeTypes.register(bus);
		}
		
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onSneakClick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerInteract);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerTick);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static boolean isVivecraftPresent() {
		return INSTANCE.isVivecraftPresent;
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
