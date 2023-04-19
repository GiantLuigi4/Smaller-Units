package tfc.smallerunits.mixin.data.access;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.access.ChunkHolderAccessor;

// https://github.com/iPortalTeam/ImmersivePortalsMod/blob/1.19/imm_ptl_core/src/main/java/qouteall/imm_ptl/core/mixin/common/chunk_sync/MixinChunkHolder.java
// AT doesn't work here if I wanna be compatible with IP
@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin implements ChunkHolderAccessor {
	@Shadow public abstract void broadcast(Packet<?> packet, boolean bl);
	
	@Override
	public void SU$call_broadcast(Packet<?> packet, boolean bl) {
		broadcast(packet, bl);
	}
}
