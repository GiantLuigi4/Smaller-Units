package tfc.smallerunits.utils.platform.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class GenericRegister<T> extends RegistryWrapper<T> {
	//#if FABRIC
	net.minecraft.core.Registry<T> registry;
	//#else
	//$$net.minecraftforge.registries.DeferredRegister<T> registry;
	//#endif
	
	public GenericRegister(Class<T> clazz, String namespace) {
		super(namespace);
		this.namespace = namespace;
		//#if FABRIC
		if (clazz == Block.class) this.registry = (net.minecraft.core.Registry<T>) net.minecraft.core.Registry.BLOCK;
		else if (clazz == Item.class) this.registry = (net.minecraft.core.Registry<T>) net.minecraft.core.Registry.ITEM;
		//#else
		//$$if (clazz == Block.class) this.registry = (net.minecraftforge.registries.DeferredRegister<T>) net.minecraftforge.registries.DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.BLOCKS, namespace);
		//$$else if (clazz == Item.class) this.registry = (net.minecraftforge.registries.DeferredRegister<T>) net.minecraftforge.registries.DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ITEMS, namespace);
		//#endif
	}
	
	public <E extends T> Supplier<E> register(String regName, Supplier<E> obj) {
		//#if FABRIC
		E t = net.minecraft.core.Registry.register(registry, new net.minecraft.resources.ResourceLocation(namespace, regName), obj.get());
		//#else
		//$$E t = (E) registry.register(regName, obj);
		//#endif
		return () -> t;
	}
	
	@Override
	public void register() {
		//#if FABRIC==1
		// no-op
		//#else
		//$$net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(registry);
		//#endif
	}
}
