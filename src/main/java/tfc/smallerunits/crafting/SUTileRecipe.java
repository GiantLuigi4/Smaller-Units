package tfc.smallerunits.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tfc.smallerunits.TileResizingItem;
import tfc.smallerunits.UnitItem;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.UnitPallet;

import java.util.ArrayList;

public class SUTileRecipe extends SpecialRecipe {
	public SUTileRecipe(ResourceLocation idIn) {
		super(idIn);
	}
	
	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		boolean hasResizer = false;
		int otherThings = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).getItem() instanceof UnitItem) return false;
			if (inv.getStackInSlot(i).getItem() instanceof TileResizingItem) {
				if (((TileResizingItem) inv.getStackInSlot(i).getItem()).getScale() > 0) {
					hasResizer = true;
				} else {
					return false;
				}
			} else if (inv.getStackInSlot(i).getItem() instanceof BlockItem) otherThings++;
			else if (!inv.getStackInSlot(i).isEmpty()) return false;
		}
		return hasResizer && otherThings <= 1;
	}
	
	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack otherThing = null;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).getItem() instanceof BlockItem) otherThing = inv.getStackInSlot(i);
		}
		ItemStack stack = new ItemStack(Deferred.UNITITEM.get());
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("upb", 4);
		if (otherThing != null) {
			ArrayList<SmallUnit> units = new ArrayList<>();
			units.add(new SmallUnit(new UnitPos(0, 64, 0, BlockPos.ZERO, 4), ((BlockItem) otherThing.getItem()).getBlock().getDefaultState()));
			UnitPallet pallet = new UnitPallet(units);
			nbt.put("containedUnits", pallet.nbt);
		}
		stack.getOrCreateTag().put("BlockEntityTag", nbt);
		return stack;
	}
	
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return RecipeUtils.getRemainingItems(inv);
	}
	
	@Override
	public boolean canFit(int width, int height) {
		return width >= 1 && height >= 1;
	}
	
	@Override
	public IRecipeSerializer<?> getSerializer() {
		return CraftingRegistry.TILE.get();
	}
}
