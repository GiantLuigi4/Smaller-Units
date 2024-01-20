package tfc.smallerunits.plat.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.plat.CapabilityProvider;
import tfc.smallerunits.plat.itf.CapabilityLike;
import tfc.smallerunits.plat.itf.IMayManageModelData;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.scale.ResizingUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

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
	
	public static boolean shouldCaptureBlockSnapshots(Level level) {
		return level.captureBlockSnapshots;
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
	
	// entity
	public static PortalInfo createPortalInfo(Entity pEntity, Level a) {
		ITickerLevel lvl = (ITickerLevel) a;
		
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
	
	public static Entity migrateEntity(Entity pEntity, ServerLevel serverLevel, int upb, Level lvl) {
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
				BlockPos bp = ((ITickerLevel) serverLevel).getRegion().pos.toBlockPos();
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
	
	// block entity
	public static void beLoaded(BlockEntity pBlockEntity, Level level) {
	}
	
	public static void dataPacket(BlockEntity be, CompoundTag tag) {
		be.onDataPacket(null, ClientboundBlockEntityDataPacket.create(be, (e) -> tag));
	}
	
	public static <T extends BlockEntity> AABB getRenderBox(T pBlockEntity) {
		return pBlockEntity.getRenderBoundingBox();
	}
	
	// events
	public static void preTick(ServerLevel level, BooleanSupplier pHasTimeLeft) {
		MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.START, level, pHasTimeLeft));
	}
	
	public static void postTick(ServerLevel level, BooleanSupplier pHasTimeLeft) {
		MinecraftForge.EVENT_BUS.post(new TickEvent.WorldTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, pHasTimeLeft));
	}
	
	public static void loadLevel(ServerLevel serverLevel) {
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(serverLevel));
	}
	
	public static void unloadLevel(Level level) {
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(level));
	}
	
	public static void chunkLoaded(LevelChunk bvci) {
		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(bvci));
	}
	
	// reach
	public static double getReach(LivingEntity entity, double reach) {
		if (entity.getAttributes().hasAttribute(ForgeMod.REACH_DISTANCE.get()))
			reach = entity.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		return reach;
	}
	
	public static double getReach(LivingEntity entity) {
		return getReach(entity, 7);
	}
	
	public static AttributeInstance getReachAttrib(LivingEntity livingEntity) {
		return livingEntity.getAttribute(ForgeMod.REACH_DISTANCE.get());
	}
	
	// tabs
	public static CreativeModeTab tab(String name, Supplier<Item> icon) {
		return new CreativeModeTab(name) {
			@NotNull
			@Override
			public ItemStack makeIcon() {
				return new ItemStack(icon.get());
			}
		};
	}
	
	public static void customPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket, Object context, PacketListener listener) {
		if (!net.minecraftforge.network.NetworkHooks.onCustomPayload(clientboundCustomPayloadPacket, ((tfc.smallerunits.networking.hackery.NetworkContext) context).connection)) {
			clientboundCustomPayloadPacket.handle((ClientGamePacketListener) listener);
		}
	}
	
	public static void injectCrashReport(String smallerUnits, Supplier<String> o) {
		CrashReportCallables.registerCrashCallable("Smaller Units", o);
	}
	
	public static void updateModelData(ClientLevel level, BlockEntity be) {
		Objects.requireNonNull(((IMayManageModelData) level).getModelDataManager()).requestModelDataRefresh(be);
	}
	
	public static int getLightEmission(BlockState state, BlockGetter level, BlockPos pPos) {
		return state.getLightEmission(level, pPos);
	}
	
	public static boolean collisionExtendsVertically(BlockState blockstate, Level lvl, BlockPos blockpos1, Entity entity) {
		return blockstate.collisionExtendsVertically(lvl, blockpos1, entity);
	}
	
	public static void startupWarning(String msg) {
		ModLoader.get().addWarning(new ModLoadingWarning(
				ModLoadingContext.get().getActiveContainer().getModInfo(),
				ModLoadingStage.CONSTRUCT,
				// TODO: translation text component
				msg
		));
	}
	
	public static CapabilityLike getSuCap(LevelChunk levelChunk) {
		return levelChunk.getCapability(CapabilityProvider.SU_CAPABILITY_TOKEN).orElse(null);
	}
	
	public static CompoundTag chunkCapNbt(LevelChunk basicVerticalChunk) {
		return basicVerticalChunk.writeCapsToNBT();
	}
	
	public static void readChunkCapNbt(LevelChunk shell, CompoundTag capabilities) {
		shell.readCapsFromNBT(capabilities);
	}
	
	public static Tag serializeEntity(Entity ent) {
		return ((Entity) ent).serializeNBT();
	}
	
	public static boolean canRenderIn(BakedModel model, BlockState block, Random randomSource, Object modelData, RenderType chunkBufferLayer) {
//		return (model.getRenderTypes(block, randomSource, (IModelData) modelData).contains(chunkBufferLayer));
		return ItemBlockRenderTypes.canRenderInLayer(block, chunkBufferLayer);
	}
	
	public static void tesselate(BlockRenderDispatcher dispatcher, BlockAndTintGetter wld, BakedModel blockModel, BlockState block, BlockPos offsetPos, PoseStack stk, VertexConsumer consumer, boolean b, Random randomSource, int i, int i1, Object modelData, RenderType chunkBufferLayer) {
		dispatcher.getModelRenderer().tesselateBlock(
				wld, dispatcher.getBlockModel(block),
				block, offsetPos, stk,
				consumer, true,
				randomSource,
				0, 0,
				(IModelData) modelData
		);
	}
	
	public static boolean isFabric() {
		return false;
	}
}
