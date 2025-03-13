package tfc.smallerunits.mixin.client.access;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Invoker
    void invokeRenderEntity(Entity $$0, double $$1, double $$2, double $$3, float $$4, PoseStack $$5, MultiBufferSource $$6);
}
