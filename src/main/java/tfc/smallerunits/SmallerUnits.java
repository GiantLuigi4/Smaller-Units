package tfc.smallerunits;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	}
	
	private void setup(final FMLCommonSetupEvent event) {
	}
}
