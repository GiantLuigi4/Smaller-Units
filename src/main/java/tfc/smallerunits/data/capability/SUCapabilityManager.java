package tfc.smallerunits.data.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.network.PacketDistributor;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.access.tracking.FastCapabilityHandler;

// so I mostly just abandoned any documentation that I was given and write this
// CCA's readme is actually extremely good
// way better than anything I've ever had on forge
// https://github.com/OnyxStudios/Cardinal-Components-API/blob/1.18/README.md
// this whole class is basically just a wrapper around forge's method of storing additional data to a chunk
// on fabric, it should either be pretty similar or simpler
public class SUCapabilityManager {
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
				new CapabilityProvider(new SUCapability(event.getObject().getLevel()))
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
	
	/**
	 * Idk what there is to say about this
	 * Imo, the name says it all
	 *
	 * @param chunk the chunk in question
	 * @return the corresponding ISUCapability
	 */
	public static ISUCapability getCapability(LevelChunk chunk) {
		if (chunk instanceof FastCapabilityHandler)
			return ((FastCapabilityHandler) chunk).getSUCapability();
		return chunk.getCapability(SU_CAPABILITY_TOKEN, null).orElse(null);
	}
	
	public static ISUCapability getCapability(Level lvl, ChunkAccess chunk) {
		if (chunk instanceof LevelChunk) return getCapability((LevelChunk) chunk);
		return getCapability(lvl.getChunkAt(chunk.getPos().getWorldPosition()));
	}
	
	/**
	 * basically; I'm too lazy to fully port unimportant stuff sometimes
	 * This will likely be slower than calling the overload which takes a chunk
	 * Reason: this runs validation even if it doesn't need to, and if you already have the chunk, then you don't need to get the chunk again
	 * This method will get the chunk, validate it, return it's capability
	 */
	public static ISUCapability getCapability(Level world, ChunkPos pos) {
		ChunkAccess access = world.getChunk(/* CC safety */ pos.getWorldPosition());
		if (!(access instanceof LevelChunk)) return getCapability(world.getChunkAt(pos.getWorldPosition()));
		if (access instanceof FastCapabilityHandler chunk) return chunk.getSUCapability();
		return ((LevelChunk) access).getCapability(SU_CAPABILITY_TOKEN, null).orElse(null);
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
	 * <p>
	 * Actually no, that's not finally
	 * *Finally*, iterate through every small block entity and tell the tile that the player has started to track it
	 *
	 * @param event the event saying that the chunk has started to be tracked
	 */
	public static void onChunkWatchEvent(ChunkWatchEvent.Watch event) {
		if (event.getPlayer() != null) {
			ISUCapability capability = SUCapabilityManager.getCapability(event.getWorld(), event.getPos());
			if (capability == null) return;
			for (UnitSpace unit : capability.getUnits()) {
				if (unit == null) continue;
				unit.sendSync(PacketDistributor.PLAYER.with(event::getPlayer));
			}
			// TODO: create packet
		}
	}
}
