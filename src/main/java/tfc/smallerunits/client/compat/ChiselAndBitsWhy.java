package tfc.smallerunits.client.compat;

import mod.chiselsandbits.forge.platform.client.model.data.ForgeBlockModelDataPlatformDelegate;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.entity.block.IBlockEntityWithModelData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fml.ModList;

public class ChiselAndBitsWhy {
	public static IModelData maybeGetModelData(BlockEntity blockEntity) {
		if (!ModList.get().isLoaded("chiselsandbits"))
			return null;
		if (blockEntity instanceof IBlockEntityWithModelData) {
			// I really should not need this
			IBlockModelData data = ((IBlockEntityWithModelData) blockEntity).getBlockModelData();
			if (data instanceof ForgeBlockModelDataPlatformDelegate platformDelegate)
				return platformDelegate.getDelegate();
			else return null;
		}
		return null;
	}
}
