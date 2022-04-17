package tfc.smallerunits.simulation.world.server.compatilayer;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import tfc.smallerunits.simulation.world.server.compatilayer.c1182.LevelSourceProvider182;

import java.util.List;

public class LevelSourceProvider {
	public static ChunkGenerator createGenerator(String version, Level lvl) {
		if (version.contains("1.18.2")) return LevelSourceProvider182.get(lvl);
		return generator181();
	}
	
	public static ChunkGenerator generator181() {
//		return null;
		return new FlatLevelSource(
				new FlatLevelGeneratorSettings(
						new StructureSettings(false),
						RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY)
				).withLayers(
						List.of(new FlatLayerInfo(0, Blocks.AIR)),
						new StructureSettings(false)
				)
		);
	}
}
