package tfc.smallerunits.utils.platform;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;
import tfc.smallerunits.utils.scale.ResizingUtils;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class PlatformUtils {
	public static boolean isDevEnv() {
		return !FMLEnvironment.production;
	}
	
	public static boolean isLoaded(String mod) {
		return ModList.get().isLoaded(mod);
	}
	
	public static boolean isClient() {
		return FMLEnvironment.dist.isClient();
	}
	
	public static ResourceLocation getRegistryName(BlockEntity be) {
		return Registry.BLOCK_ENTITY_TYPE.getKey(be.getType());
	}
	
//	public static double getReach(LivingEntity entity, double reach) {
//		AttributeInstance instance = entity.getAttribute(ReachEntityAttributes.REACH);
//		if (instance == null) return reach;
//		AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);
//
//		for (AttributeModifier instanceModifier : instance.getModifiers())
//			if (instanceModifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_BASE))
//				reach *= instanceModifier.getAmount();
//
//		for (AttributeModifier instanceModifier : instance.getModifiers())
//			if (instanceModifier.getOperation().equals(AttributeModifier.Operation.ADDITION))
//				reach += instanceModifier.getAmount();
//
//		for (AttributeModifier instanceModifier : instance.getModifiers())
//			if (instanceModifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_TOTAL))
//				if (!instanceModifier.equals(modifier))
//					reach *= instanceModifier.getAmount();
//
//		if (modifier != null)
//			reach *= modifier.getAmount();
//
//		return reach;
//	}
//
//	public static double getReach(LivingEntity entity) {
//		return getReach(entity, 7);
//	}
	
	public static boolean shouldCaptureBlockSnapshots(Level level) {
		return level.captureBlockSnapshots;
	}
	
	public static double getStepHeight(LocalPlayer player) {
		return player.maxUpStep;
	}
	
//	public static void postUnload(Level level) {
//		ServerWorldEvents.UNLOAD.invoker().onWorldUnload(level.getServer(), (ServerLevel) level);
//	}
//
//	public static <T extends BlockEntity> AABB getRenderBox(T pBlockEntity) {
//		return ICullableBE.getCullingBB(pBlockEntity);
//	}
//
//	public static AttributeInstance getReachAttrib(LivingEntity livingEntity) {
//		return livingEntity.getAttribute(ReachEntityAttributes.REACH);
//	}
//
//	public static CompoundTag getCapTag(Object level) {
//		if (level instanceof ComponentProvider provider)
//			return provider.getComponentContainer().toTag(new CompoundTag());
//		throw new RuntimeException(level + " is not a component provider.");
//	}
//
//	public static void readCaps(Object level, CompoundTag tag) {
//		if (level instanceof ComponentProvider provider)
//			provider.getComponentContainer().fromTag(tag);
//		throw new RuntimeException(level + " is not a component provider.");
//	}
	
	public static void preTick(TickerServerLevel level, BooleanSupplier pHasTimeLeft) {
		MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.START, level, pHasTimeLeft));
	}
	
	public static void postTick(TickerServerLevel level, BooleanSupplier pHasTimeLeft) {
		MinecraftForge.EVENT_BUS.post(new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, pHasTimeLeft));
	}
	
	public static PortalInfo createPortalInfo(Entity pEntity, ITickerLevel lvl) {
		Vec3 pos = pEntity.getPosition(1);
		BlockPos bp = lvl.getRegion().pos.toBlockPos();
		pos = pos.scale(1d / lvl.getUPB());
		pos = pos.add(bp.getX(), bp.getY(), bp.getZ());
		return new PortalInfo(
				pos,
				pEntity.getDeltaMovement(),
				pEntity.getYRot(),
				pEntity.getXRot()
		);
	}
	
	private static final ArrayList<Runnable> toRun = new ArrayList<>();
	
	public static void delayConfigInit(Runnable r) {
		if (hasConfigLib()) {
			if (r == null) {
				for (Runnable runnable : toRun) {
					runnable.run();
				}
				toRun.clear();
				return;
			}
			
			toRun.add(r);
		}
	}
	
	private static boolean hasConfigLib() {
		return true;
	}
	
	public static void beLoaded(BlockEntity pBlockEntity, Level level) {
//		if (level.isClientSide)
//			IHateTheDistCleaner.loadBe(pBlockEntity, level);
//		else
//			ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(pBlockEntity, (ServerLevel) level);
	}
	
	public static Entity migrateEntity(Entity pEntity, TickerServerLevel serverLevel, int upb, Level lvl) {
		return pEntity.changeDimension((ServerLevel) lvl, new ITeleporter() {
			@Override
			public Entity placeEntity(Entity entity1, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
				PortalInfo portalinfo = getPortalInfo(entity1, destWorld, null);
				
				currentWorld.getProfiler().popPush("reloading");
				Entity newEntity = entity1.getType().create(destWorld);
				if (newEntity != null) {
					ResizingUtils.resizeForUnit(entity1, 1f / upb);
					
					newEntity.restoreFrom(entity1);
					newEntity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, newEntity.getXRot());
					newEntity.setDeltaMovement(portalinfo.speed);
					destWorld.addFreshEntity(newEntity);
				}
				
				return newEntity;
			}
			
			@Nullable
			@Override
			public PortalInfo getPortalInfo(Entity entity1, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
				Vec3 pos = entity1.getPosition(1);
				BlockPos bp = serverLevel.getRegion().pos.toBlockPos();
				pos = pos.scale(1d / upb);
				pos = pos.add(bp.getX(), bp.getY(), bp.getZ());
				return new PortalInfo(
						pos,
						entity1.getDeltaMovement(),
						entity1.getYRot(),
						entity1.getXRot()
				);
			}
			
			@Override
			public boolean isVanilla() {
				return false;
			}
			
			@Override
			public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
				return false;
			}
		});
	}
	
	public static void loadLevel(TickerServerLevel serverLevel) {
		MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(serverLevel));
	}
	
	public static void altBeLoad(BlockEntity pBlockEntity) {
		pBlockEntity.onLoad();
	}
}
