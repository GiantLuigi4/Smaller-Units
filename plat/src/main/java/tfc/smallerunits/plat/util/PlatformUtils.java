package tfc.smallerunits.plat.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import tfc.smallerunits.plat.itf.CapabilityLike;
import tfc.smallerunits.plat.itf.ICullableBE;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class PlatformUtils {
	public static boolean isDevEnv() {
		throw new RuntimeException();
	}
	
	public static boolean isLoaded(String mod) {
		throw new RuntimeException();
	}
	
	public static boolean isClient() {
		throw new RuntimeException();
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
		throw new RuntimeException();
	}
	
	public static double getStepHeight(LocalPlayer player) {
		return player.maxUpStep;
	}

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
	
	// config
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
	
	// entity
	public static PortalInfo createPortalInfo(Entity pEntity, Level lvl) {
		throw new RuntimeException();
	}
	
	public static Entity migrateEntity(Entity pEntity, ServerLevel serverLevel, int upb, Level lvl) {
		throw new RuntimeException();
	}
	
	// block entity
	public static void beLoaded(BlockEntity pBlockEntity, Level level) {
//		if (level.isClientSide)
//			IHateTheDistCleaner.loadBe(pBlockEntity, level);
//		else
//			ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(pBlockEntity, (ServerLevel) level);
	}
	
	public static void dataPacket(BlockEntity be, CompoundTag tag) {
		throw new RuntimeException("");
	}
	
	public static <T extends BlockEntity> AABB getRenderBox(T pBlockEntity) {
		return ICullableBE.getCullingBB(pBlockEntity);
	}
	
	// events
	public static void preTick(ServerLevel level, BooleanSupplier pHasTimeLeft) {
		throw new RuntimeException();
	}
	
	public static void postTick(ServerLevel level, BooleanSupplier pHasTimeLeft) {
		throw new RuntimeException();
	}
	
	public static void loadLevel(ServerLevel serverLevel) {
		throw new RuntimeException();
	}
	
	public static void unloadLevel(Level level) {
		throw new RuntimeException();
	}
	
	public static void chunkLoaded(LevelChunk bvci) {
		throw new RuntimeException();
	}
	
	// reach
	public static double getReach(LivingEntity entity, double reach) {
		throw new RuntimeException();
	}
	
	public static double getReach(LivingEntity entity) {
		return getReach(entity, 7);
	}
	
	public static AttributeInstance getReachAttrib(LivingEntity livingEntity) {
		throw new RuntimeException();
	}
	
	// tabs
	public static CreativeModeTab tab(String name, Supplier<Item> icon) {
		throw new RuntimeException();
	}
	
	public static void customPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket, Object context, PacketListener listener) {
		throw new RuntimeException();
	}
	
	public static void injectCrashReport(String smallerUnits, Supplier<String> o) {
		throw new RuntimeException();
	}
	
	public static void updateModelData(ClientLevel level, BlockEntity be) {
		throw new RuntimeException();
	}
	
	public static int getLightEmission(BlockState state, BlockGetter level, BlockPos pPos) {
		throw new RuntimeException();
	}
	
	public static boolean collisionExtendsVertically(BlockState blockstate, Level lvl, BlockPos blockpos1, Entity entity) {
		throw new RuntimeException();
	}
	
	public static void startupWarning(String msg) {
		throw new RuntimeException();
	}
	
	public static CapabilityLike getSuCap(LevelChunk levelChunk) {
		throw new RuntimeException();
	}
	
	public static CompoundTag chunkCapNbt(LevelChunk basicVerticalChunk) {
		throw new RuntimeException();
	}
	
	public static void readChunkCapNbt(LevelChunk shell, CompoundTag capabilities) {
		throw new RuntimeException();
	}
	
	public static Tag serializeEntity(Entity ent) {
		CompoundTag tag = new CompoundTag();
		ent.save(tag);
		return tag;
	}
	
	public static boolean canRenderIn(BakedModel model, BlockState block, Random randomSource, Object modelData, RenderType chunkBufferLayer) {
		throw new RuntimeException();
	}
	
	public static void tesselate(BlockRenderDispatcher dispatcher, BlockAndTintGetter wld, BakedModel blockModel, BlockState block, BlockPos offsetPos, PoseStack stk, VertexConsumer consumer, boolean b, Random randomSource, int i, int i1, Object modelData, RenderType chunkBufferLayer) {
		dispatcher.getModelRenderer().tesselateBlock(
				wld, dispatcher.getBlockModel(block),
				block, offsetPos, stk,
				consumer, true,
				randomSource,
				0, 0
		);
	}
	
	public static boolean isFabric() {
		throw new RuntimeException();
	}
}
