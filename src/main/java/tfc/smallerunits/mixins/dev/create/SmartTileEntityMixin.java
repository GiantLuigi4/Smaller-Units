package tfc.smallerunits.mixins.dev.create;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tfc.smallerunits.utils.accessor.IHaveWorld;

// create has a bit of a mapping issue when loaded in an MCP dev envro
@Mixin(SmartTileEntity.class)
public class SmartTileEntityMixin {
	/**
	 * @author GiantLuigi4
	 * Reason: patch over mapping issue with MCP dev envros
	 */
	@Overwrite
	public World getWorld() {
		return ((IHaveWorld) this).SmallerUnits_getWorld();
	}
}
