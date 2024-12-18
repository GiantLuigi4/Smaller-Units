package tfc.smallerunits.plat;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PlatformRegistry<T> {
    String modid;
    BiFunction<String, T, T> registrarFunction;

    public PlatformRegistry(Class<T> cls, String modid) {
        this.modid = modid;
        if (cls == Block.class) this.registrarFunction = (name, obj) -> (T) Blocks.register(name, (Block) obj);
        else if (cls == Item.class) this.registrarFunction = (name, obj) -> (T) Items.registerItem(name, (Item) obj);
        else if (cls == RecipeSerializer.class) {
            this.registrarFunction = (name, obj) -> (T) Registry.register(
                    (Registry) BuiltInRegistries.RECIPE_SERIALIZER,
                    name,
                    (Object) obj
            );
        } else if (cls == CreativeModeTab.class) {
            this.registrarFunction = (name, obj) -> (T) Registry.register(
                    (Registry) BuiltInRegistries.CREATIVE_MODE_TAB,
                    name,
                    (Object) obj
            );
        } else {
            throw new RuntimeException("Not Implemented: " + cls.getName());
        }
    }

    public void register() {
    }

    public Supplier<T> register(String name, Supplier<T> value) {
        T tem = registrarFunction.apply(modid + ":" + name, value.get());
        return () -> tem;
    }
}
