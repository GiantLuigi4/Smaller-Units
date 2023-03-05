package tfc.smallerunits.mixin.compat.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin implements SUCompiledChunkAttachments {
	SUCapableChunk capableChunk;
	
	@Override
	public SUCapableChunk getSUCapable() {
		return capableChunk;
	}
	
	@Override
	public void setSUCapable(SUCapableChunk chunk) {
		this.capableChunk = chunk;
	}
}
