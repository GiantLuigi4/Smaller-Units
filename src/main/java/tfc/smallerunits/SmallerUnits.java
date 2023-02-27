package tfc.smallerunits;

import dev.onyxstudios.cca.api.v3.chunk.ChunkSyncCallback;
import io.netty.channel.ChannelPipeline;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.client.texture.SpriteRegistryCallbackHolder;
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

// The value here should match an entry in the META-INF/mods.toml file
public class SmallerUnits implements ModInitializer {
	public static float tesselScale = 1;
	private static boolean isVivecraftPresent;
	private static boolean isVFEPresent;
	private static boolean isOFPresent;
	
	@Override
	public void onInitialize() {
	}
	
	public SmallerUnits() {
		SUNetworkRegistry.init();
		/* registries */
		Registry.BLOCK_REGISTER.register();
		Registry.ITEM_REGISTER.register();
		CraftingRegistry.RECIPES.register();
		/* mod loading events */
		ClientLifecycleEvents.CLIENT_STARTED.register((a) -> setup());
		/* in game events */
		ChunkSyncCallback.EVENT.register(SUCapabilityManager::onChunkWatchEvent);
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			setupConnectionButchery(handler.player, handler.connection, handler.connection.channel.pipeline());
		});
		
		ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoaded);
		ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnloaded);
		
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
		
		if (PlatformUtils.isClient()) {
			ClientTickEvents.START_CLIENT_TICK.register((i) -> {
				SyncPacketS2C.tick();
			});
			SpriteRegistryCallbackHolder.EVENT_GLOBAL.register((listener, whatdoyouwantfabric) -> {
				if (listener.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
					whatdoyouwantfabric.register(new ResourceLocation("smallerunits:block/white_pixel"));
				}
			});
			SUItemRenderProperties.init();
		}
		
		isVivecraftPresent = PlatformUtils.isLoaded("vivecraft");
		isVFEPresent = PlatformUtils.isLoaded("vivecraftforgeextensions");
//		try {
//			Class<?> clazz = Class.forName("net.optifine.Config");
//			if (clazz != null) {
//				ModLoader.get().addWarning(new ModLoadingWarning(
//						ModLoadingContext.get().getActiveContainer().getModInfo(),
//						ModLoadingStage.CONSTRUCT,
//						// TODO: translation text component
//						ChatFormatting.YELLOW + "Smaller Units" + ChatFormatting.RESET + "\nSU and Optifine are " + ChatFormatting.RED + ChatFormatting.BOLD + "highly incompatible" + ChatFormatting.RESET + " with eachother."
//				));
//				isOFPresent = true;
//			}
//		} catch (Throwable ignored) {
//		}
	}
	
	private void onChunkLoaded(ServerLevel world, LevelChunk chunk) {
		if (world.getChunkSource().chunkMap instanceof RegionalAttachments attachments) {
			ChunkAccess access = chunk;
			int min = access.getMinBuildHeight();
			int max = access.getMaxBuildHeight();
			ChunkPos pos = access.getPos();
			for (int y = min; y < max; y += 16)
				attachments.SU$findChunk(y, pos, (rp, r) -> r.addRef(rp));
		}
	}
	
	private void onChunkUnloaded(ServerLevel lvl, LevelChunk lvlChk) {
		ISUCapability capability = SUCapabilityManager.getCapability(lvlChk);
		for (UnitSpace unit : capability.getUnits()) {
			for (BasicVerticalChunk chunk : unit.getChunks()) {
				chunk.maybeUnload();
			}
		}
		
		if (lvl.getChunkSource().chunkMap instanceof RegionalAttachments attachments) {
			ChunkAccess access = lvlChk;
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
	
	private void setup() {
		if (PlatformUtils.isLoaded("pehkui")) PehkuiSupport.setup();
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
}
