package tfc.smallerunits.mixin.compat.optimization.sodium;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
import tfc.smallerunits.client.render.compat.sodium.SodiumGridAttachments;

import java.util.HashMap;

@Mixin(ClientLevel.class)
public class LevelMixin implements SodiumGridAttachments {
	@Unique
	HashMap<ChunkPos, SUCompiledChunkAttachments> renderChunks = new HashMap<>();
	
	@Override
	public HashMap<ChunkPos, SUCompiledChunkAttachments> getRenderChunks() {
		return renderChunks;
	}
	
	@Inject(at = @At("TAIL"), method = "onChunkLoaded")
	public void preChunkLoad(ChunkPos p_171650_, CallbackInfo ci) {
		SUCapableChunk chk = (SUCapableChunk) ((ClientLevel) (Object) this).getChunk(p_171650_.x, p_171650_.z);
		getRenderChunks().put(p_171650_, new SUCompiledChunkAttachments() {
			@Override
			public SUCapableChunk getSUCapable() {
				return chk;
			}
			
			@Override
			public void setSUCapable(SUCapableChunk chunk) {
			}
		});
	}
	@Inject(at = @At("TAIL"), method = "unload")
	public void postUnload(LevelChunk p_104666_, CallbackInfo ci) {
		getRenderChunks().remove(p_104666_.getPos());
	}
}
