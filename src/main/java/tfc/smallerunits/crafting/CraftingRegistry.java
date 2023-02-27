package tfc.smallerunits.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import tfc.smallerunits.utils.platform.registry.RecipeRegister;

import java.util.function.Supplier;

//https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/version/1.16.3/src/main/java/mod/chiselsandbits/registry/ModRecipeSerializers.java
public class CraftingRegistry {
	public static final RecipeRegister<RecipeSerializer<?>> RECIPES = new RecipeRegister<>("smallerunits");
	
	public static final Supplier<RecipeSerializer<UnitResizingRecipe>> SIZING = RECIPES.register("su_resizing", () -> new SimpleRecipeSerializer<>(UnitResizingRecipe::new));
}
