package tfc.smallerunits.mixin.compat.optimization.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.SUChunkRender;

import java.lang.ref.WeakReference;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin implements SUCompiledChunkAttachments {
	WeakReference<SUCapableChunk> capableChunk;
	@Unique
	private SUChunkRender compChunk;
	
	@Override
	public SUCapableChunk getSUCapable() {
		if (capableChunk == null) return null;
		return capableChunk.get();
	}
	
	@Override
	public void setSUCapable(int yCoord, SUCapableChunk chunk) {
		if (chunk instanceof EmptyLevelChunk) return;
		this.capableChunk = new WeakReference<>(chunk);
		compChunk = chunk.SU$getRenderer(yCoord);
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
		return compChunk;
	}
}
