package tfc.smallerunits.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import tfc.smallerunits.dimension.SUModDimension;

@Mod.EventBusSubscriber(modid = "smallerunits",bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventRegistry {
	public static DimensionType DIMENSION = null;
	
	static ResourceLocation location = new ResourceLocation("smallerunits" + ":susimulator");
	
	@ObjectHolder("smallerunits" + ":susimulator")
	public static final ModDimension DIMHOLDER = null;
	
	@SubscribeEvent
	public static void onDimensionRegistryEvent(final RegistryEvent.Register<ModDimension> event) {
		System.out.println("hello from mod dimension registry");
		event.getRegistry().register(new SUModDimension().setRegistryName(location));
	}
}
