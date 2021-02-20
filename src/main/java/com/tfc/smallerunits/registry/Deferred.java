package com.tfc.smallerunits.registry;

import com.tfc.smallerunits.Group;
import com.tfc.smallerunits.SmallerUnitISTER;
import com.tfc.smallerunits.TileResizingItem;
import com.tfc.smallerunits.UnitItem;
import com.tfc.smallerunits.block.SmallerUnitBlock;
import com.tfc.smallerunits.block.UnitTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Deferred {
	public static final Group group = new Group("Smaller Units");
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "smallerunits");
	
	public static final RegistryObject<Block> UNIT = BLOCKS.register("su", SmallerUnitBlock::new);
	
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "smallerunits");
	public static final RegistryObject<Item> UNITITEM = ITEMS.register("su", () -> new UnitItem(UNIT.get(), new Item.Properties().rarity(Rarity.create("su", TextFormatting.GREEN))
			.setISTER(() -> SmallerUnitISTER::new)
	));
	public static final RegistryObject<Item> SHRINKER = ITEMS.register("su_shrinker", () -> new TileResizingItem(-1));
	public static final RegistryObject<Item> GROWER = ITEMS.register("su_grower", () -> new TileResizingItem(1));
	
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, "smallerunits");
	
	public static final RegistryObject<TileEntityType<UnitTileEntity>> UNIT_TE = TILE_ENTITIES.register("sute", () -> TileEntityType.Builder.create(UnitTileEntity::new, UNIT.get()).build(null));
}
