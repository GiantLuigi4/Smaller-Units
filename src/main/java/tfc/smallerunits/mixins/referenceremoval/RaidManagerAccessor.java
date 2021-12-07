package tfc.smallerunits.mixins.referenceremoval;

import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RaidManager.class)
public interface RaidManagerAccessor {
	@Accessor("world")
	void setWorld(ServerWorld newWorld);
}
