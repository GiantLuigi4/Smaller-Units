package tfc.smallerunits;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class UnitItem extends BlockItem {
	public UnitItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}
	
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		CompoundNBT defaultNBT=new CompoundNBT();
		defaultNBT.putString("world","0,0,0,Block{minecraft:stone}");
		defaultNBT.putInt("upb",4);
		items.forEach((itemStack -> {if (itemStack.getItem() instanceof UnitItem)itemStack.getOrCreateTag().put("BlockEntityTag",defaultNBT);}));
		super.fillItemGroup(group, items);
	}
}
