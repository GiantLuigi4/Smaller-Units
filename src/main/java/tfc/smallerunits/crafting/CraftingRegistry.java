package tfc.smallerunits.crafting;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

//https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/version/1.16.3/src/main/java/mod/chiselsandbits/registry/ModRecipeSerializers.java
public class CraftingRegistry {
	public static final DeferredRegister<IRecipeSerializer<?>> recipeSerializers = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "smallerunits");
	
	public static final RegistryObject<IRecipeSerializer<UnitSizingRecipe>> SIZING = recipeSerializers.register("su_resizing", () -> new SpecialRecipeSerializer<>(UnitSizingRecipe::new));
	public static final RegistryObject<IRecipeSerializer<SUTileRecipe>> TILE = recipeSerializers.register("su_tile", () -> new SpecialRecipeSerializer<>(SUTileRecipe::new));
	public static final RegistryObject<IRecipeSerializer<HammerRecipe>> HAMMER = recipeSerializers.register("su_hammer", () -> new SpecialRecipeSerializer<>(HammerRecipe::new));
}
