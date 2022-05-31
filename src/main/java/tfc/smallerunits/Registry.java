package tfc.smallerunits;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Registry {
	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(Block.class, "smallerunits");
	public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(Item.class, "smallerunits");
	public static final RegistryObject<Item> UNIT_SPACE_ITEM = ITEM_REGISTER.register("unit_space", UnitSpaceItem::new);
	public static final RegistryObject<Item> SHRINKER = ITEM_REGISTER.register("su_shrinker", () -> new TileResizingItem(-1));
	public static final CreativeModeTab tab = new CreativeModeTab("Smaller Units") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(SHRINKER.get());
		}
	};
	public static final RegistryObject<Item> GROWER = ITEM_REGISTER.register("su_grower", () -> new TileResizingItem(1));
	
	public static final RegistryObject<Block> UNIT_SPACE = BLOCK_REGISTER.register("unit_space", UnitSpaceBlock::new);
	// TODO: don't register this, maybe?
	public static final RegistryObject<Block> UNIT_EDGE = BLOCK_REGISTER.register("unit_edge", UnitEdge::new);
}
