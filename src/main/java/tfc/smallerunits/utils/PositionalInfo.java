package tfc.smallerunits.utils;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.Random;
import java.util.UUID;

public class PositionalInfo {
	public final Vec3 pos;
	public final Level lvl;
	public final AABB box;
	public final float eyeHeight;
	private static final UUID SU_REACH_UUID = new UUID(new Random(847329).nextLong(), new Random(426324).nextLong());
	private boolean isReachSet = false;
	private Object particleEngine = null;
	public final Vec3 oPos;
	public final Vec3 oldPos;
	
	public PositionalInfo(Player pPlayer) {
		pos = new Vec3(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
		lvl = pPlayer.level;
		box = pPlayer.getBoundingBox();
		eyeHeight = pPlayer.eyeHeight;
		oPos = new Vec3(pPlayer.xo, pPlayer.yo, pPlayer.zo);
		oldPos = new Vec3(pPlayer.xOld, pPlayer.yOld, pPlayer.zOld);
	}
	
	public void scalePlayerReach(Player pPlayer, int upb) {
		AttributeInstance instance = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get());
		instance.removeModifier(SU_REACH_UUID);
		instance.addPermanentModifier(
				new AttributeModifier(SU_REACH_UUID, "su:reach", upb, AttributeModifier.Operation.MULTIPLY_TOTAL)
		);
		isReachSet = true;
	}
	
	public void adjust(Player pPlayer, UnitSpace space) {
		AABB scaledBB;
		pPlayer.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(this.box, this.pos, space.unitsPerBlock, space.regionPos));
		pPlayer.eyeHeight = (float) (this.eyeHeight * space.unitsPerBlock);
		pPlayer.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
		setupClient(pPlayer, space.getMyLevel());
		pPlayer.xo = HitboxScaling.scaleX(space, pPlayer.xo);
		pPlayer.yo = HitboxScaling.scaleY(space, pPlayer.yo);
		pPlayer.zo = HitboxScaling.scaleZ(space, pPlayer.zo);
		pPlayer.xOld = HitboxScaling.scaleX(space, pPlayer.xOld);
		pPlayer.yOld = HitboxScaling.scaleY(space, pPlayer.yOld);
		pPlayer.zOld = HitboxScaling.scaleZ(space, pPlayer.zOld);
		
		pPlayer.level = space.getMyLevel();
	}
	
	public void reset(Player pPlayer) {
		if (isReachSet) {
			AttributeInstance instance = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get());
			instance.removeModifier(SU_REACH_UUID);
			isReachSet = false;
		}
		pPlayer.level = lvl;
		if (FMLEnvironment.dist.isClient()) {
			if (pPlayer.level.isClientSide) {
				IHateTheDistCleaner.resetClient(pPlayer, lvl, particleEngine);
			}
		}
		pPlayer.setBoundingBox(box);
		pPlayer.setPosRaw(pos.x, pos.y, pos.z);
		pPlayer.xOld = oldPos.x;
		pPlayer.yOld = oldPos.y;
		pPlayer.zOld = oldPos.z;
		pPlayer.xo = oPos.x;
		pPlayer.yo = oPos.y;
		pPlayer.zo = oPos.z;
		pPlayer.eyeHeight = eyeHeight;
	}
	
	public void setupClient(Player player, Level spaceLevel) {
		if (FMLEnvironment.dist.isClient()) {
			if (player.level.isClientSide) {
				particleEngine = IHateTheDistCleaner.adjustClient(player, spaceLevel);
			}
		}
	}
}
