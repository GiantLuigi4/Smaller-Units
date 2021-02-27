package com.tfc.smallerunits.crafting;

import com.mojang.datafixers.util.Pair;
import com.tfc.smallerunits.TileResizingItem;
import com.tfc.smallerunits.UnitItem;
import com.tfc.smallerunits.registry.Deferred;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class RecipeUtils {
	public static NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		
		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack item = inv.getStackInSlot(i);
			if (item.getItem() instanceof TileResizingItem) {
				ItemStack newStack = item.copy();
				nonnulllist.set(i, newStack);
			} else if (item.hasContainerItem()) {
				nonnulllist.set(i, item.getContainerItem());
			}
		}
		
		return nonnulllist;
	}
	
	public static boolean matchesShapelessResizing(CraftingInventory inventory) {
		int count = 0;
		UnitItem unitIfPresent = null;
		TileResizingItem hammer = null;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i).copy();
			count += stack.isEmpty() ? 0 : 1;
			if (count > 2) return false;
			else if (stack.getItem() instanceof UnitItem && unitIfPresent != null) return false;
			else if (stack.getItem() instanceof TileResizingItem && hammer != null) return false;
			else if (stack.getItem() instanceof UnitItem) unitIfPresent = ((UnitItem) stack.getItem());
			else if (stack.getItem() instanceof TileResizingItem) hammer = ((TileResizingItem) stack.getItem());
			else if (!stack.isEmpty()) return false;
		}
		if (hammer == null) return false;
		return hammer.getScale() > 0 || unitIfPresent != null;
	}
	
	public static Pair<ItemStack, ItemStack> getUnitAndHammer(CraftingInventory inventory) {
		int count = 0;
		ItemStack unitIfPresent = null;
		ItemStack hammer = null;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack stack = inventory.getStackInSlot(i).copy();
			count += stack.isEmpty() ? 0 : 1;
			if (count > 2) return null;
			else if (stack.getItem() instanceof UnitItem) unitIfPresent = stack;
			else if (stack.getItem() instanceof TileResizingItem) hammer = stack;
			else if (!stack.isEmpty()) return null;
		}
		if (unitIfPresent == null) {
			unitIfPresent = new ItemStack(Deferred.UNITITEM.get());
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("upb", 3);
			unitIfPresent.getOrCreateTag().put("BlockEntityTag", nbt);
		}
		return Pair.of(unitIfPresent, hammer);
	}
}
