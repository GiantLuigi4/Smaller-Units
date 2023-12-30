package tfc.smallerunits;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import tfc.smallerunits.plat.PlatformRegistry;
import tfc.smallerunits.plat.util.PlatformUtils;

import java.util.function.Supplier;

public class Registry {
	public static final PlatformRegistry<Block> BLOCK_REGISTER = new PlatformRegistry(Block.class, "smallerunits");
	public static final PlatformRegistry<Item> ITEM_REGISTER = new PlatformRegistry(Item.class, "smallerunits");
	public static final Supplier<Item> UNIT_SPACE_ITEM = ITEM_REGISTER.register("unit_space", UnitSpaceItem::new);
	public static final Supplier<Item> SHRINKER = ITEM_REGISTER.register("su_shrinker", () -> new TileResizingItem(-1));
	public static final CreativeModeTab tab = PlatformUtils.tab("Smaller Units", SHRINKER);
	public static final Supplier<Item> GROWER = ITEM_REGISTER.register("su_grower", () -> new TileResizingItem(1));
	
	public static final Supplier<Block> UNIT_SPACE = BLOCK_REGISTER.register("unit_space", UnitSpaceBlock::new);
	// TODO: don't register this, maybe?
	public static final Supplier<Block> UNIT_EDGE = BLOCK_REGISTER.register("unit_edge", UnitEdge::new);
}
