package tfc.smallerunits.crafting;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import tfc.smallerunits.TileResizingItem;
import tfc.smallerunits.utils.config.ServerConfig;

public class UnitResizingRecipe extends CustomRecipe {
	public UnitResizingRecipe(ResourceLocation idIn) {
		super(idIn);
	}
	
	@Override
	public boolean matches(CraftingContainer pContainer, Level pLevel) {
		if (!RecipeUtils.matchesShapelessResizing(pContainer)) return false;
		Pair<ItemStack, ItemStack> pair = RecipeUtils.getUnitAndHammer(pContainer);
		ItemStack stack1 = pair.getFirst();
		ItemStack stack2 = pair.getSecond();
		if (stack2 == null || stack2.isEmpty()) return false;
		int upb = stack1.getOrCreateTag().getInt("upb");
		if (upb <= ServerConfig.SizeOptions.minScale && ((TileResizingItem) stack2.getItem()).getScale() < 0)
			return false;
		return upb < ServerConfig.SizeOptions.maxScale || ((TileResizingItem) stack2.getItem()).getScale() <= 0;
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		return RecipeUtils.getRemainingItems(inv);
	}
	
	@Override
	public ItemStack assemble(CraftingContainer inv) {
		Pair<ItemStack, ItemStack> pair = RecipeUtils.getUnitAndHammer(inv);
		ItemStack stack1 = pair.getFirst();
		stack1.setCount(1);
		ItemStack stack2 = pair.getSecond();
		int upb = stack1.getOrCreateTag().getInt("upb");
		upb += ((TileResizingItem) stack2.getItem()).getScale();
		stack1.getOrCreateTag().putInt("upb", upb);
		return stack1;
	}
	
	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return pWidth >= 2 || pHeight >= 2;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return CraftingRegistry.SIZING.get();
	}
}