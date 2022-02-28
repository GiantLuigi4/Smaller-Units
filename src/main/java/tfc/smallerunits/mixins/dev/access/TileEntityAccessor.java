package tfc.smallerunits.mixins.dev.access;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.utils.accessor.IHaveWorld;

import javax.annotation.Nullable;

@Mixin(TileEntity.class)
public class TileEntityAccessor implements IHaveWorld {
	@Shadow
	@Nullable
	protected World world;
	
	@Override
	public World SmallerUnits_getWorld() {
		return world;
	}
}
