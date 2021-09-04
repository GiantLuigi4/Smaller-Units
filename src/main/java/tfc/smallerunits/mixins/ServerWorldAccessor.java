package tfc.smallerunits.mixins;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.WorldCapabilityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
	@Accessor
	void setCapabilityData(WorldCapabilityData capabilityData);
}
