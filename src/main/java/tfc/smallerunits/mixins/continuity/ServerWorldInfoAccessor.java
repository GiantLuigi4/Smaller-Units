package tfc.smallerunits.mixins.continuity;

import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorldInfo.class)
public interface ServerWorldInfoAccessor {
	@Accessor("wasModded")
	void setModded(boolean val);
	
	@Accessor("initialized")
	void setInitialized(boolean b);
}
