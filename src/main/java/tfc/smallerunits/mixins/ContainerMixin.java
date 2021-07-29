package tfc.smallerunits.mixins;

import net.minecraft.inventory.container.Container;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Container.class)
public class ContainerMixin {
	public boolean canCloseNaturally = true;
	
	public void setCanCloseNaturally(boolean canCloseNaturally) {
		this.canCloseNaturally = canCloseNaturally;
	}
	
	public boolean canCloseNaturally() {
		return canCloseNaturally;
	}
}
