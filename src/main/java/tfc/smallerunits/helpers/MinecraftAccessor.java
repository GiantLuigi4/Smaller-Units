package tfc.smallerunits.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class MinecraftAccessor {
	public static PlayerEntity getPlayer() {
		return Minecraft.getInstance().player;
	}
	
	public static void setWorld(World world) {
		if (!(world instanceof ClientWorld)) return;
		Minecraft.getInstance().world = (ClientWorld) world;
	}
}
