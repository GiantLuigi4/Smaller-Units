package tfc.smallerunits;

import io.netty.channel.ChannelPipeline;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.crafting.CraftingRegistry;
import tfc.smallerunits.data.capability.AttachmentRegistry;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.SUNetworkRegistry;
import tfc.smallerunits.networking.hackery.InfoRegistry;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.networking.sync.SyncPacketS2C;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.utils.config.ServerConfig;
import tfc.smallerunits.utils.platform.PlatformUtils;
import tfc.smallerunits.utils.scale.PehkuiSupport;

//#if FABRIC==1
//#else
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//#endif

// The value here should match an entry in the META-INF/mods.toml file
//@formatter:off
//#if FORGE == 1
@Mod("smallerunits")
public class SmallerUnits {
//#else
//$$public class SmallerUnits implements net.fabricmc.api.ModInitializer { @Override public void onInitialize() {}
//#endif
//@formatter:on
	
	public static float tesselScale = 0;
	private static boolean isVivecraftPresent;
	private static boolean isVFEPresent;
	private static boolean isOFPresent;
	private static final boolean isImmPrtlPresent = PlatformUtils.isLoaded("imm_ptl_core");
	
	public SmallerUnits() {
		//#if FORGE==1
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		//#endif
		
		SUNetworkRegistry.init();
		/* registries */
		Registry.BLOCK_REGISTER.register();
		Registry.ITEM_REGISTER.register();
		CraftingRegistry.RECIPES.register();
		/* mod loading events */
		//#if FABRIC==1
		//$$if (PlatformUtils.isClient()) ClientLifecycleEvents.CLIENT_STARTED.register((a) -> setup());
		//$$else ServerLifecycleEvents.SERVER_STARTED.register((a) -> setupCfg());
		//#else
		modBus.addListener(AttachmentRegistry::onRegisterCapabilities);
		modBus.addListener(this::setup);
		//#endif
		/* in game events */
		
		//#if FABRIC==1
		//$$ChunkSyncCallback.EVENT.register(SUCapabilityManager::onChunkWatch);
		//$$ServerPlayConnectionEvents.INIT.register((handler, server) -> {
		//$$	setupConnectionButchery(handler.player, handler.connection, handler.connection.channel.pipeline());
		//$$});
		//$$
		//$$ServerChunkEvents.CHUNK_LOAD.register(SmallerUnits::onChunkLoaded);
		//$$ServerChunkEvents.CHUNK_UNLOAD.register(SmallerUnits::onChunkUnloaded);
		//#else
		forgeBus.addGenericListener(LevelChunk.class, AttachmentRegistry::onAttachCapabilities);
		
		forgeBus.addListener(SmallerUnits::onChunkLoaded);
		forgeBus.addListener(SmallerUnits::onChunkUnloaded);
		//#endif
		
		PlatformUtils.delayConfigInit(() -> {
			//noinspection Convert2MethodRef
			ServerConfig.init();
		});
		
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
		
		if (PlatformUtils.isClient()) {
			//#if FABRIC==1
			//$$ClientTickEvents.START_CLIENT_TICK.register((i) -> {
			//$$	SyncPacketS2C.tick();
			//$$	NetCtx.tick();
			//$$});
			//$$SpriteRegistryCallbackHolder.EVENT_GLOBAL.register((listener, whatdoyouwantfabric) -> {
			//$$	if (listener.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			//$$		whatdoyouwantfabric.register(new ResourceLocation("smallerunits:block/white_pixel"));
			//$$	}
			//$$});
			//$$SUItemRenderProperties.init();
			//#else
			forgeBus.addListener(SyncPacketS2C::tick);
			modBus.addListener(SmallerUnits::onTextureStitch);
			//#endif
		}
		
		isVivecraftPresent = PlatformUtils.isLoaded("vivecraft");
		isVFEPresent = PlatformUtils.isLoaded("vivecraftforgeextensions");
		
		//#if FORGE==1
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
		//#endif
	}
	
	private void setupCfg() {
		PlatformUtils.delayConfigInit(null);
	}
	
	private static void onChunkLoaded(
			//#if FABRIC==1
			//$$ServerLevel lvl, LevelChunk lvlChk
			//#else
			ChunkEvent.Load loadEvent
			//#endif
	) {
		//#if FORGE==1
		if (!(loadEvent.getLevel() instanceof ServerLevel)) return;
		if (!(loadEvent.getChunk() instanceof LevelChunk)) return;
		//noinspection PatternVariableCanBeUsed
		ServerLevel lvl = (ServerLevel) loadEvent.getLevel();
		//noinspection PatternVariableCanBeUsed
		LevelChunk lvlChk = (LevelChunk) loadEvent.getChunk();
		//#endif
		
		if (lvl.getChunkSource().chunkMap instanceof RegionalAttachments attachments) {
			SUCapabilityManager.onChunkLoad(lvlChk);
			
			ChunkAccess access = lvlChk;
			int min = access.getMinBuildHeight();
			int max = access.getMaxBuildHeight();
			ChunkPos pos = access.getPos();
			for (int y = min; y < max; y += 16)
				attachments.SU$findChunk(y, pos, (rp, r) -> r.addRef(rp));
		}
	}
	
	private static void onChunkUnloaded(
			//#if FABRIC==1
			//$$ServerLevel lvl, LevelChunk lvlChk
			//#else
			ChunkEvent.Unload loadEvent
			//#endif
	) {
		//#if FORGE==1
		if (!(loadEvent.getLevel() instanceof ServerLevel)) return;
		if (!(loadEvent.getChunk() instanceof LevelChunk)) return;
		//noinspection PatternVariableCanBeUsed
		ServerLevel lvl = (ServerLevel) loadEvent.getLevel();
		//noinspection PatternVariableCanBeUsed
		LevelChunk lvlChk = (LevelChunk) loadEvent.getChunk();
		//#endif
		
		ISUCapability capability = SUCapabilityManager.getCapability(lvlChk);
		for (UnitSpace unit : capability.getUnits()) {
			for (BasicVerticalChunk chunk : unit.getChunks()) {
				chunk.maybeUnload();
			}
		}
		
		if (lvl.getChunkSource().chunkMap instanceof RegionalAttachments attachments) {
			int min = lvlChk.getMinBuildHeight();
			int max = lvlChk.getMaxBuildHeight();
			ChunkPos pos = lvlChk.getPos();
			for (int y = min; y < max; y += 16)
				attachments.SU$findChunk(y, pos, (rp, r) -> {
					if (r.subtractRef(rp) <= 0) {
						Region region = attachments.SU$getRegionMap().remove(rp);
						if (region != null) region.close();
					}
				});
		}
	}
	
	private void setup(
			//#if FORGE==1
			final FMLCommonSetupEvent event
			//#endif
	) {
		if (PlatformUtils.isLoaded("pehkui")) PehkuiSupport.setup();
		setupCfg();
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
	
	public static boolean isImmersivePortalsPresent() {
		return isImmPrtlPresent;
	}
	
	//#if FORGE==1
	private static void onTextureStitch(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			event.addSprite(new ResourceLocation("smallerunits:block/white_pixel"));
		}
	}
	//#endif
}
