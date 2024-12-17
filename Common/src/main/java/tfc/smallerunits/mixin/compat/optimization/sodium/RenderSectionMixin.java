package tfc.smallerunits.mixin.compat.optimization.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SUChunkRender;

import java.lang.ref.WeakReference;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin implements SUCompiledChunkAttachments {
	WeakReference<SUCapableChunk> capableChunk;
	
	@Override
	public SUCapableChunk getSUCapable() {
		if (capableChunk == null) return null;
		return capableChunk.get();
	}
	
	@Override
	public void setSUCapable(SUCapableChunk chunk) {
		if (chunk instanceof EmptyLevelChunk) return;
		this.capableChunk = new WeakReference<>(chunk);
	}

	@Override
	public void markForCull() {
		throw new RuntimeException("TODO");
	}

	@Override
	public boolean needsCull() {
		return true;
	}

	public void markCulled()  {
		throw new RuntimeException("TODO");
	}

	@Override
	public SUChunkRender SU$getChunkRender() {
		throw new RuntimeException("TODO");
	}
}
