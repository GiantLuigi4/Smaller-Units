package tfc.smallerunits.mixin.core.gui.client.expansion;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tfc.smallerunits.utils.asm.AssortedQol;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {
	@Shadow protected HitResult block;
	
	@Inject(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
//	public void postGetBlock(CallbackInfoReturnable<List<String>> cir, long i, long j, long k, long l, List<String> list, BlockPos blockpos, BlockState blockstate, UnmodifiableIterator var12, Map.Entry var13) {
	public void postGetBlock(CallbackInfoReturnable<List<String>> cir, long i, long j, long k, long l, List list, BlockPos blockpos, BlockState blockstate) {
		AssortedQol.handleBlockInfo(block, cir, list);
	}
}
