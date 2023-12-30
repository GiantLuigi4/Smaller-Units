package tfc.smallerunits.plat;

import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class PlatformRegistry<T> {
	net.minecraft.core.Registry<T> registry;
	String modid;
	
	public PlatformRegistry(Class<T> cls, String modid) {
		this.modid = modid;
		if (cls == Block.class) this.registry = (net.minecraft.core.Registry<T>) net.minecraft.core.Registry.BLOCK;
		else if (cls == Item.class) this.registry = (net.minecraft.core.Registry<T>) net.minecraft.core.Registry.ITEM;
		else if (cls == RecipeSerializer.class) this.registry = (net.minecraft.core.Registry<T>) Registry.RECIPE_SERIALIZER;
	}
	
	public void register() {
	}
	
	public Supplier<T> register(String name, Supplier<T> value) {
		T tem = net.minecraft.core.Registry.register(registry, new net.minecraft.resources.ResourceLocation(modid, name), value.get());
		return () -> tem;
	}
}
