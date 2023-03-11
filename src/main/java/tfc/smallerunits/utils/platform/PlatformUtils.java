package tfc.smallerunits.utils.platform;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.simulation.level.server.TickerServerLevel;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.asm.MixinConnector;

import java.util.ArrayList;

public class PlatformUtils {
	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
	
	public static boolean isLoaded(String mod) {
		return FabricLoader.getInstance().isModLoaded(mod);
	}
	
	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT);
	}
	
	public static ResourceLocation getRegistryName(BlockEntity be) {
		return Registry.BLOCK_ENTITY_TYPE.getKey(be.getType());
	}
	
	public static double getReach(Player sender) {
		double reach = 7;
		AttributeInstance instance = sender.getAttribute(ReachEntityAttributes.REACH);
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
		
		return reach;
	}
	
	public static boolean shouldCaptureBlockSnapshots(Level level) {
		return false;
	}
	
	public static double getStepHeight(LocalPlayer player) {
		return player.maxUpStep;
	}
	
	public static void postUnload(Level level) {
		ServerWorldEvents.UNLOAD.invoker().onWorldUnload(level.getServer(), (ServerLevel) level);
	}
	
	public static <T extends BlockEntity> AABB getRenderBox(T pBlockEntity) {
		VoxelShape shape = pBlockEntity.getBlockState().getShape(pBlockEntity.getLevel(), pBlockEntity.getBlockPos());
		if (shape == Shapes.empty() || shape.isEmpty()) return new AABB(pBlockEntity.getBlockPos());
		return shape.bounds();
	}
	
	public static AttributeInstance getReachAttrib(LivingEntity livingEntity) {
		return livingEntity.getAttribute(ReachEntityAttributes.REACH);
	}
	
	public static CompoundTag getCapTag(ComponentProvider level) {
		return level.getComponentContainer().toTag(new CompoundTag());
	}
	
	public static void readCaps(ComponentProvider level, CompoundTag tag) {
		level.getComponentContainer().fromTag(tag);
	}
	
	public static void preTick(TickerServerLevel level) {
		ServerTickEvents.START_WORLD_TICK.invoker().onStartTick(level);
	}
	
	public static void postTick(TickerServerLevel level) {
		ServerTickEvents.END_WORLD_TICK.invoker().onEndTick(level);
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
		return !MixinConnector.isFabric || isLoaded("cloth-config2");
	}
}
