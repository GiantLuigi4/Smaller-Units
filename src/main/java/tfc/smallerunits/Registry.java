package tfc.smallerunits;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import tfc.smallerunits.utils.platform.registry.GenericRegister;

import java.util.function.Supplier;

public class Registry {
	public static final GenericRegister<Block> BLOCK_REGISTER = new GenericRegister(Block.class, "smallerunits");
	public static final GenericRegister<Item> ITEM_REGISTER = new GenericRegister(Item.class, "smallerunits");
	
	public static final Supplier<Item> UNIT_SPACE_ITEM = ITEM_REGISTER.register("unit_space", UnitSpaceItem::new);
	public static final Supplier<Item> SHRINKER = ITEM_REGISTER.register("su_shrinker", () -> new TileResizingItem(-1));
	
	public static final CreativeModeTab tab = FabricItemGroupBuilder.create(new ResourceLocation("smallerunits:inventory_tab")).icon(() -> new ItemStack(SHRINKER.get())).build();
	public static final Supplier<Item> GROWER = ITEM_REGISTER.register("su_grower", () -> new TileResizingItem(1));
	
	public static final Supplier<Block> UNIT_SPACE = BLOCK_REGISTER.register("unit_space", UnitSpaceBlock::new);
	// TODO: don't register this, maybe?
	public static final Supplier<Block> UNIT_EDGE = BLOCK_REGISTER.register("unit_edge", UnitEdge::new);
}
