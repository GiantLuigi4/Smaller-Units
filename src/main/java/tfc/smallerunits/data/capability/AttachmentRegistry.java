package tfc.smallerunits.data.capability;

//#if FABRIC==1
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class AttachmentRegistry implements ChunkComponentInitializer {
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
//#else
//$$import net.minecraft.core.Direction;
//$$import net.minecraft.nbt.CompoundTag;
//$$import net.minecraft.resources.ResourceLocation;
//$$import net.minecraft.world.level.chunk.LevelChunk;
//$$import net.minecraftforge.common.capabilities.*;
//$$import net.minecraftforge.common.util.INBTSerializable;
//$$import net.minecraftforge.common.util.LazyOptional;
//$$import net.minecraftforge.event.AttachCapabilitiesEvent;
//$$import org.jetbrains.annotations.NotNull;
//$$import org.jetbrains.annotations.Nullable;
//$$
//$$public class AttachmentRegistry implements ICapabilityProvider, INBTSerializable<CompoundTag> {
//$$	private final ISUCapability capability;
//$$	private final LazyOptional<?> optional;
//$$
//$$	public static final Capability<ISUCapability> SU_CAPABILITY_TOKEN = CapabilityManager.get(new CapabilityToken<>() {
//$$	});
//$$
//$$	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
//$$		event.register(ISUCapability.class);
//$$	}
//$$
//$$	public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
//$$		// TODO: check if the chunk is an instance of a SU chunk
//$$		// if it is and instance of a SU chunk, use lightweightProvider (optimized for memory usage)
//$$		// elsewise, use provider (optimized for speed)
//$$		event.addCapability(
//$$				new ResourceLocation("smallerunits", "unit_space_cap"),
//$$				// I find it a bit ridiculous that I need a whole provider for every single chunk in the world...
//$$				// but I guess it makes sense
//$$				new AttachmentRegistry(new SUCapability(event.getObject(), event.getObject().getLevel()))
//$$		);
//$$	}
//$$
//$$	public AttachmentRegistry(ISUCapability capability) {
//$$		this.capability = capability;
//$$		optional = LazyOptional.of(() -> capability);
//$$	}
//$$
//$$	@NotNull
//$$	@Override
//$$	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
//$$		if (cap.equals(SU_CAPABILITY_TOKEN)) // really?
//$$			//noinspection unchecked
//$$			return (LazyOptional<T>) optional;
//$$		return LazyOptional.empty();
//$$	}
//$$
//$$	@Override
//$$	public CompoundTag serializeNBT() {
//$$		return capability.serializeNBT();
//$$	}
//$$
//$$	@Override
//$$	public void deserializeNBT(CompoundTag nbt) {
//$$		capability.deserializeNBT(0, nbt);
//$$	}
//$$}
//#endif
