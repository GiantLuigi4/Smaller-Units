package tfc.smallerunits.utils.vr.player;

import net.minecraft.world.entity.player.Player;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.platform.PlatformUtils;
import tfc.smallerunits.utils.vr.player.mods.VFE;
import tfc.smallerunits.utils.vr.player.mods.Vivecraft;

public class VRPlayerManager {
	public static SUVRPlayer getPlayer(Player player) {
		SUVRPlayer vrPlayer = getVivecraft(player);
		if (vrPlayer == null) return getVFE(player); // TODO: other mods?
		return vrPlayer;
	}
	
	private static SUVRPlayer getVFE(Player player) {
		if (SmallerUnits.isVFEPresent())
			return VFE.getPlayer(player);
		return null;
	}
	
	protected static SUVRPlayer getVivecraft(Player player) {
		if (SmallerUnits.isVivecraftPresent()) {
			if (player.level.isClientSide && PlatformUtils.isClient()) {
				if (IHateTheDistCleaner.isClientPlayer(player)) return Vivecraft.getPlayerClient();
				else return Vivecraft.getOtherClient(player);
			} else return Vivecraft.getPlayerJRBudda(player);
		}
		return null;
	}
}
