package tfc.smallerunits.utils.platform.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import tfc.smallerunits.utils.asm.MappingInfo;
import tfc.smallerunits.utils.asm.Remapper;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class RecipeRegister<T extends RecipeSerializer<?>> {
	String namespace;
	Remapper remapper = new Remapper(FabricLoader.getInstance().getMappingResolver());
	
	public RecipeRegister(String namespace) {
		this.namespace = namespace;
	}
	
	public <I extends T> Supplier<I> register(String regName, Supplier<T> obj) {
		try {
			String method = remapper.mapMethod(new MappingInfo(
					"net/minecraft/class_1865",
					"method_17724",
					"(Ljava/lang/String;Lnet/minecraft/class_1865;)Lnet/minecraft/class_1865;"
			));
			method = method.split("\\(")[0];
			Method m = RecipeSerializer.class.getMethod(method, String.class, RecipeSerializer.class);
			m.setAccessible(true);
			//noinspection unchecked
			I t = (I) Registry.register(Registry.RECIPE_SERIALIZER, namespace + ":" + regName, obj.get());
			return () -> t;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public void register() {
		// no-op
	}
}