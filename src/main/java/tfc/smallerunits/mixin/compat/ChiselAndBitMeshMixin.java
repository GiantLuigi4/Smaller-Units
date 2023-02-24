package tfc.smallerunits.mixin.compat;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.client.model.data.ChiseledBlockModelDataExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.simulation.level.client.FakeClientLevel;

/*
@Mixin(value = ChiseledBlockModelDataExecutor.class, remap = false)
public class ChiselAndBitMeshMixin {
	@Inject(at = @At("HEAD"), method = "lambda$updateModelDataCore$5", cancellable = true)
	private static void preUpdate(ChiseledBlockEntity tileEntity, CallbackInfo ci) {
		if (tileEntity.getLevel() instanceof FakeClientLevel) {
			((FakeClientLevel) tileEntity.getLevel()).modelDataManager.requestModelDataRefresh(tileEntity);
			tileEntity.getLevel().sendBlockUpdated(
					tileEntity.getBlockPos(),
					tileEntity.getBlockState(),
					tileEntity.getBlockState(),
					8
			);
			ci.cancel();
		}
	}
} */
