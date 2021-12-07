package tfc.smallerunits.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import tfc.smallerunits.TileResizingItem;
import tfc.smallerunits.utils.world.client.FakeClientWorld;

public class ClientUtils {
	public static PlayerEntity getPlayer() {
		return Minecraft.getInstance().player;
	}
	
	public static void setWorld(World world) {
		if (!(world instanceof ClientWorld)) return;
		Minecraft.getInstance().world = (ClientWorld) world;
	}
	
	public static World getWorld() {
		return Minecraft.getInstance().world;
	}
	
	public static boolean checkClientWorld(World world) {
		return world instanceof ClientWorld;
	}
	
	public static void unloadWorld(World fakeWorld) {
		if (fakeWorld instanceof FakeClientWorld) ((FakeClientWorld) fakeWorld).unload();
	}
	
	public static boolean isHammerHeld() {
		PlayerEntity e = getPlayer();
		if (e == null) return false;
		if (e.getHeldItem(Hand.MAIN_HAND).getItem() instanceof TileResizingItem) return true;
		else return e.getHeldItem(Hand.OFF_HAND).getItem() instanceof TileResizingItem;
	}
}
