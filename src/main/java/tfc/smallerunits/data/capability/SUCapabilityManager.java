package tfc.smallerunits.data.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;

// so I mostly just abandoned any documentation that I was given and write this
// CCA's readme is actually extremely good
// way better than anything I've ever had on forge
// https://github.com/OnyxStudios/Cardinal-Components-API/blob/1.18/README.md
// this whole class is basically just a wrapper around forge's method of storing additional data to a chunk
// on fabric, it should either be pretty similar or simpler
public class SUCapabilityManager {
	private static final CapabilityProvider provider = new CapabilityProvider();
	
	/**
	 * attaches an SUCapability to a chunk
	 * attempts to optimize for performance in the parent world and for memory in the unit world
	 * @param event the event, which contains info about the chunk as well as the list of already attached capabilities... why am I explaining what the event contains, lol
	 */
	public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
		// TODO: check if the chunk is an instance of a SU chunk
		// if it is and instance of a SU chunk, use lightweightProvider (optimized for memory usage)
		// elsewise, use provider (optimized for speed)
		// I'm not sure if CCA supports this
		// if it does not, when porting to fabric, choose either lightweightProvider or provider
		event.addCapability(
				new ResourceLocation("smallerunits", "unit_space_cap"),
				provider
		);
	}
	
	// I can't remember the CCA equivalent to this, but I know it's an entry point
	/**
	 * Runs during game load
	 * @param event the Event, what words about this do you want from me
	 */
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(ISUCapability.class);
	}
	
	public static final Capability<ISUCapability> SU_CAPABILITY_TOKEN = CapabilityManager.get(new CapabilityToken<>() {
	});
	
	/**
	 * Idk what there is to say about this
	 * Imo, the name says it all
	 * @param chunk the chunk in question
	 * @return the corresponding ISUCapability
	 */
	public static ISUCapability getCapability(LevelChunk chunk) {
		return chunk.getCapability(SU_CAPABILITY_TOKEN, null).orElse(null);
	}
	
	// for CCA, most of this stuff can be automated via an AutoSyncedComponent
	// as for the BE tracking stuff, I believe this would come in handy,
	// https://github.com/OnyxStudios/Cardinal-Components-API/blob/d059688e5329be0e6e3dc9f09af9c165767701e6/cardinal-components-chunk/src/main/java/dev/onyxstudios/cca/api/v3/chunk/ChunkSyncCallback.java#L35-L39
	/**
	 * Tracks when a player starts to watch a chunk
	 * When this occurs, take whichever server level is used for that specific region file
	 * Next, take all unit tile entities in the specific chunk
	 * Then, start creating packets which hold the data which the client needs of the units in the chunk
	 * Finally, send these two the client
	 *
	 * Actually no, that's not finally
	 * *Finally*, iterate through every small block entity and tell the tile that the player has started to track it
	 * @param event the event saying that the chunk has started to be tracked
	 */
	public static void onChunkWatchEvent(ChunkWatchEvent event) {
		if (event.getPlayer() instanceof ServerPlayer) {
			ISUCapability capability = SUCapabilityManager.getCapability(event.getWorld(), event.getPos());
			if (capability == null) return;
			// TODO: create packet
		}
	}
	
	/**
	 * basically; I'm too lazy to fully port unimportant stuff sometimes
	 * This will likely be slower than calling the overload which takes a chunk
	 * Reason: this runs validation even if it doesn't need to, and if you already have the chunk, then you don't need to get the chunk again
	 * This method will get the chunk, validate it, return it's capability
	 */
	@Deprecated(forRemoval = true)
	private static ISUCapability getCapability(ServerLevel world, ChunkPos pos) {
		ChunkAccess access = world.getChunk(/* CC safety */ pos.getWorldPosition());
		// potentially redundant validation
		if (!(access instanceof LevelChunk)) return null;
		return ((LevelChunk) access).getCapability(SU_CAPABILITY_TOKEN, null).orElse(null);
	}
}
