package tfc.smallerunits.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/* this whole class should be unnecessary, however forge says that clean code is a concept dreamed of by idiots */
/* really, what it is, is that forge wants to enforce that you don't reference client only code from common code */
/* however, you can reference client code from common code and still have safe code */
/* I make sure that my code is safe, but forge sees that I reference client code, and thus it throws an error with no obvious reason */
/* it is stupid and I hate it because it causes this class to have to exist */
public class IHateTheDistCleaner {
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
}
