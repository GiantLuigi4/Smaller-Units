package tfc.smallerunits.utils.asm;

import net.minecraft.world.level.block.entity.BlockEntity;

public class ModCompat {
	/**
	 * mixin point; called when a small BE is added on client
	 */
	public static void onAddBE(BlockEntity be) {
	}
	
	/**
	 * mixin point; called when a small BE is removed on client
	 */
	public static void onRemoveBE(BlockEntity be) {
	}
}
