package tfc.smallerunits.utils.platform;

import io.netty.buffer.Unpooled;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;
import tfc.smallerunits.utils.asm.MixinConnector;

import java.util.ArrayList;

//#if FABRIC==1
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import tfc.smallerunits.utils.platform.hooks.ICullableBE;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.PositionalInfo;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerLevel;
//#else
//$$ import net.minecraftforge.common.ForgeMod;
//$$ import net.minecraftforge.common.MinecraftForge;
//$$ import net.minecraftforge.event.TickEvent;
//$$ import net.minecraftforge.event.level.LevelEvent;
//$$ import net.minecraftforge.fml.LogicalSide;
//$$ import net.minecraftforge.fml.ModList;
//$$ import net.minecraftforge.fml.loading.FMLEnvironment;
//#endif

public class PlatformUtils {
	//@formatter:off
	//#if FABRIC==1
	public static boolean isDevEnv() { return FabricLoader.getInstance().isDevelopmentEnvironment(); }
	public static boolean isLoaded(String mod) { return FabricLoader.getInstance().isModLoaded(mod); }
	public static boolean isClient() { return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT); }
	public static boolean shouldCaptureBlockSnapshots(Level level) { return false; }
	public static double getStepHeight(LocalPlayer player) { return player.maxUpStep; }
	//#else
//$$ 	public static boolean isDevEnv() { return !FMLEnvironment.production; }
//$$ 	public static boolean isLoaded(String mod) { return ModList.get().isLoaded(mod); }
//$$ 	public static boolean isClient() { return FMLEnvironment.dist.isClient(); }
//$$ 	public static boolean shouldCaptureBlockSnapshots(Level level) { return level.captureBlockSnapshots; }
//$$ 	public static double getStepHeight(LocalPlayer player) { return player.getStepHeight(); }
	//#endif
	//@formatter:on
	
	public static ResourceLocation getRegistryName(BlockEntity be) {
		return Registry.BLOCK_ENTITY_TYPE.getKey(be.getType());
	}
	
	public static double getReach(LivingEntity entity, double reach) {
		//#if FABRIC==1
		AttributeInstance instance = entity.getAttribute(ReachEntityAttributes.REACH);
		if (instance == null) return reach;
		AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);

		for (AttributeModifier instanceModifier : instance.getModifiers())
			if (instanceModifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_BASE))
				reach *= instanceModifier.getAmount();

		for (AttributeModifier instanceModifier : instance.getModifiers())
			if (instanceModifier.getOperation().equals(AttributeModifier.Operation.ADDITION))
				reach += instanceModifier.getAmount();

		for (AttributeModifier instanceModifier : instance.getModifiers())
			if (instanceModifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_TOTAL))
				if (!instanceModifier.equals(modifier))
					reach *= instanceModifier.getAmount();

		if (modifier != null)
			reach *= modifier.getAmount();

		//#else
//$$ 		if (entity.getAttributes().hasAttribute(ForgeMod.REACH_DISTANCE.get()))
//$$ 			reach = entity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		//#endif
		
		return reach;
	}
	
	//@formatter:off
	public static double getReach(LivingEntity entity) { return getReach(entity, 7); }
	
	//#if FABRIC==1
	public static void preTick(TickerServerLevel level) { ServerTickEvents.START_WORLD_TICK.invoker().onStartTick(level); }
	public static void postTick(TickerServerLevel level) { ServerTickEvents.END_WORLD_TICK.invoker().onEndTick(level); }
	public static void postUnload(Level level) { ServerWorldEvents.UNLOAD.invoker().onWorldUnload(level.getServer(), (ServerLevel) level); }
	public static <T extends BlockEntity> AABB getRenderBox(T pBlockEntity) { return ICullableBE.getCullingBB(pBlockEntity); }
	public static AttributeInstance getReachAttrib(LivingEntity livingEntity) { return livingEntity.getAttribute(ReachEntityAttributes.REACH); }
	//#else
//$$ 	public static void preTick(TickerServerLevel level) { new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.START, level, () -> true); }
//$$ 	public static void postTick(TickerServerLevel level) { new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, () -> true); }
//$$ 	public static void postUnload(Level level) { MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(level)); }
//$$ 	public static <T extends BlockEntity> AABB getRenderBox(T pBlockEntity) { return pBlockEntity.getRenderBoundingBox(); }
//$$ 	public static AttributeInstance getReachAttrib(LivingEntity livingEntity) { return livingEntity.getAttribute(ForgeMod.ATTACK_RANGE.get()); }
	//#endif
	//@formatter:on
	
	public static CompoundTag getCapTag(Object level) {
		//#if FABRIC==1
		if (level instanceof ComponentProvider provider)
			return provider.getComponentContainer().toTag(new CompoundTag());
		throw new RuntimeException(level + " is not a component provider.");
		//#else
//$$ 		if (level instanceof TickerServerLevel serverLevel)
//$$ 			return serverLevel.getCaps().serializeNBT();
//$$ 		else throw new RuntimeException(level + " is not a ticker server level.");
		//#endif
	}
	
	public static void readCaps(Object level, CompoundTag tag) {
		//#if FABRIC==1
		if (level instanceof ComponentProvider provider)
			provider.getComponentContainer().fromTag(tag);
		else throw new RuntimeException(level + " is not a component provider.");
		//#else
//$$ 		if (level instanceof TickerServerLevel serverLevel)
//$$ 			serverLevel.getCaps().deserializeNBT(tag);
//$$ 		else throw new RuntimeException(level + " is not a ticker server level.");
		//#endif
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
		//#if FABRIC==1
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
		//#endif
	}
	
	private static boolean hasConfigLib() {
		return !MixinConnector.isFabric || isLoaded("cloth-config2");
	}
	
	public static void beLoaded(BlockEntity pBlockEntity, Level level) {
		//#if FABRIC==1
		if (level.isClientSide)
			IHateTheDistCleaner.loadBe(pBlockEntity, level);
		else
			ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(pBlockEntity, (ServerLevel) level);
		//#else
//$$ 		pBlockEntity.onLoad();
		//#endif
	}
	
	public static FriendlyByteBuf newByteBuf() {
		return new FriendlyByteBuf(Unpooled.buffer());
	}
}
