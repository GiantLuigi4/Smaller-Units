package tfc.smallerunits.utils.platform;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
		return 7;
		
//		double reach = 7;
//		if (ctx.getSender().getAttributes().hasAttribute(ForgeMod.REACH_DISTANCE.get()))
//			reach = ctx.getSender().getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		
		// TODO: reach distance attribute support
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
		// TODO:
		return null;
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
}
