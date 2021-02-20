package com.tfc.smallerunits;

import com.tfc.smallerunits.registry.Deferred;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TileResizingItem extends Item {
	private final int scale;
	
	public TileResizingItem(int scale) {
		super(new Properties().maxStackSize(1).group(Deferred.group));
		this.scale = scale;
	}
	
	public int getScale() {
		return -scale;
	}
	
	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return itemStack;
	}
	
	@Override
	public boolean hasContainerItem() {
		return true;
	}
}
