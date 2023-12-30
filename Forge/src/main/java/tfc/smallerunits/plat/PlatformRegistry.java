package tfc.smallerunits.plat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PlatformRegistry<T> {
	DeferredRegister<T> internal;
	
	public PlatformRegistry(Class<T> cls, String modid) {
		if (cls.equals(Item.class)) {
			internal = (DeferredRegister<T>) DeferredRegister.create(ForgeRegistries.ITEMS, modid);
		} else if (cls.equals(Block.class)) {
			internal = (DeferredRegister<T>) DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
		} else if (cls.equals(RecipeSerializer.class)) {
			internal = (DeferredRegister<T>) DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, modid);
		} else
			throw new RuntimeException("Unsupported registry type.");
	}
	
	public void register() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		internal.register(modBus);
	}
	
	public Supplier<T> register(String name, Supplier<T> value) {
		return internal.register(name, value);
	}
}
