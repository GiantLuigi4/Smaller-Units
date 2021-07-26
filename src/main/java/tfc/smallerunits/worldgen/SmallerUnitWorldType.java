//package com.tfc.smallerunits.worldgen;
//
//import net.minecraft.util.registry.DynamicRegistries;
//import net.minecraft.util.registry.Registry;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.biome.provider.OverworldBiomeProvider;
//import net.minecraft.world.gen.ChunkGenerator;
//import net.minecraft.world.gen.DimensionSettings;
//import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
//import net.minecraftforge.common.world.ForgeWorldType;
//
//public class SmallerUnitWorldType extends ForgeWorldType {
//	public SmallerUnitWorldType(IChunkGeneratorFactory factory) {
//		super(factory);
//	}
//
//	public SmallerUnitWorldType(IBasicChunkGeneratorFactory factory) {
//		super(SmallerUnitWorldType::createGenerator);
//	}
//
//	public static ChunkGenerator createGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed, String generatorSettings) {
////		return super.createChunkGenerator(biomeRegistry, dimensionSettingsRegistry, seed, generatorSettings);
//		return new SmallerUnitsChunkGenerator(new OverworldBiomeProvider(seed, false, false, biomeRegistry), seed, () -> {
//			return (DimensionSettings)dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242734_c);
//		});
//	}
//
//	@Override
//	public DimensionGeneratorSettings createSettings(DynamicRegistries dynamicRegistries, long seed, boolean generateStructures, boolean generateLoot, String generatorSettings) {
//		return super.createSettings(dynamicRegistries, seed, generateStructures, generateLoot, generatorSettings);
//	}
//}
