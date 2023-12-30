package tfc.smallerunits.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import tfc.smallerunits.plat.PlatformRegistry;

import java.util.function.Supplier;

//https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/version/1.16.3/src/main/java/mod/chiselsandbits/registry/ModRecipeSerializers.java
public class CraftingRegistry {
	public static final PlatformRegistry<RecipeSerializer<?>> RECIPES = new PlatformRegistry(RecipeSerializer.class, "smallerunits");
	
	public static final Supplier<RecipeSerializer<UnitResizingRecipe>> SIZING = (Supplier<RecipeSerializer<UnitResizingRecipe>>) (Object) RECIPES.register("su_resizing", () -> new SimpleRecipeSerializer<>(UnitResizingRecipe::new));
//	public static final RegistryObject<RecipeSerializer<SUTileRecipe>> TILE = RECIPES.register("su_tile", () -> new SimpleRecipeSerializer<>(SUTileRecipe::new));
}
