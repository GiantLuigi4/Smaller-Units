package tfc.smallerunits.simulation.level.server;

import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.Optional;

public class LevelSourceProviderProvider {
	public static ChunkGenerator createGenerator(String version, Level lvl) {
		return generator181(lvl);
	}
	
	public static ChunkGenerator generator181(Level lvl) {
		return new FlatLevelSource(
				new FlatLevelGeneratorSettings(
						new StructureSettings(false),
						lvl.registryAccess().registry(Registry.BIOME_REGISTRY).get()
				)
		);
	}
}
