package tfc.smallerunits.utils.vr.player;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.utils.IHateTheDistCleaner;

public class VRPlayerManager {
	public static SUVRPlayer getPlayer(Player player) {
		SUVRPlayer vrPlayer = getVivecraft(player);
//		if (vrPlayer == null) return null; // TODO: other mods?
		return vrPlayer;
	}
	
	protected static SUVRPlayer getVivecraft(Player player) {
		if (SmallerUnits.isVivecraftPresent()) {
			if (player.level.isClientSide && FMLEnvironment.dist.isClient()) {
				if (IHateTheDistCleaner.isClientPlayer(player)) {
					return Vivecraft.getPlayerClient();
				} else {
					// TODO:
					return null;
				}
			} else {
				return Vivecraft.getPlayerJRBudda(player);
			}
		}
		return null;
	}
}
