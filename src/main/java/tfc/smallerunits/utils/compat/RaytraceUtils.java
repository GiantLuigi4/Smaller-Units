package tfc.smallerunits.utils.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.vivecraft.gameplay.VRPlayer;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;
import tfc.smallerunits.utils.compat.vr.vivecraft.ViveSettings;

/**
 * created due to the fact that compat with vivecraft would need one of three things:
 * A) a complete rewrite of smaller units
 * B) a complete rewrite of vivecraft selection logic and likely also a sizeable rewrite of vivecraft rendering
 * C) me to figure out how to compile against the vivecraft API
 * <p>
 * I went with C
 */
public class RaytraceUtils {
	// don't ask
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
	
	private static final int LEFT = 0;
	private static final int RIGHT = 0;
	
	public static Vector3d getStartVector(Entity entity) {
		if (Smallerunits.isVivecraftPresent()) {
			try {
				if (FMLEnvironment.dist.isClient() && entity.getEntityWorld().isRemote) {
					if (Minecraft.getInstance().player.getUniqueID().equals(entity.getUniqueID())) {
						VRPlayer player = VRPlayer.get();
						// TODO: check that the player is in vr mode
						if (player != null && player.vrdata_world_render != null) {
							return player.vrdata_world_render.getController(ViveSettings.isReverseHands() ? LEFT : RIGHT).getPosition();
						}
					}
				} else {
					if (entity instanceof ServerPlayerEntity) {
						SUVRPlayer vivePlayer = SUVRPlayer.getPlayer((ServerPlayerEntity) entity);
						if (vivePlayer != null)
							return vivePlayer.getControllerPos(0);
					}
				}
			} catch (Throwable ignored) {
				if (!FMLEnvironment.production) System.out.println("Race conditions go brr!");
			}
		}
		return entity.getEyePosition(getPct(entity));
	}
	
	public static Vector3d getLookVector(Entity entity) {
		if (Smallerunits.isVivecraftPresent()) {
			try {
				if (FMLEnvironment.dist.isClient() && entity.getEntityWorld().isRemote) {
					if (Minecraft.getInstance().player.getUniqueID().equals(entity.getUniqueID())) {
						VRPlayer player = VRPlayer.get();
						// TODO: check that the player is in vr mode
						if (player != null && player.vrdata_world_render != null) {
							return player.vrdata_world_render.getController(ViveSettings.isReverseHands() ? LEFT : RIGHT).getDirection();
						}
					}
				} else {
					if (entity instanceof ServerPlayerEntity) {
						SUVRPlayer vivePlayer = SUVRPlayer.getPlayer((ServerPlayerEntity) entity);
						if (vivePlayer != null)
							return vivePlayer.getControllerAngle(0);
					}
				}
			} catch (Throwable ignored) {
				if (!FMLEnvironment.production) System.out.println("Race conditions go brr!");
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
