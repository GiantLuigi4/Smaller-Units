package tfc.smallerunits.Registry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.SmallerUnitBlock;
import tfc.smallerunits.SmallerUnitsITSER;
import tfc.smallerunits.SmallerUnitsTileEntity;

public class Deferred {
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, "smallerunits");
	public static final RegistryObject<Block> UNIT = BLOCKS.register("su", SmallerUnitBlock::new);
	
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, "smallerunits");
//	public static final RegistryObject<Item> UNITITEM = ITEMS.register("su", ()->new BlockItem(UNIT.get(),((Supplier<Item.Properties>)()->{try{return(new Properties().rarity(Rarity.create("su",TextFormatting.GREEN)));}catch(NoSuchMethodError err){return(new Item.Properties().rarity(Rarity.create("su",TextFormatting.GREEN)));}}).get()));
	public static final RegistryObject<Item> UNITITEM = ITEMS.register("su", ()->new BlockItem(UNIT.get(),new Item.Properties().rarity(Rarity.create("su",TextFormatting.GREEN)).setISTER(()->SmallerUnitsITSER::new)));
	
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, "smallerunits");
	public static final RegistryObject<TileEntityType<SmallerUnitsTileEntity>> TILE_ENTITY = TILE_ENTITIES.register("sute", () -> TileEntityType.Builder.create(SmallerUnitsTileEntity::new, UNIT.get()).build(null));
}