package tfc.smallerunits;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import tfc.smallerunits.utils.platform.registry.GenericRegister;

import java.util.function.Supplier;

public class Registry {
	public static final GenericRegister<Block> BLOCK_REGISTER = new GenericRegister(Block.class, "smallerunits");
	public static final GenericRegister<Item> ITEM_REGISTER = new GenericRegister(Item.class, "smallerunits");
	
	public static final Supplier<UnitSpaceItem> UNIT_SPACE_ITEM = ITEM_REGISTER.register("unit_space", UnitSpaceItem::new);
	public static final Supplier<TileResizingItem> SHRINKER = ITEM_REGISTER.register("su_shrinker", () -> new TileResizingItem(-1));
	public static final Supplier<TileResizingItem> GROWER = ITEM_REGISTER.register("su_grower", () -> new TileResizingItem(1));
	
	public static final Supplier<UnitSpaceBlock> UNIT_SPACE = BLOCK_REGISTER.register("unit_space", UnitSpaceBlock::new);
	// TODO: don't register this, maybe?
	public static final Supplier<UnitEdge> UNIT_EDGE = BLOCK_REGISTER.register("unit_edge", UnitEdge::new);
	
	//#if FABRIC==1
	public static final CreativeModeTab tab = net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder.create(new net.minecraft.resources.ResourceLocation("smallerunits:inventory_tab")).icon(() -> new ItemStack(SHRINKER.get())).appendItems((list) -> {
		UNIT_SPACE_ITEM.get().SU$fillItemCategory(null, list);
		list.add(new ItemStack(SHRINKER.get()));
		list.add(new ItemStack(GROWER.get()));
	}).build();
	//#else
//$$ 	public static final CreativeModeTab tab = new CreativeModeTab("Smaller Units") {
//$$ 		@Override
//$$ 		public ItemStack makeIcon() {
//$$ 			return new ItemStack(SHRINKER.get());
//$$ 		}
//$$ 	};
	//#endif
}
