//package com.tfc.smallerunits.worldgen;
//
//import net.minecraft.world.gen.NoiseChunkGenerator;
//import net.minecraftforge.common.world.ForgeWorldType;
//import net.minecraftforge.fml.ModLoadingContext;
//import net.minecraftforge.fml.RegistryObject;
//import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import vazkii.quark.content.world.client.RealisticWorldType;
//
//public class WorldTypeRegistry {
//	public static DeferredRegister<ForgeWorldType> WORLD_TYPES = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, ModLoadingContext.get().getActiveNamespace());
//
//	public static final RegistryObject<ForgeWorldType> SU_WORLD_TYPE = WORLD_TYPES.register("smaller_units", () -> new SmallerUnitWorldType((a, b, c) -> null));
//
//	public static void init() {
//		WORLD_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
//	}
//}
