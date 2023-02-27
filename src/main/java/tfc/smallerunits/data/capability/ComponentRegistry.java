package tfc.smallerunits.data.capability;

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

public class ComponentRegistry implements ChunkComponentInitializer {
	public static final ComponentKey<SUCapability> SU_CAPABILITY_COMPONENT_KEY =
			ComponentRegistryV3.INSTANCE.getOrCreate(new ResourceLocation("smallerunits:unit_space_cap"), SUCapability.class);
	
	@Override
	public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
		registry.register(SU_CAPABILITY_COMPONENT_KEY, (a) -> {
			if (a instanceof LevelChunk lvlChk) {
				return new SUCapability(a, lvlChk.getLevel());
			} else if (a.getHeightAccessorForGeneration() instanceof Level level) {
				return new SUCapability(a, level);
			} else if (a.levelHeightAccessor instanceof Level level) {
				return new SUCapability(a, level);
			}
			throw new RuntimeException("uhhhh I ned help");
		});
	}
}
