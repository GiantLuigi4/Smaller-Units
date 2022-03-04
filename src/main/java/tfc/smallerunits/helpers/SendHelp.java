package tfc.smallerunits.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.IBlockBreaker;
import tfc.smallerunits.utils.data.SUCapabilityManager;

// mojang why is this so complex
public class SendHelp {
	private static int counter = 0;
	
	// returns whether or not block breaking should be started
	public static boolean doStuff(boolean leftClick, PlayerController controller, PlayerEntity player, CallbackInfo ci) {
		if (leftClick) {
			counter = 0;
			RayTraceResult result = Minecraft.getInstance().objectMouseOver;
			if (result.getType() == RayTraceResult.Type.BLOCK) {
				if (result instanceof BlockRayTraceResult) {
					UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), ((BlockRayTraceResult) result).getPos());
					if (tileEntity == null) {
						((IBlockBreaker) controller).SmallerUnits_resetBreaking();
						return false;
					}
					controller.onPlayerDamageBlock(tileEntity.getPos(), ((BlockRayTraceResult) result).getFace());
					ci.cancel();
					return false;
				}
			}
			ci.cancel();
			return true;
		} else {
			if (counter == 0) {
				assert controller != null; // this should never be called while controller is null
				((IBlockBreaker) controller).SmallerUnits_resetBreaking();
				ci.cancel();
				counter = -1;
			} else if (counter != -1) {
				counter = 1;
			}
		}
		return false;
	}
}
