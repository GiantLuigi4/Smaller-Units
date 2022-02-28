package tfc.smallerunits.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CommandBlockScreen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tfc.smallerunits.TileResizingItem;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.data.SUCapabilityManager;
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
	
	public static boolean checkFakeClientWorld(World world) {
		return world instanceof FakeClientWorld;
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
	
	public static UnitTileEntity getOwner(World world) {
		return ((FakeClientWorld) world).owner;
	}
	
	public static boolean isScreenCmdScreen() {
		return Minecraft.getInstance().currentScreen instanceof CommandBlockScreen;
	}
	
	
	public static void updateCmdScreen() {
		// code inspection moment
		assert Minecraft.getInstance().currentScreen != null;
		((CommandBlockScreen) Minecraft.getInstance().currentScreen).updateGui();
	}
	
	public static void openSign(BlockPos realPos, BlockPos pos) {
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(ClientUtils.getWorld(), realPos);
		if (tileEntity == null) return;
		
		TileEntity tileentity = tileEntity.getTileEntity(pos);
		if (!(tileentity instanceof SignTileEntity)) {
			tileentity = new SignTileEntity();
			((TileEntity) tileentity).setWorldAndPos(tileEntity.getFakeWorld(), pos);
		}
		
		// networking should never happen while player is null
		assert Minecraft.getInstance().player != null;
		Minecraft.getInstance().player.openSignEditor((SignTileEntity) tileentity);
	}
}
