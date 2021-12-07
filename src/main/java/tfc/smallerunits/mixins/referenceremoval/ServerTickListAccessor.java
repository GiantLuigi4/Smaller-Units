package tfc.smallerunits.mixins.referenceremoval;

import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerTickList.class)
public interface ServerTickListAccessor {
	@Accessor("world")
	void setWorld(ServerWorld newWorld);
}
