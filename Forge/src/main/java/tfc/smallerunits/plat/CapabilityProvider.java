package tfc.smallerunits.plat;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;

@ApiStatus.Internal
public class CapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	private final ISUCapability capability;
	private final LazyOptional<?> optional;
	
	/**
	 * attaches an SUCapability to a chunk
	 * attempts to optimize for performance in the parent world and for memory in the unit world
	 *
	 * @param event the event, which contains info about the chunk as well as the list of already attached capabilities... why am I explaining what the event contains, lol
	 */
	public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
		// TODO: check if the chunk is an instance of a SU chunk
		// if it is and instance of a SU chunk, use lightweightProvider (optimized for memory usage)
		// elsewise, use provider (optimized for speed)
		event.addCapability(
				new ResourceLocation("smallerunits", "unit_space_cap"),
				// I find it a bit ridiculous that I need a whole provider for every single chunk in the world...
				// but I guess it makes sense
				new CapabilityProvider(new SUCapability(event.getObject().getLevel(), event.getObject()))
		);
	}
	
	// I can't remember the CCA equivalent to this, but I know it's an entry point
	
	/**
	 * Runs during game load
	 *
	 * @param event the Event, what words about this do you want from me
	 */
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(ISUCapability.class);
	}
	
	public static final Capability<ISUCapability> SU_CAPABILITY_TOKEN = CapabilityManager.get(new CapabilityToken<>() {
	});
	
	public CapabilityProvider(ISUCapability capability) {
		this.capability = capability;
		optional = LazyOptional.of(() -> capability);
	}
	
	@NotNull
	@Override
	public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap.equals(SU_CAPABILITY_TOKEN)) // really?
			//noinspection unchecked
			return (LazyOptional<T>) optional;
		return LazyOptional.empty();
	}
	
	@Override
	public CompoundTag serializeNBT() {
		return capability.serializeNBT(new CompoundTag());
	}
	
	@Override
	public void deserializeNBT(CompoundTag nbt) {
		capability.deserializeNBT(0, nbt);
	}
}
