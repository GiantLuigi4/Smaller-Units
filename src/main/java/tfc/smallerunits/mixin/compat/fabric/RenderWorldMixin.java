package tfc.smallerunits.mixin.compat.fabric;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.client.render.util.RenderWorld;

@Mixin(RenderWorld.class)
public interface RenderWorldMixin extends RenderAttachedBlockView {
}
