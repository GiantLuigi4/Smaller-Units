//package com.tfc.smallerunits.worldgen;
//
//import com.google.common.collect.ImmutableList;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import com.tfc.smallerunits.block.UnitTileEntity;
//import com.tfc.smallerunits.registry.Deferred;
//import net.minecraft.block.Blocks;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockReader;
//import net.minecraft.world.IWorld;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.provider.BiomeProvider;
//import net.minecraft.world.chunk.IChunk;
//import net.minecraft.world.gen.*;
//import net.minecraft.world.gen.feature.structure.StructureManager;
//import net.minecraft.world.gen.settings.DimensionStructuresSettings;
//
//import java.util.function.Supplier;
//
//public class SmallerUnitsChunkGenerator extends ChunkGenerator {
//	public static final Codec<SmallerUnitsChunkGenerator> CODEC = RecordCodecBuilder.create((p_236091_0_) -> {
//		return p_236091_0_.group(BiomeProvider.CODEC.fieldOf("biome_source").forGetter((p_236096_0_) -> {
//			return p_236096_0_.biomeProvider;
//		}), Codec.LONG.fieldOf("seed").stable().forGetter((p_236093_0_) -> {
//			return p_236093_0_.seed;
//		}), DimensionSettings.field_236098_b_.fieldOf("settings").forGetter((p_236090_0_) -> {
//			return p_236090_0_.settings;
//		})).apply(p_236091_0_, p_236091_0_.stable(SmallerUnitsChunkGenerator::new));
//	});
//	private final long seed;
//	protected final Supplier<DimensionSettings> settings;
//
//	private SmallerUnitsChunkGenerator(BiomeProvider provider, BiomeProvider provider1, long seed, Supplier<DimensionSettings> settings) {
//		super(provider, provider1, settings.get().getStructures(), seed);
//		this.seed = seed;
//		this.settings = settings;
//		parent = new NoiseChunkGenerator(provider, seed, settings);
//	}
//
//	public SmallerUnitsChunkGenerator(BiomeProvider biomeProvider, Long aLong, Supplier<DimensionSettings> dimensionSettingsSupplier) {
//		this(biomeProvider, biomeProvider, aLong, dimensionSettingsSupplier);
//	}
//
//	@Override
//	// getCodec
//	protected Codec<? extends ChunkGenerator> func_230347_a_() {
//		return CODEC;
//	}
//
//	ChunkGenerator parent;
//
//	@Override
//	// withSeed
//	public ChunkGenerator func_230349_a_(long p_230349_1_) {
//		parent = parent.func_230349_a_(p_230349_1_);
//		return this;
//	}
//
//	@Override
//	public void generateSurface(WorldGenRegion region, IChunk chunk) {
////		for (int x = 0; x < 4; x++) {
////			for (int y = 0; y < 4; y++) {
////				for (int z = 0; z < 4; z++) {
////					tileEntity.getFakeWorld().setBlockState(
////							new BlockPos(x,y,z),
////							parent.generateSurface();
////					)
////				}
////			}
////		}
//		System.out.println("h");
//	}
//
//	@Override
//	// spawnStructures
//	public void func_230352_b_(IWorld world, StructureManager structureManager, IChunk chunk) {
//		chunk.setBlockState(
//				new BlockPos(chunk.getPos().x, 0, chunk.getPos().z),
//				Deferred.UNIT.get().getDefaultState(), false
//		);
//		UnitTileEntity tileEntity = Deferred.UNIT_TE.get().create();
//		System.out.println(world.getClass());
//		((World) world).setTileEntity(
//				new BlockPos(chunk.getPos().x, 0, chunk.getPos().z),
//				tileEntity
//		);
//		WorldGenRegion region1 = new WorldGenRegion(tileEntity.worldServer, ImmutableList.of(tileEntity.worldServer.chunk));
//		parent.generateSurface(region1, tileEntity.worldServer.chunk);
//	}
//
//	@Override
//	public int getHeight(int x, int z, Heightmap.Type heightmapType) {
//		return 0;
//	}
//
//	@Override
//	// idk
//	public IBlockReader func_230348_a_(int x, int z) {
//		return null;
//	}
//}
