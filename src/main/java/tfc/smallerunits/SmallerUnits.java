package tfc.smallerunits;

import io.netty.channel.ChannelPipeline;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.PacketReader;
import tfc.smallerunits.networking.hackery.PacketWriter;
import tfc.smallerunits.networking.sync.SyncPacketS2C;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class SmallerUnits {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static boolean isOFPresent;
	
	public SmallerUnits() {
		SUNetworkRegistry.init();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		/* registries */
		Registry.BLOCK_REGISTER.register(modBus);
		/* mod loading events */
		modBus.addListener(SUCapabilityManager::onRegisterCapabilities);
		/* in game events */
		forgeBus.addListener(SyncPacketS2C::tick);
		forgeBus.addListener(SUCapabilityManager::onChunkWatchEvent);
//		forgeBus.addListener(this::connect0);
		forgeBus.addListener(this::connect1);
		forgeBus.addGenericListener(LevelChunk.class, SUCapabilityManager::onAttachCapabilities);
		
		try {
			Class<?> clazz = Class.forName("net.optifine.Config");
			if (clazz != null) {
				ModLoader.get().addWarning(new ModLoadingWarning(
						ModLoadingContext.get().getActiveContainer().getModInfo(),
						ModLoadingStage.CONSTRUCT,
						// TODO: translation text component
						ChatFormatting.YELLOW + "Smaller Units" + ChatFormatting.RESET + "\nSU and Optifine are " + ChatFormatting.RED + ChatFormatting.BOLD + "highly incompatible" + ChatFormatting.RESET + " with eachother."
				));
				isOFPresent = true;
			}
		} catch (Throwable ignored) {
		}
	}
	
	public static void setupConnectionButchery(ChannelPipeline pipeline) {
		pipeline.addLast(SmallerUnits.class.getName() + ":writer", new PacketWriter());
		pipeline.addFirst(SmallerUnits.class.getName() + ":reader", new PacketReader());
	}

//	public void connect0(NetworkEvent.ServerCustomPayloadLoginEvent event) {
//		ChannelPipeline pipeline = event.getSource().get().getSender().connection.connection.channel().pipeline();
//		addListeners(pipeline);
//	}
	
	public static boolean isIsOFPresent() {
		return isOFPresent;
	}
	
	public void connect1(PlayerEvent.PlayerLoggedInEvent event) {
		ChannelPipeline pipeline;
		if (event.getPlayer() instanceof ServerPlayer) {
			pipeline = ((ServerPlayer) event.getPlayer()).connection.connection.channel().pipeline();
		} else {
//			if (event.getPlayer() == Minecraft.getInstance().player) {
//				pipeline = ((LocalPlayer)event.getPlayer()).connection.getConnection().channel().pipeline();
			return;
//			} else {
//				return;
//			}
		}
		setupConnectionButchery(pipeline);
	}
}
