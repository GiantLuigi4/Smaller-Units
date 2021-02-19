package com.tfc.smallerunits.client;

import com.tfc.smallerunits.block.UnitTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;

public class TickHandler {
	private static boolean isTicking = false;
	private static int num = 0;
	
	public static void onTick(TickEvent.ClientTickEvent event) {
		World world = Minecraft.getInstance().world;
		if (!isTicking && num >= 3) {
			isTicking = true;
			if (world != null && world.isRemote && event.phase.equals(TickEvent.Phase.START) && event.side.isClient()) {
				for (TileEntity tileEntity : world.tickableTileEntities) {
					if (tileEntity instanceof UnitTileEntity) {
//						long start = new Date().getTime();
//						((UnitTileEntity) tileEntity).world.tick(() -> Math.abs(new Date().getTime() - start) <= 10);
					}
				}
				num = 0;
			}
			isTicking = false;
		}
		num++;
	}
}
