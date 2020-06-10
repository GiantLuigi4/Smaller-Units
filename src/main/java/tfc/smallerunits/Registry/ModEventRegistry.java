package tfc.smallerunits.Registry;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import tfc.smallerunits.Dimension.SUModDimension;

@Mod.EventBusSubscriber(modid = "smallerunits",bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventRegistry {
	public static DimensionType DIMENSION = null;

	static ResourceLocation location=new ResourceLocation("smallerunits"+":susimulator");

	@ObjectHolder("smallerunits"+":susimulator")
	public static final ModDimension DIMHOLDER=null;

	@SubscribeEvent
	public static void onDimensionRegistryEvent(final RegistryEvent.Register<ModDimension> event) {
		System.out.println("hello from mod dimension registry");
		event.getRegistry().register(new SUModDimension().setRegistryName(location));
	}
}
