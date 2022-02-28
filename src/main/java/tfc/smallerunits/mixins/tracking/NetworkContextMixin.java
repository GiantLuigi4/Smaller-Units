package tfc.smallerunits.mixins.tracking;

import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.utils.accessor.WorldWrappedContext;

@Mixin(NetworkEvent.Context.class)
public class NetworkContextMixin implements WorldWrappedContext {
	public World parent;
	
	@Override
	public World SmallerUnits_getParentWorld() {
		return parent;
	}
	
	@Override
	public void SmallerUnits_setParentWorld(World wld) {
		parent = wld;
	}
}
