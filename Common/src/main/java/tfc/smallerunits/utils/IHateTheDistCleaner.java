package tfc.smallerunits.utils;

import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import qouteall.imm_ptl.core.ClientWorldLoader;
import tfc.smallerunits.TileResizingItem;
import tfc.smallerunits.client.access.workarounds.ParticleEngineHolder;
import tfc.smallerunits.client.render.compat.UnitParticleEngine;
import tfc.smallerunits.simulation.level.client.TickerClientLevel;

import java.util.concurrent.atomic.AtomicReference;

/* this whole class should be unnecessary, however forge says that clean code is a concept dreamed of by idiots */
/* really, what it is, is that forge wants to enforce that you don't reference client only code from common code */
/* however, you can reference client code from common code and still have safe code */
/* I make sure that my code is safe, but forge sees that I reference client code, and thus it throws an error with no obvious reason */
/* it is stupid and I hate it because it causes this class to have to exist */
public class IHateTheDistCleaner {
	public static AtomicReference<ChunkRenderDispatcher.RenderChunk> currentRenderChunk = new AtomicReference<>();

//	@OnlyIn(Dist.CLIENT)
//	public static ChunkRenderDispatcher.RenderChunk updateRenderChunk(ChunkRenderDispatcher.RenderChunk chunk) {
//		currentRenderChunk.set(chunk);
//		return chunk;
//	}
	
	public static Level getClientLevel() {
		return Minecraft.getInstance().level;
	}
	
	public static MinecraftServer getIntegratedServer() {
		return Minecraft.getInstance().getSingleplayerServer();
	}
	
	public static String getVersion() {
		return Minecraft.getInstance().getLaunchedVersion();
	}
	
	public static boolean isClientLevel(Level level) {
		return level instanceof ClientLevel;
	}
	
	public static boolean isClientPacketListener(PacketListener listener) {
		return listener instanceof ClientGamePacketListener;
	}
	
	public static Player getPlayer() {
		return Minecraft.getInstance().player;
	}
	
	public static void tickLevel(Level level) {
		((ClientLevel) level).tick(() -> true);
		((ClientLevel) level).tickEntities();
	}
	
	public static boolean isHammerHeld() {
		Player player = Minecraft.getInstance().player;
		for (ItemStack handSlot : player.getHandSlots())
			if (handSlot.getItem() instanceof TileResizingItem)
				return true;
		return false;
	}
	
	private static Camera camera;
	
	public static void updateCamera() {
		camera = Minecraft.getInstance().getEntityRenderDispatcher().camera;
	}
	
	public static Vec3 getCameraPos() {
		return camera.getPosition();
	}
	
	
	public static Vector3f getCameraLook() {
		return camera.getLookVector();
	}
	
	public static boolean isCameraPresent() {
		return camera != null;
	}
	
	public static Object adjustClient(Player pPlayer, Level spaceLevel, boolean updateParticleEngine) {
		if (pPlayer == Minecraft.getInstance().player) {
			ParticleEngine engine = null;
			Minecraft.getInstance().level = ((LocalPlayer) pPlayer).clientLevel = (ClientLevel) spaceLevel;
			
			if (updateParticleEngine) {
				engine = Minecraft.getInstance().particleEngine;
				UnitParticleEngine upe = ((TickerClientLevel) spaceLevel).getParticleEngine();
				if (upe != null)
					Minecraft.getInstance().particleEngine = upe;
			}
			return engine;
		}
		return null;
	}
	
	public static void resetClient(Player pPlayer, Level lvl, Object engine) {
		if (pPlayer.level.isClientSide) {
			if (pPlayer instanceof LocalPlayer) {
				Minecraft.getInstance().level = ((LocalPlayer) pPlayer).clientLevel = (ClientLevel) lvl;
				
				if (Minecraft.getInstance().level instanceof ParticleEngineHolder engineHolder) {
					if (engineHolder.myEngine() != null) {
						if ((Minecraft.getInstance().particleEngine = engineHolder.myEngine()) != null)
							return;
					}
				}
				if (engine != null) Minecraft.getInstance().particleEngine = (ParticleEngine) engine;
			}
		}
	}
	
	public static Object getScreen() {
		return Minecraft.getInstance().screen;
	}
	
	public static Object getParticleEngine(Player player) {
		if (player == Minecraft.getInstance().player) {
			if (player.level instanceof ParticleEngineHolder engineHolder) {
				ParticleEngine engine = engineHolder.myEngine();
				if (engine == null) engineHolder.setParticleEngine(engine = Minecraft.getInstance().particleEngine);
				return engine;
			}
			return Minecraft.getInstance().particleEngine;
		}
		return null;
	}
	
	public static Object getMinecraft() {
		return Minecraft.getInstance();
	}
	
	public static boolean isClientPlayer(Player player) {
		return player.getUUID().equals(Minecraft.getInstance().player.getUUID());
	}
	
	public static void setClientLevel(Level clientLevel) {
		Minecraft.getInstance().level = (ClientLevel) clientLevel;
	}
	
	public static Level getOptionalIPWorld(ResourceKey<Level> lvl) {
		return ClientWorldLoader.getOptionalWorld(lvl);
	}
	
	public static ClientPacketListener getConnection() {
		ClientPacketListener con = Minecraft.getInstance().getConnection();
		if (con == null) {
			if (Minecraft.getInstance().player != null)
				con = Minecraft.getInstance().player.connection;
		}
		return con;
	}
}
