package tfc.smallerunits.utils.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class GenericRegister<T> extends RegistryWrapper<T> {
	Registry<T> registry;
	
	public GenericRegister(Class<T> clazz, String namespace) {
		super(namespace);
		this.namespace = namespace;
		if (clazz == Block.class) this.registry = (Registry<T>) Registry.BLOCK;
		else if (clazz == Item.class) this.registry = (Registry<T>) Registry.ITEM;
	}
	
	public <E extends T> Supplier<E> register(String regName, Supplier<E> obj) {
		E t = Registry.register(registry, new ResourceLocation(namespace, regName), obj.get());
		return () -> t;
	}
	
	@Override
	public void register() {
		// no-op
	}
}
