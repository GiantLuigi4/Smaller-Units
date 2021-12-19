package tfc.smallerunits;

//import com.tfc.smallerunits.client.TickHandler;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
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
import tfc.collisionreversion.api.CollisionReversionAPI;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.client.RenderingHandler;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.crafting.CraftingRegistry;
import tfc.smallerunits.helpers.PacketHacksHelper;
import tfc.smallerunits.networking.*;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.renderer.FlywheelProgram;
import tfc.smallerunits.utils.compat.FlywheelEvents;
import tfc.smallerunits.utils.data.SUCapabilityManager;
import tfc.smallerunits.utils.scale.pehkui.PehkuiSupport;
import tfc.smallerunits.utils.scale.threecore.SUResizeType;
import tfc.smallerunits.utils.shapes.CollisionReversionShapeGetter;
import tfc.smallerunits.utils.tracking.PlayerDataManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

//import com.tfc.smallerunits.worldgen.WorldTypeRegistry;

//import com.tfc.smallerunits.mixins.SimpleChannelAccessor;
//import com.tfc.smallerunits.networking.SUWorldDirectingPacket;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class Smallerunits {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final String networkingVersion = "2.2";
	
	// TODO: semantic version acceptance system thing
	// if other side is on newer sub version but same major, allow connection
	// if other side is on newer major, deny
	// if other side is on older major or older sub version, deny
	//
	// 2.1 can connect to 2.0, 2.0 can't connect to 2.1
	// 3.* can't connect to 2.*
	// 2.* can't connect to 3.*
	public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("smaller_units", "main"),
			() -> networkingVersion,
			(s) -> compareVersionsInverse(networkingVersion, s),
			(s) -> compareVersions(networkingVersion, s)
	);
	
	public static boolean compareVersions(String str0, String str1) {
		String[] ver0 = parseVersion(str0);
		String[] ver1 = parseVersion(str1);
		ver0 = addPlaceholders(ver0, ver1);
		ver1 = addPlaceholders(ver1, ver0);
		
		if (!ver0[0].equals(ver1[0])) return false;
		
		for (int i = 0; i < ver0.length; i++) {
			if (Integer.parseInt(ver0[i]) < Integer.parseInt(ver1[i]))
				return false;
		}
		return true;
	}
	
	public static boolean compareVersionsInverse(String str0, String str1) {
		String[] ver0 = parseVersion(str0);
		String[] ver1 = parseVersion(str1);
		ver0 = addPlaceholders(ver0, ver1);
		ver1 = addPlaceholders(ver1, ver0);
		
		if (!ver0[0].equals(ver1[0])) return false;
		
		for (int i = 0; i < ver0.length; i++) {
			if (Integer.parseInt(ver0[i]) < Integer.parseInt(ver1[i]))
				return true;
		}
		return true;
	}
	
	public static String[] addPlaceholders(String[] ver0, String[] ver1) {
		int len = Math.max(ver0.length, ver1.length);
		String[] strs = new String[len];
		for (int i = 0; i < len; i++) {
			if (i < ver0.length) {
				strs[i] = ver0[i];
			} else {
				strs[i] = "0";
			}
		}
		return strs;
	}
	
	public static String[] parseVersion(String input) {
		if (input.contains(".")) {
			return input.split("\\.");
		}
		return new String[]{input};
	}

	// wat? lol
