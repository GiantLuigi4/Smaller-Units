package tfc.smallerunits.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

//https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/version/1.16.3/src/main/java/mod/chiselsandbits/registry/ModRecipeSerializers.java
public class CraftingRegistry {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "smallerunits");
	
	public static final RegistryObject<RecipeSerializer<UnitResizingRecipe>> SIZING = RECIPES.register("su_resizing", () -> new SimpleRecipeSerializer<>(UnitResizingRecipe::new));
//	public static final RegistryObject<RecipeSerializer<SUTileRecipe>> TILE = RECIPES.register("su_tile", () -> new SimpleRecipeSerializer<>(SUTileRecipe::new));
}
