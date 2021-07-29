package tfc.smallerunits.mixins;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// thank you immersive portals, very cool
// but in all seriousness, the check this is for should be done anyway and AT doesn't work on WorldRenderer$world
@Mixin(WorldRenderer.class)
public interface WorldRendererMixin {
	@Accessor("world")
	ClientWorld getWorld();
}
