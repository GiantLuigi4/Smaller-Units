package tfc.smallerunits.utils.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.vivecraft.gameplay.VRPlayer;
import tfc.smallerunits.Smallerunits;

/**
 * created due to the fact that compat with vivecraft would need one of three things:
 * A) a complete rewrite of smaller units
 * B) a complete rewrite of vivecraft selection logic and likely also a sizeable rewrite of vivecraft rendering
 * C) me to figure out how to compile against the vivecraft API
 */
public class RaytraceUtils {
	public static float getPct(Entity entity) {
		if (true) return 1;
		if (entity == null) return 0;
		if (FMLEnvironment.dist.isClient()) {
			if (entity.getEntityWorld().isRemote) {
				return Minecraft.getInstance().getRenderPartialTicks();
			}
		}
		return 0;
	}
	
	public static Vector3d getStartVector(Entity entity) {
		if (Smallerunits.isVivecraftPresent()) {
			// TODO: vivecraft compat
			if (FMLEnvironment.dist.isClient()) {
				if (Minecraft.getInstance().player.getUniqueID().equals(entity.getUniqueID())) {
					VRPlayer player = VRPlayer.get();
					// TODO: check that the player is in vr mode
					if (player != null && player.vrdata_world_render != null) {
						return player.vrdata_world_render.getController(0).getPosition();
					}
				}
			}
		}
		return entity.getEyePosition(getPct(entity));
	}
	
	public static Vector3d getLookVector(Entity entity) {
		if (Smallerunits.isVivecraftPresent()) {
			// TODO: vivecraft compat
			if (FMLEnvironment.dist.isClient()) {
				if (Minecraft.getInstance().player.getUniqueID().equals(entity.getUniqueID())) {
					VRPlayer player = VRPlayer.get();
					// TODO: check that the player is in vr mode
					if (player != null && player.vrdata_world_render != null) {
						return player.vrdata_world_render.getController(0).getDirection();
					}
				}
			}
		}
		return entity.getLook(getPct(entity));
	}
	
	public static double getReach(Entity entity) {
		double reach = 7;
		if (entity instanceof PlayerEntity)
			reach = ((LivingEntity) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		return reach;
	}
}
