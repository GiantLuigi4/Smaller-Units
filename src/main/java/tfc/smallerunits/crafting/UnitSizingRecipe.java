package tfc.smallerunits.crafting;

import com.mojang.datafixers.util.Pair;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import tfc.smallerunits.TileResizingItem;
import tfc.smallerunits.config.SmallerUnitsConfig;

public class UnitSizingRecipe extends SpecialRecipe {
	public UnitSizingRecipe(ResourceLocation idIn) {
		super(idIn);
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
//		ItemStack stack1 = null;
//		ItemStack stack2 = null;
//		for (int i = 0; i < inv.getSizeInventory(); i++) {
//			if (stack1 == null) {
//				stack1 = inv.getStackInSlot(i);
//				if (stack1.isEmpty()) stack1 = null;
//			} else if (stack2 == null) {
//				stack2 = inv.getStackInSlot(i);
//				if (stack2.isEmpty()) stack2 = null;
//			} else {
//				return false;
//			}
//		}
//		if (stack1 == null || stack2 == null) return false;
//		if (stack2.getItem() instanceof UnitItem) {
//			ItemStack temp1 = stack1;
//			ItemStack temp2 = stack2;
//			stack2 = temp1;
//			stack1 = temp2;
//		}
		if (!RecipeUtils.matchesShapelessResizing(inv)) return false;
		Pair<ItemStack, ItemStack> pair = RecipeUtils.getUnitAndHammer(inv);
		ItemStack stack1 = pair.getFirst();
		ItemStack stack2 = pair.getSecond();
		if (stack2 == null || stack2.isEmpty()) return false;
		int upb = stack1.getOrCreateTag().getCompound("BlockEntityTag").getInt("upb");
		if (upb == SmallerUnitsConfig.SERVER.minUPB.get() && ((TileResizingItem) stack2.getItem()).getScale() < 0)
			return false;
		if (upb >= SmallerUnitsConfig.SERVER.maxUPB.get() && ((TileResizingItem) stack2.getItem()).getScale() > 0)
			return false;
		return true;
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return RecipeUtils.getRemainingItems(inv);
	}
	
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
//		ItemStack stack1 = null;
//		ItemStack stack2 = null;
//		for (int i = 0; i < inv.getSizeInventory(); i++) {
//			if (stack1 == null) {
//				stack1 = inv.getStackInSlot(i).copy();
//				if (stack1.isEmpty()) stack1 = null;
//			} else if (stack2 == null) {
//				stack2 = inv.getStackInSlot(i).copy();
//				if (stack2.isEmpty()) stack2 = null;
//			}
//		}
//		if (stack1 == null || stack2 == null) return null;
//		if (stack2.getItem() instanceof UnitItem) {
//			ItemStack temp1 = stack1;
//			ItemStack temp2 = stack2;
//			stack2 = temp1;
//			stack1 = temp2;
//		}
//		int upb = stack1.getOrCreateTag().getCompound("BlockEntityTag").getInt("upb");
//		upb += ((TileResizingItem) stack2.getItem()).getScale();
//		stack1.getOrCreateTag().getCompound("BlockEntityTag").putInt("upb", upb);
//		return stack1;
		Pair<ItemStack, ItemStack> pair = RecipeUtils.getUnitAndHammer(inv);
		ItemStack stack1 = pair.getFirst();
		stack1.setCount(1);
		ItemStack stack2 = pair.getSecond();
		int upb = stack1.getOrCreateTag().getCompound("BlockEntityTag").getInt("upb");
		upb += ((TileResizingItem) stack2.getItem()).getScale();
		stack1.getOrCreateTag().getCompound("BlockEntityTag").putInt("upb", upb);
		return stack1;
	}
	
	@Override
	public boolean canFit(int width, int height) {
		return width >= 2 || height >= 2;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return CraftingRegistry.SIZING.get();
	}
}
