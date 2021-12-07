package tfc.smallerunits.mixins.rendering.unit_in_block;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.utils.accessor.INeedPosition;

@Mixin(ChunkRenderDispatcher.ChunkRender.class)
public abstract class ChunkRenderMixin {
	@Shadow
	@Final
	private BlockPos.Mutable position;
	
	@Inject(at = @At("TAIL"), method = "makeCompileTaskChunk")
	public void postMakeTask(CallbackInfoReturnable<ChunkRenderDispatcher.ChunkRender.ChunkRenderTask> cir) {
		((INeedPosition) cir.getReturnValue()).SmallerUnits_setPos(this.position.toImmutable());
	}
}
