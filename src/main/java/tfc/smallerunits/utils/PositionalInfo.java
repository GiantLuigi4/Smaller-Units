package tfc.smallerunits.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.render.compat.UnitParticleEngine;
import tfc.smallerunits.simulation.world.client.FakeClientWorld;
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
	
	public PositionalInfo(Player pPlayer) {
		pos = new Vec3(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
		lvl = pPlayer.level;
		box = pPlayer.getBoundingBox();
		eyeHeight = pPlayer.eyeHeight;
		
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
		pPlayer.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(this.box, this.pos, space.unitsPerBlock));
		pPlayer.eyeHeight = (float) (this.eyeHeight * space.unitsPerBlock);
		pPlayer.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
		if (FMLEnvironment.dist.isClient()) {
			if (pPlayer.level.isClientSide) {
				Minecraft.getInstance().level = ((LocalPlayer) pPlayer).clientLevel = ((ClientLevel) space.getMyLevel());
				
				particleEngine = Minecraft.getInstance().particleEngine;
				UnitParticleEngine upe = ((FakeClientWorld) space.getMyLevel()).getParticleEngine();
				if (upe != null)
					Minecraft.getInstance().particleEngine = upe;
			}
		}
		pPlayer.level = space.getMyLevel();
	}
	
	public void reset(Player pPlayer) {
		if (isReachSet) {
			AttributeInstance instance = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get());
			instance.removeModifier(SU_REACH_UUID);
			isReachSet = false;
		}
		pPlayer.level = lvl;
		if (pPlayer.level.isClientSide) {
			if (pPlayer instanceof LocalPlayer) {
				((LocalPlayer) pPlayer).clientLevel = (ClientLevel) lvl;
				Minecraft.getInstance().level = ((LocalPlayer) pPlayer).clientLevel;
				
				if (particleEngine != null) Minecraft.getInstance().particleEngine = (ParticleEngine) particleEngine;
			}
		}
		pPlayer.setBoundingBox(box);
		pPlayer.setPosRaw(pos.x, pos.y, pos.z);
		pPlayer.eyeHeight = eyeHeight;
	}
}