//	public static void onConfigEvent(ModConfig.ModConfigEvent event) {
//		if (event.getConfig().getModId().equals("smallerunits")) {
//			CustomArrayList.growthRate = (Integer) Config.COMMON.listGrowthRate.get() - 1;
//			CustomArrayList.minGrowth = (Integer)Config.COMMON.minGrowth.get();
//		}
//	}
	
	private static Smallerunits INSTANCE;
	private final boolean isVivecraftPresent;
	private static boolean isCollisionReversionPresent = false;
	
	public static boolean isVivecraftPresent() {
		return INSTANCE.isVivecraftPresent;
	}
	
	public Smallerunits() {
		INSTANCE = this;
		
		// make sure the SU event bus is loaded and whatnot
		IEventBus suEventBus = SmallerUnitsAPI.EVENT_BUS;
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);
		
		if (FMLEnvironment.dist.isClient()) {
			if (ClientBrandRetriever.getClientModName().equals("vivecraft")) {
				boolean vivecraftPresence = false;
				
				try {
					Class<?> clazz = Class.forName("org.vivecraft.api.VRData");
					if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
					Method m = clazz.getMethod("getController", int.class);
					if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
						throw new ReflectiveOperationException("disable");
					
					clazz = Class.forName("org.vivecraft.api.VRData$VRDevicePose");
					if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
					m = clazz.getMethod("getPosition");
					if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
						throw new ReflectiveOperationException("disable");
					m = clazz.getMethod("getDirection");
					if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
						throw new ReflectiveOperationException("disable");
					
					clazz = Class.forName("org.vivecraft.gameplay.VRPlayer");
					if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
					m = clazz.getMethod("get");
					if (!(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
						throw new ReflectiveOperationException("disable");
					Field f = clazz.getField("vrdata_world_render");
					if (!(Modifier.isPublic(f.getModifiers()))) throw new RuntimeException("disable");
					
					LOGGER.info("Vivecraft detected; enabling support");
					vivecraftPresence = true;
				} catch (ReflectiveOperationException err) {
					err.printStackTrace();
					LOGGER.warn("Vivecraft detected; however, the version of vivecraft which is present does not match with what smaller units expects");
					
					String detectedVivecraftVersion = "null";
					try {
						Field f = Minecraft.class.getField("minecriftVerString");
						detectedVivecraftVersion = (String) f.get(Minecraft.getInstance());
					} catch (Throwable ignored) {
					}
					LOGGER.warn("Found: " + detectedVivecraftVersion + ", Expected: " + "Vivecraft 1.16.5 jrbudda-7-5 1.16.5");
					ModLoader.get().addWarning(
							new ModLoadingWarning(
									ModLoadingContext.get().getActiveContainer().getModInfo(),
									ModLoadingStage.CONSTRUCT, "smallerunits.vivecraft.support.version.error"
							)
					);
				}
				isVivecraftPresent = vivecraftPresence;
			} else {
				isVivecraftPresent = false;
			}
		} else {
			isVivecraftPresent = false; // TODO: when vivecraft gets an API, use that to test for vivecraft
		}
		
//		WorldTypeRegistry.init();
		
		isCollisionReversionPresent = ModList.get().isLoaded("collision_reversion");
		
		if (isCollisionReversionPresent) {
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
					// TODO: make all packets only set the packet handled if it's on the right side
					if (ctx.get().getDirection().getReceptionSide().isClient()) {
						packet.processPacket(null);
						ctx.get().setPacketHandled(true);
					}
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
		NETWORK_INSTANCE.registerMessage(6, STileNBTPacket.class,
				STileNBTPacket::writePacketData,
				STileNBTPacket::new,
				(packet, ctx) -> {
					ctx.get().setPacketHandled(true);
					packet.handle(ctx);
				}
		);
		NETWORK_INSTANCE.registerMessage(7, CBreakLittleBlockStatusPacket.class,
				CBreakLittleBlockStatusPacket::writePacketData,
				CBreakLittleBlockStatusPacket::new,
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
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SmallerUnitsConfig.commonSpec);
		
		if (FMLEnvironment.dist.isClient()) {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SmallerUnitsConfig.clientSpec);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onDrawSelectionBox);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onChangeDimensions);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onLeaveWorld);
			MinecraftForge.EVENT_BUS.addListener(RenderingHandler::onRenderTick);
			
			if (ModList.get().isLoaded("flywheel")) {
				bus.addListener(FlywheelProgram::onFlywheelInit);
				MinecraftForge.EVENT_BUS.addListener(FlywheelEvents::onReloadRenderers);
			}
		}
		
		if (ModList.get().isLoaded("threecore")) {
			LOGGER.info("ThreeCore detected; enabling support");
			
			SUResizeType.suSizeChangeTypes.register(bus);
		}
		
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onSneakClick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerInteract);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerTick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onWorldTick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerBreakBlock);
		MinecraftForge.EVENT_BUS.addListener(SUCapabilityManager::onChunkWatchEvent);
		MinecraftForge.EVENT_BUS.addGenericListener(Chunk.class, CommonEventHandler::onAttachCapabilities);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static boolean useSelectionReversion(IBlockReader worldIn) {
		return isCollisionReversionPresent && (
				javaWhyAreDumb$1() && (
						!(worldIn instanceof World) ||
								((World) worldIn).isRemote));
	}
	
	private static boolean javaWhyAreDumb$1() {
		return CollisionReversionAPI.useSelection();
	}
	
	private static boolean javaWhyAreDumb$2() {
		return CollisionReversionAPI.useCollision();
	}
	
	public static boolean useCollisionReversion(IBlockReader worldIn) {
		return isCollisionReversionPresent && (
				javaWhyAreDumb$2() && (
						!(worldIn instanceof World) ||
								((World) worldIn).isRemote));
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		SUCapabilityManager.register();
		if (ModList.get().isLoaded("pehkui")) PehkuiSupport.setup();
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
