package tfc.smallerunits.Registry;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import tfc.smallerunits.Smallerunits;

@Mod.EventBusSubscriber(modid = "smallerunits", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusSubscriber {
	
	static ResourceLocation location=new ResourceLocation("smallerunits","susimulator");
	
	@SubscribeEvent
	public static void onRegisterDimensionsEvent(final RegisterDimensionsEvent event) {
		Smallerunits.LOGGER.log(Level.INFO,"hello from forge dimension registry");
		if (DimensionType.byName(location) == null) {
			System.out.println("register dimension");
			ModEventRegistry.DIMENSION = DimensionManager.registerDimension(location, ModEventRegistry.DIMHOLDER, new PacketBuffer(Unpooled.buffer()), true);
			System.out.println(ModEventRegistry.DIMENSION);
		}
	}
}

