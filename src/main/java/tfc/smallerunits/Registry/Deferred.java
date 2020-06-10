package tfc.smallerunits.Registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.SmallerUnitBlock;
import tfc.smallerunits.SmallerUnitsITSER;
import tfc.smallerunits.SmallerUnitsTileEntity;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class Deferred {
	public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, "smallerunits");
	public static final RegistryObject<Block> UNIT = BLOCKS.register("su", SmallerUnitBlock::new);
	
	public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, "smallerunits");
//	public static final RegistryObject<Item> UNITITEM = ITEMS.register("su", ()->new BlockItem(UNIT.get(),((Supplier<Item.Properties>)()->{try{return(new Properties().rarity(Rarity.create("su",TextFormatting.GREEN)));}catch(NoSuchMethodError err){return(new Item.Properties().rarity(Rarity.create("su",TextFormatting.GREEN)));}}).get()));
	public static final RegistryObject<Item> UNITITEM = ITEMS.register("su", ()->new BlockItem(UNIT.get(),new Item.Properties().rarity(Rarity.create("su",TextFormatting.GREEN)).setISTER(()->SmallerUnitsITSER::new)));
	
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, "smallerunits");
	public static final RegistryObject<TileEntityType<SmallerUnitsTileEntity>> TILE_ENTITY = TILE_ENTITIES.register("sute", () -> TileEntityType.Builder.create(SmallerUnitsTileEntity::new, UNIT.get()).build(null));

	public static final DeferredRegister<ModDimension> DIMENSIONS = new DeferredRegister<>(ForgeRegistries.MOD_DIMENSIONS, "smallerunits");
//	public static final RegistryObject<ModDimension> SMALLER_UNIT_SIMULATOR = DIMENSIONS.register("susimulator", () -> new ModDimension() {
//			@Override
//			public BiFunction<World, DimensionType, ? extends Dimension> getFactory() {
//				return new BiFunction<World, DimensionType, Dimension>() {
//					@Override
//					public Dimension apply(World world, DimensionType dimensionType) {
//						return new Dimension(world,dimensionType,0) {
//							@Override
//							public ChunkGenerator<?> createChunkGenerator() {
//								return new ChunkGenerator<OverworldGenSettings>(this.getWorld(), new BiomeProvider(ImmutableSet.of(Biomes.PLAINS)) {
//									@Override
//									public Biome getNoiseBiome(int x, int y, int z) {
//										return Biomes.PLAINS;
//									}
//								},new OverworldGenSettings()) {
//									@Override
//									public void generateSurface(WorldGenRegion p_225551_1_, IChunk p_225551_2_) {
//									}
//
//									@Override
//									public int getGroundHeight() {
//										return 0;
//									}
//
//									@Override
//									public void makeBase(IWorld worldIn, IChunk chunkIn) {
//									}
//
//									@Override
//									public int func_222529_a(int p_222529_1_, int p_222529_2_, Heightmap.Type heightmapType) {
//										return 0;
//									}
//								};
//							}
//
//							@Nullable
//							@Override
//							public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
//								return null;
//							}
//
//							@Nullable
//							@Override
//							public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
//								return null;
//							}
//
//							@Override
//							public float calculateCelestialAngle(long worldTime, float partialTicks) {
//								return 0;
//							}
//
//							@Override
//							public boolean isSurfaceWorld() {
//								return false;
//							}
//
//							@Override
//							public Vec3d getFogColor(float celestialAngle, float partialTicks) {
//								return new Vec3d(0,0,0);
//							}
//
//							@Override
//							public boolean canRespawnHere() {
//								return false;
//							}
//
//							@Override
//							public boolean doesXZShowFog(int x, int z) {
//								return false;
//							}
//						};
//					}
//				};
//			};
//		}
//	);
}
