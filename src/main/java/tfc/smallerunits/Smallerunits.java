package tfc.smallerunits;

//import com.tfc.smallerunits.client.TickHandler;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
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
import tfc.collisionreversion.api.CollisionReversionAPI;
import tfc.smallerunits.api.SmallerUnitsAPI;
import tfc.smallerunits.client.RenderingHandler;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.crafting.CraftingRegistry;
import tfc.smallerunits.helpers.PacketHacksHelper;
import tfc.smallerunits.networking.*;
import tfc.smallerunits.networking.screens.CUpdateLittleCommandBlockPacket;
import tfc.smallerunits.networking.screens.CUpdateLittleSignPacket;
import tfc.smallerunits.networking.screens.CUpdateLittleStructureBlockPacket;
import tfc.smallerunits.networking.screens.SOpenLittleSignPacket;
import tfc.smallerunits.networking.tracking.SSyncSUData;
import tfc.smallerunits.networking.util.SimpleChannelWrapper;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.renderer.FlywheelProgram;
import tfc.smallerunits.utils.compat.FlywheelEvents;
import tfc.smallerunits.utils.compat.vr.vivecraft.MinecriftDetector;
import tfc.smallerunits.utils.data.SUCapabilityManager;
import tfc.smallerunits.utils.scale.pehkui.PehkuiSupport;
import tfc.smallerunits.utils.scale.threecore.SUResizeType;
import tfc.smallerunits.utils.shapes.CollisionReversionShapeGetter;

import javax.swing.*;
import java.awt.*;

//import com.tfc.smallerunits.worldgen.WorldTypeRegistry;

//import com.tfc.smallerunits.mixins.SimpleChannelAccessor;
//import com.tfc.smallerunits.networking.SUWorldDirectingPacket;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class Smallerunits {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final String networkingVersion = "3.1.1";
	
	protected static String serverVersion = "";
	// main.server_sub.client_sub
	//
	// main gets bumped when a new packet is added or a packet is changed without backwards compat
	// client_sub gets bumped when a client packet is changed but has backwards compat
	// server_sub gets bumped when a server packet is changed but has backwards compat
	public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation("smaller_units", "main"),
			() -> networkingVersion,
			(s) -> compareVersionsClient(networkingVersion, s),
			(s) -> compareVersionsServer(networkingVersion, s)
	);
	private static Smallerunits INSTANCE;
	private static boolean isCollisionReversionPresent = false;
	private final boolean isVFEUsed;
	private final boolean isVivecraftPresent;
	
	public Smallerunits() {
		INSTANCE = this;
		
		// make sure the SU event bus is loaded and whatnot
		IEventBus suEventBus = SmallerUnitsAPI.EVENT_BUS;
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);
		
		{
			boolean vivecraft = false;
			boolean vfe = false;
			if (FMLEnvironment.dist.isClient()) {
				vivecraft = MinecriftDetector.testClient();
			}
			if (!FMLEnvironment.dist.isClient() || !FMLEnvironment.production) { // I need to be able to test stuff easily
				// on server, this is what it is
				vfe = vivecraft = ModList.get().isLoaded("vivecraftforgeextensions");
			}
			isVivecraftPresent = vivecraft;
			isVFEUsed = vfe;
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
		
		SimpleChannelWrapper wrapper = new SimpleChannelWrapper(NETWORK_INSTANCE);
		wrapper.registerMsg(SLittleBlockEventPacket.class,
				SLittleBlockEventPacket::new,
				(packet, ctx) -> {
					// TODO: make all packets only set the packet handled if it's on the right side
					if (ctx.get().getDirection().getReceptionSide().isClient()) {
						packet.handle(ctx);
						ctx.get().setPacketHandled(true);
					}
				}
		);
		wrapper.registerMsg(CLittleBlockInteractionPacket.class, CLittleBlockInteractionPacket::new);
		wrapper.registerMsg(SLittleEntityUpdatePacket.class, SLittleEntityUpdatePacket::new);
		wrapper.registerMsg(SLittleEntityStatusPacket.class, SLittleEntityStatusPacket::new);
		wrapper.registerMsg(SLittleTileEntityUpdatePacket.class, SLittleTileEntityUpdatePacket::new);
		wrapper.registerMsg(SLittleBlockChangePacket.class, SLittleBlockChangePacket::new);
		wrapper.registerMsg(STileNBTPacket.class, STileNBTPacket::new);
		wrapper.registerMsg(CBreakLittleBlockStatusPacket.class, CBreakLittleBlockStatusPacket::new);
		wrapper.registerMsg(CUpdateLittleCommandBlockPacket.class, CUpdateLittleCommandBlockPacket::new);
		wrapper.registerMsg(SOpenLittleSignPacket.class, SOpenLittleSignPacket::new);
		wrapper.registerMsg(CUpdateLittleSignPacket.class, CUpdateLittleSignPacket::new);
		wrapper.registerMsg(CUpdateLittleStructureBlockPacket.class, CUpdateLittleStructureBlockPacket::new);
		wrapper.registerMsg(SSyncSUData.class, SSyncSUData::new);

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
		
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CommonEventHandler::onSneakClick);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CommonEventHandler::onPlayerInteract);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerTick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onWorldTick);
		MinecraftForge.EVENT_BUS.addListener(CommonEventHandler::onPlayerBreakBlock);
		MinecraftForge.EVENT_BUS.addListener(SUCapabilityManager::onChunkWatchEvent);
		MinecraftForge.EVENT_BUS.addGenericListener(Chunk.class, CommonEventHandler::onAttachCapabilities);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	// wat? lol
