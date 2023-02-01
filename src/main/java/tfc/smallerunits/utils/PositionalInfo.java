package tfc.smallerunits.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;

import java.util.Random;
import java.util.UUID;

public class PositionalInfo {
	public final Vec3 pos;
	public final Level lvl;
	public final AABB box;
	public final float eyeHeight;
	public static final UUID SU_REACH_UUID = new UUID(new Random(847329).nextLong(), new Random(426324).nextLong());
	private boolean isReachSet = false;
	private Object particleEngine = null;
	public final Vec3 oPos;
	public final Vec3 oldPos;
	
	public PositionalInfo(Entity pEntity) {
		this(pEntity, true);
	}
	
	public PositionalInfo(Entity pEntity, boolean cacheParticleEngine) {
		if (!RenderSystem.isOnRenderThread() && !RenderSystem.isOnGameThread()) {
			System.out.println("h");
		}
		pos = new Vec3(pEntity.getX(), pEntity.getY(), pEntity.getZ());
		lvl = pEntity.level;
		box = pEntity.getBoundingBox();
		eyeHeight = pEntity.eyeHeight;
		oPos = new Vec3(pEntity.xo, pEntity.yo, pEntity.zo);
		oldPos = new Vec3(pEntity.xOld, pEntity.yOld, pEntity.zOld);
		if (FMLEnvironment.dist.isClient() && cacheParticleEngine) {
			if (pEntity.getLevel().isClientSide) {
				if (pEntity instanceof Player player) {
					particleEngine = IHateTheDistCleaner.getParticleEngine(player);
					
					if (particleEngine instanceof UnitParticleEngine || particleEngine == null || player.level instanceof ITickerLevel || Minecraft.getInstance().level instanceof ITickerLevel) {
						System.out.println("stop");
					}
				}
			}
		}
	}
	
	public void scalePlayerReach(Player pPlayer, int upb) {
		AttributeInstance instance = pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get());
		instance.removeModifier(SU_REACH_UUID);
		instance.addPermanentModifier(
				new AttributeModifier(SU_REACH_UUID, "su:reach", upb, AttributeModifier.Operation.MULTIPLY_TOTAL)
		);
		isReachSet = true;
	}
	
	public void adjust(Entity pEntity, UnitSpace space) {
		adjust(pEntity, space.getMyLevel(), space.unitsPerBlock, space.regionPos, true);
	}
	
	public void adjust(Entity pEntity, UnitSpace space, boolean updateParticleEngine) {
		adjust(pEntity, space.getMyLevel(), space.unitsPerBlock, space.regionPos, updateParticleEngine);
	}
	
	public void adjust(Entity pEntity, Level level, int upb, RegionPos regionPos) {
		adjust(pEntity, level, upb, regionPos, true);
	}
	
	public void adjust(Entity pEntity, Level level, int upb, RegionPos regionPos, boolean updateParticleEngine) {
		AABB scaledBB;
		pEntity.setBoundingBox(scaledBB = HitboxScaling.getOffsetAndScaledBox(this.box, this.pos, upb, regionPos));
		pEntity.eyeHeight = (float) (this.eyeHeight * upb);
		pEntity.setPosRaw(scaledBB.getCenter().x, scaledBB.minY, scaledBB.getCenter().z);
		if (pEntity instanceof Player player)
			setupClient(player, level, updateParticleEngine);
		// TODO: fix this
		pEntity.xo = HitboxScaling.scaleX((ITickerLevel) level, pEntity.xo);
		pEntity.yo = HitboxScaling.scaleY((ITickerLevel) level, pEntity.yo);
		pEntity.zo = HitboxScaling.scaleZ((ITickerLevel) level, pEntity.zo);
		pEntity.xOld = HitboxScaling.scaleX((ITickerLevel) level, pEntity.xOld);
		pEntity.yOld = HitboxScaling.scaleY((ITickerLevel) level, pEntity.yOld);
		pEntity.zOld = HitboxScaling.scaleZ((ITickerLevel) level, pEntity.zOld);
		
		pEntity.level = level;
	}
	
	public void reset(Entity pEntity) {
		if (isReachSet) {
			if (pEntity instanceof LivingEntity livingEntity) {
				AttributeInstance instance = livingEntity.getAttribute(ForgeMod.REACH_DISTANCE.get());
				instance.removeModifier(SU_REACH_UUID);
				isReachSet = false;
			}
		}
		pEntity.level = lvl;
		if (pEntity.getLevel().isClientSide) {
			if (pEntity instanceof Player player) {
				resetClient(player);
				if (particleEngine != null)
					if (Minecraft.getInstance().particleEngine instanceof UnitParticleEngine || particleEngine == null) {
						System.out.println("why");
					}
				if (player.level instanceof ITickerLevel || Minecraft.getInstance().level instanceof ITickerLevel) {
					System.out.println("why");
				}
			}
		}
		pEntity.setBoundingBox(box);
		pEntity.setPosRaw(pos.x, pos.y, pos.z);
		pEntity.xOld = oldPos.x;
		pEntity.yOld = oldPos.y;
		pEntity.zOld = oldPos.z;
		pEntity.xo = oPos.x;
		pEntity.yo = oPos.y;
		pEntity.zo = oPos.z;
		pEntity.eyeHeight = eyeHeight;
	}
	
	public void resetClient(Player player) {
		if (FMLEnvironment.dist.isClient()) {
			if (player.level.isClientSide) {
				IHateTheDistCleaner.resetClient(player, lvl, particleEngine);
				
				if (player.level instanceof ITickerLevel || Minecraft.getInstance().level instanceof ITickerLevel) {
					System.out.println("WHY");
				}
				if (Minecraft.getInstance().particleEngine == null) {
					System.out.println("STOP");
				}
			}
		}
	}
	
	public void setupClient(Player player, Level spaceLevel, boolean updateParticleEngine) {
		if (FMLEnvironment.dist.isClient()) {
			if (player.level.isClientSide) {
				Object o = IHateTheDistCleaner.adjustClient(player, spaceLevel, particleEngine != null);
				
				if (Minecraft.getInstance().particleEngine == null) {
					System.out.println("STOP");
				}
//				if (updateParticleEngine && RenderSystem.isOnGameThread()) particleEngine = o;
//
//				// TODO: fix
//				if (particleEngine instanceof UnitParticleEngine) {
//					System.out.println("harold");
//				}
			}
		}
	}
}
