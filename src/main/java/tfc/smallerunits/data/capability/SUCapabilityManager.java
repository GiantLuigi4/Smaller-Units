package tfc.smallerunits.data.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.access.tracking.FastCapabilityHandler;
import tfc.smallerunits.networking.platform.PacketTarget;

// so I mostly just abandoned any documentation that I was given and write this
// CCA's readme is actually extremely good
// way better than anything I've ever had on forge
// https://github.com/OnyxStudios/Cardinal-Components-API/blob/1.18/README.md
// this whole class is basically just a wrapper around forge's method of storing additional data to a chunk
// on fabric, it should either be pretty similar or simpler
public class SUCapabilityManager {
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
		return getCap(chunk);
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
		return getCap((LevelChunk) access);
	}
	
	private static ISUCapability getCap(LevelChunk chunk) {
		//#if FABRIC==1
		//$$return ((dev.onyxstudios.cca.api.v3.component.ComponentProvider) chunk).getComponent(AttachmentRegistry.SU_CAPABILITY_COMPONENT_KEY);
		//#else
		return chunk.getCapability(AttachmentRegistry.SU_CAPABILITY_TOKEN).orElse(null);
		//#endif
	}
	
	// for CCA, most of this stuff can be automated via an AutoSyncedComponent
	// as for the BE tracking stuff, I believe this would come in handy,
	// https://github.com/OnyxStudios/Cardinal-Components-API/blob/d059688e5329be0e6e3dc9f09af9c165767701e6/cardinal-components-chunk/src/main/java/dev/onyxstudios/cca/api/v3/chunk/ChunkSyncCallback.java#L35-L39
	
	public static void onChunkLoad(LevelChunk chunk) {
		ISUCapability capability = SUCapabilityManager.getCapability(chunk);
		for (UnitSpace unit : capability.getUnits()) unit.tick();
	}
	
	/**
	 * Tracks when a player starts to watch a chunk
	 * When this occurs, take whichever server level is used for that specific region file
	 * Next, take all unit tile entities in the specific chunk
	 * Then, start creating packets which hold the data which the client needs of the units in the chunk
	 * Finally, send these two the client
	 * <p>
	 * Actually no, that's not finally
	 * *Finally*, iterate through every small block entity and tell the tile that the player has started to track it
	 */
	public static void onChunkWatch(ServerPlayer player, LevelChunk chunk) {
		if (player != null) {
			ISUCapability capability = SUCapabilityManager.getCapability(chunk);
			if (capability == null) return;
			for (UnitSpace unit : capability.getUnits()) {
				if (unit == null) continue;
				unit.sendSync(PacketTarget.player(player));
			}
		}
	}
	
	public static void ip$onChunkWatch(ServerPlayer player, LevelChunk chunk) {
		if (player != null) {
			ISUCapability capability = SUCapabilityManager.getCapability(chunk);
			if (capability == null) return;
			for (UnitSpace unit : capability.getUnits()) {
				if (unit == null) continue;
				unit.sendRedirectableSync(player);
			}
		}
	}
}