//	public static void onConfigEvent(ModConfig.ModConfigEvent event) {
//		if (event.getConfig().getModId().equals("smallerunits")) {
//			CustomArrayList.growthRate = (Integer) Config.COMMON.listGrowthRate.get() - 1;
//			CustomArrayList.minGrowth = (Integer)Config.COMMON.minGrowth.get();
//		}
//	}
	
	public static String getServerVersion() {
		return serverVersion;
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
	
	public static boolean compareVersionsServer(String str0, String str1) {
		if (str1.contains("compat")) return true;
		str0 = str0.split("compat")[0];
		str1 = str0.split("compat")[0];
		String[] serverVer = parseVersion(str0);
		String[] clientVer = parseVersion(str1);
		serverVer = addPlaceholders(serverVer, clientVer);
		clientVer = addPlaceholders(clientVer, serverVer);
		Smallerunits.serverVersion = str0;
		
		if (serverVer.length == 0 || clientVer.length == 0) return false;
		if (!serverVer[0].equals(clientVer[0])) return false;
		
		if (serverVer.length < 2 || clientVer.length < 2) return false;
		// server uses newer server sub than client
		// client is allowed
		if (Integer.parseInt(clientVer[1]) >= Integer.parseInt(serverVer[1])) return true;
		if (serverVer.length > 2 && clientVer.length > 2) {
			// server uses older client version than client
			// client is allowed
			if (Integer.parseInt(clientVer[2]) <= Integer.parseInt(serverVer[2])) return false;
		} else {
			// client does not have sub but server does
			// client uses older client networking version
			// client is allowed
			if (serverVer.length > clientVer.length) return false;
		}
		return false;
	}
	
	public static boolean isVivecraftPresent() {
		return INSTANCE.isVivecraftPresent;
	}
	
	public static boolean compareVersionsClient(String str0, String str1) {
		if (str0.contains("compat")) return true;
		str0 = str0.split("compat")[0];
		str1 = str0.split("compat")[0];
		String[] clientVer = parseVersion(str0);
		String[] serverVer = parseVersion(str1);
		clientVer = addPlaceholders(clientVer, serverVer);
		serverVer = addPlaceholders(serverVer, clientVer);
		Smallerunits.serverVersion = str1;
		
		if (clientVer.length == 0 || serverVer.length == 0) return false;
		if (!clientVer[0].equals(serverVer[0])) return false;
		
		if (clientVer.length < 2 || serverVer.length < 2) return false;
		if (Integer.parseInt(serverVer[1]) <= Integer.parseInt(clientVer[1])) return true;
		if (clientVer.length > 2 && serverVer.length > 2) {
			if (Integer.parseInt(serverVer[2]) >= Integer.parseInt(clientVer[2])) return false;
		} else {
			if (clientVer.length > serverVer.length) return false;
		}
		return false;
	}
	
	public static boolean isVFEPresent() {
		return INSTANCE.isVFEUsed;
	}
	
	public static boolean useSelectionReversion(IBlockReader worldIn) {
		return isCollisionReversionPresent && (
				javaWhyAreDumb$1() && (
						!(worldIn instanceof World) ||
								((World) worldIn).isRemote));
	}
	
	// I appear to also be dumb, considering the fact that I seem to have forgotten to type "you"
	private static boolean javaWhyAreDumb$1() {
		return CollisionReversionAPI.useSelection();
	}
	
	private static boolean javaWhyAreDumb$2() {
		return CollisionReversionAPI.useCollision();
	}
	
	private static boolean javaWhyAreDumb$3() {
		return CollisionReversionAPI.useVisualShapeReversion();
	}
	
	public static boolean useCollisionReversion(IBlockReader worldIn) {
		return isCollisionReversionPresent && (
				javaWhyAreDumb$2() && (
						!(worldIn instanceof World) ||
								((World) worldIn).isRemote));
	}
	
	public static boolean useVisualShapeReversion(IBlockReader worldIn) {
		return isCollisionReversionPresent && (
				javaWhyAreDumb$3() && (
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
