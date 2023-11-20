package tfc.smallerunits;

import io.netty.channel.ChannelPipeline;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.crafting.CraftingRegistry;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.InfoRegistry;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.networking.sync.SyncPacketS2C;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.utils.config.ClientConfig;
import tfc.smallerunits.utils.config.CommonConfig;
import tfc.smallerunits.utils.config.ServerConfig;
import tfc.smallerunits.utils.scale.PehkuiSupport;

import java.util.ArrayDeque;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("smallerunits")
public class SmallerUnits {
	public static float tesselScale = 0;
	private static boolean isVivecraftPresent;
	private static boolean isVFEPresent;
	private static boolean isOFPresent;
	private static final boolean isImmPrtlPresent = ModList.get().isLoaded("imm_ptl_core");
	
	public SmallerUnits() {
		SUNetworkRegistry.init();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		/* registries */
		Registry.BLOCK_REGISTER.register(modBus);
		Registry.ITEM_REGISTER.register(modBus);
		CraftingRegistry.RECIPES.register(modBus);
		/* mod loading events */
		modBus.addListener(SUCapabilityManager::onRegisterCapabilities);
		modBus.addListener(this::setup);
		/* in game events */
		if (FMLEnvironment.dist.isClient()) forgeBus.addListener(SyncPacketS2C::tick);
//		forgeBus.addListener(SUCapabilityManager::onChunkWatchEvent);
		forgeBus.addListener(this::connect1);
		forgeBus.addGenericListener(LevelChunk.class, SUCapabilityManager::onAttachCapabilities);
		
		forgeBus.addListener(SmallerUnits::onChunkLoaded);
		forgeBus.addListener(SmallerUnits::onChunkUnloaded);
		
		if (FMLEnvironment.dist.isClient()) {
			ClientConfig.init();
//			ClientCompatConfig.init();
		}
		CommonConfig.init();
		ServerConfig.init();

		InfoRegistry.register("su:world_redir", () -> {
			if (NetworkingHacks.unitPos.get() == null) return null;
			CompoundTag tg = new CompoundTag();
//			tg.putInt("x", NetworkingHacks.unitPos.get().getX());
//			tg.putInt("y", NetworkingHacks.unitPos.get().getY());
//			tg.putInt("z", NetworkingHacks.unitPos.get().getZ());
			NetworkingHacks.unitPos.get().write(tg);
			return tg;
		}, (tag, ctx) -> {
			CompoundTag tg = (CompoundTag) tag;
//			BlockPos pos = new BlockPos(tg.getInt("x"), tg.getInt("y"), tg.getInt("z"));
			NetworkingHacks.LevelDescriptor pos = NetworkingHacks.LevelDescriptor.read(tg);
			NetworkingHacks.setPosFor(ctx.pkt, pos);
			return null;
		}, (obj, ctx) -> {
		});
		
		if (FMLEnvironment.dist.isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(SmallerUnits::onTextureStitch);
		}
		
		isVivecraftPresent = ModList.get().isLoaded("vivecraft");
		isVFEPresent = ModList.get().isLoaded("vivecraftforgeextensions");
		try {
			Class<?> clazz = Class.forName("net.optifine.Config");
			if (clazz != null) {
				ModLoader.get().addWarning(new ModLoadingWarning(
						ModLoadingContext.get().getActiveContainer().getModInfo(),
						ModLoadingStage.CONSTRUCT,
						// TODO: translation text component
						ChatFormatting.YELLOW + "Smaller Units" + ChatFormatting.RESET + "\nSU and Optifine are " + ChatFormatting.RED + ChatFormatting.BOLD + "highly incompatible" + ChatFormatting.RESET + " with eachother."
				));
				isOFPresent = true;
			}
		} catch (Throwable ignored) {
		}
		
		MinecraftForge.EVENT_BUS.addListener(SmallerUnits::onTick);
	}
	
	private static final ArrayDeque<Runnable> enqueued = new ArrayDeque<>();
	
	// this ended up being necessary, as without it, furnaces can end up deadlocing world loading
	private static void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (Runnable runnable : enqueued) {
				runnable.run();
			}
		}
	}
	
	private static void onChunkLoaded(ChunkEvent.Load loadEvent) {
		if (loadEvent.getLevel() instanceof ServerLevel lvl) {
			if (loadEvent.getChunk() instanceof LevelChunk lvlChk) {
				synchronized (enqueued) {
					enqueued.add(() -> SUCapabilityManager.onChunkLoad(lvlChk));
				}
			}
			
			if (lvl.getChunkSource().chunkMap instanceof RegionalAttachments attachments) {
				ChunkAccess access = loadEvent.getChunk();
				int min = access.getMinBuildHeight();
				int max = access.getMaxBuildHeight();
				ChunkPos pos = access.getPos();
				for (int y = min; y < max; y += 16)
					attachments.SU$findChunk(y, pos, (rp, r) -> r.addRef(rp));
			}
		}
	}
	
	private static void onChunkUnloaded(ChunkEvent.Unload loadEvent) {
		if (loadEvent.getLevel() instanceof ServerLevel lvl) {
			if (loadEvent.getChunk() instanceof LevelChunk lvlChk) {
				ISUCapability capability = SUCapabilityManager.getCapability(lvlChk);
				for (UnitSpace unit : capability.getUnits()) {
					for (BasicVerticalChunk chunk : unit.getChunks()) {
						chunk.maybeUnload();
					}
				}
			}
			
			if (lvl.getChunkSource().chunkMap instanceof RegionalAttachments attachments) {
				ChunkAccess access = loadEvent.getChunk();
				int min = access.getMinBuildHeight();
				int max = access.getMaxBuildHeight();
				ChunkPos pos = access.getPos();
				for (int y = min; y < max; y += 16)
					attachments.SU$findChunk(y, pos, (rp, r) -> {
						if (r.subtractRef(rp) <= 0) {
							Region region = attachments.SU$getRegionMap().remove(rp);
							if (region != null) region.close();
						}
					});
			}
		}
	}
	
	public static boolean isImmersivePortalsPresent() {
		return isImmPrtlPresent;
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		if (ModList.get().isLoaded("pehkui")) PehkuiSupport.setup();
	}
	
	public static void setupConnectionButchery(Player player, Connection connection, ChannelPipeline pipeline) {
//		if (!pipeline.toMap().containsKey(SmallerUnits.class.getName() + ":writer")) {
//			pipeline.addFirst(SmallerUnits.class.getName() + ":writer", new PacketWriter(player, connection));
//			pipeline.addFirst(SmallerUnits.class.getName() + ":reader", new PacketReader(player, connection));
//		}
	}

//	public void connect0(NetworkEvent.ServerCustomPayloadLoginEvent event) {
//		ChannelPipeline pipeline = event.getSource().get().getSender().connection.connection.channel().pipeline();
//		addListeners(pipeline);
//	}
	
	public static boolean isVivecraftPresent() {
		return isVivecraftPresent;
	}
	
	public static boolean isVFEPresent() {
		return isVFEPresent;
	}
	
	public static boolean isIsOFPresent() {
		return isOFPresent;
	}
	
	public void connect1(PlayerEvent.PlayerLoggedInEvent event) {
		Connection connection;
		Player player;
		if (event.getEntity() instanceof ServerPlayer) {
			connection = ((ServerPlayer) event.getEntity()).connection.connection;
			player = event.getEntity();
		} else {
			return;
		}
		setupConnectionButchery(player, connection, connection.channel().pipeline());
	}
	
	private static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			event.addSprite(new ResourceLocation("smallerunits:block/white_pixel"));
		}
	}
}
