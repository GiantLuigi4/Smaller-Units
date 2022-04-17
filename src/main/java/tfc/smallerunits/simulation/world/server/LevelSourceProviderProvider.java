package tfc.smallerunits.simulation.world.server;

import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.Optional;

public class LevelSourceProviderProvider {
	public static ChunkGenerator createGenerator(String version, Level lvl) {
		return generator181(lvl);
	}
	
	public static ChunkGenerator generator181(Level lvl) {
		return new FlatLevelSource(
				lvl.registryAccess().registry(Registry.STRUCTURE_SET_REGISTRY).get(),
				new FlatLevelGeneratorSettings(
						Optional.empty(),
						lvl.registryAccess().registry(Registry.BIOME_REGISTRY).get()
				)
		);
	}
}
