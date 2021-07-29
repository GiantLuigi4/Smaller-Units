package tfc.smallerunits.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import tfc.smallerunits.registry.Deferred;

import java.util.HashMap;

//TODO: migrate to json
public class HammerRecipe extends SpecialRecipe {
	public static final String[] shape = new String[]{
			" *#",
			" /*",
			"/  "
	};
	
	public HammerRecipe(ResourceLocation idIn) {
		super(idIn);
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		return !getHammerItem(inv).equals(Items.AIR);
	}
	
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		return new ItemStack(getHammerItem(inv));
	}
	
	public Item getHammerItem(CraftingInventory inv) {
		{
			boolean matches = true;
			HashMap<Character, Item> itemHashMap = new HashMap<>();
			itemHashMap.put(' ', Items.AIR);
			itemHashMap.put('/', Items.STICK);
			itemHashMap.put('*', Items.LAPIS_LAZULI);
			itemHashMap.put('#', Items.AIR);
			int index = 0;
			for (String s : shape) {
				for (char c : s.toCharArray()) {
					ItemStack stack = inv.getStackInSlot(index);
					if (!stack.getItem().getRegistryName().equals(itemHashMap.get(c).getRegistryName())) {
						matches = false;
						break;
					}
					index++;
				}
				if (!matches) break;
			}
			if (matches) return Deferred.SHRINKER.get();
		}
		{
			boolean matches = true;
			HashMap<Character, Item> itemHashMap = new HashMap<>();
			itemHashMap.put(' ', Items.AIR);
			itemHashMap.put('/', Items.STICK);
			itemHashMap.put('*', Items.REDSTONE);
			itemHashMap.put('#', Items.LAPIS_LAZULI);
			int index = 0;
			for (String s : shape) {
				for (char c : s.toCharArray()) {
					ItemStack stack = inv.getStackInSlot(index);
					if (!stack.getItem().getRegistryName().equals(itemHashMap.get(c).getRegistryName())) {
						matches = false;
						break;
					}
					index++;
				}
				if (!matches) break;
			}
			return matches ? Deferred.GROWER.get() : Items.AIR;
		}
	}
	
	@Override
	public boolean canFit(int width, int height) {
		return width >= 3 && height >= 3;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return CraftingRegistry.HAMMER.get();
	}
}
