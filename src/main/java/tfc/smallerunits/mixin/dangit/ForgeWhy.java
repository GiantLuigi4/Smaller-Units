package tfc.smallerunits.mixin.dangit;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.ModelDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.simulation.world.ITickerWorld;

@Mixin(value = ModelDataManager.class, remap = false)
public class ForgeWhy {
	@Inject(at = @At("HEAD"), method = "requestModelDataRefresh", cancellable = true)
	private static void stopForgeFromCrashingTheGame(BlockEntity te, CallbackInfo ci) {
		// TODO: make a better model data manager
		if (te.getLevel() instanceof ITickerWorld) ci.cancel();
	}
}
