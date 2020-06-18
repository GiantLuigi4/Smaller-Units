package tfc.smallerunits;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import tfc.smallerunits.Registry.Deferred;

public class UnitItem extends BlockItem {
	public UnitItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}
	
	//TODO:Fill item group with all pickblocked smaller units.
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		ItemStack stack=new ItemStack(Deferred.UNITITEM.get());
		CompoundNBT defaultNBT=new CompoundNBT();
		defaultNBT.putString("world","0,0,0,Block{minecraft:stone}");
		defaultNBT.putInt("upb",4);
		stack.getOrCreateTag().put("BlockEntityTag",defaultNBT);
		if (group.equals(Deferred.group)) {
			items.add(stack);
		}
		for (String s:Group.strings) {
			ItemStack stack2=new ItemStack(Deferred.UNITITEM.get());
			CompoundNBT nbt=new CompoundNBT();
			nbt.putString("world",s);
			nbt.putInt("upb",8);
			stack2.getOrCreateTag().put("BlockEntityTag",nbt);
			if (group.equals(Deferred.group)) {
				items.add(stack2);
			}
		}
		super.fillItemGroup(group, items);
	}

	//Just incase fill item group doesn't work due to some other mod being dumb.
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		CompoundNBT defaultNBT=new CompoundNBT();
		defaultNBT.putString("world","0,0,0,Block{minecraft:stone}");
		defaultNBT.putInt("upb",4);
		if (!stack.getOrCreateTag().contains("BlockEntityTag")) stack.getOrCreateTag().put("BlockEntityTag",defaultNBT);
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}
}
