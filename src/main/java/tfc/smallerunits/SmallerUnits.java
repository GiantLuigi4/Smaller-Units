package tfc.smallerunits;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.sync.SyncPacketS2C;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class SmallerUnits {
	private static final Logger LOGGER = LogManager.getLogger();
	
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
		forgeBus.addGenericListener(LevelChunk.class, SUCapabilityManager::onAttachCapabilities);
	}
}
