package tfc.smallerunits;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.smallerunits.data.capability.SUCapabilityManager;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class SmallerUnits {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public SmallerUnits() {
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		// bus.addListener(this::setup)
		Registry.BLOCK_REGISTER.register(modBus);
		modBus.addListener(SUCapabilityManager::onRegisterCapabilities);
		forgeBus.addGenericListener(LevelChunk.class, SUCapabilityManager::onAttachCapabilities);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	}
}
