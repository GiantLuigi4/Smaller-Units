package tfc.smallerunits.plat.mixin.compat.distant_horizons;

import com.seibel.distanthorizons.fabric.FabricClientProxy;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;

/**
 * When something happens to a small world, distant horizons should not acknowledge it
 * that is what this mixin does
 */
@Mixin(value = FabricClientProxy.class, remap = false)
public class FabricClientProxyMixin {
	@Inject(at = @At("HEAD"), method = "lambda$registerEvents$1", cancellable = true)
	private static void preLoadWorld(ClientLevel level, LevelChunk chunk, CallbackInfo ci) {
		if (level instanceof ITickerLevel) {
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "lambda$registerEvents$4", cancellable = true)
	private static void preUnloadWorld(ClientLevel level, LevelChunk chunk, CallbackInfo ci) {
		if (level instanceof ITickerLevel) {
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "lambda$registerEvents$2", cancellable = true)
	private static void preAttackBlock(Player player, Level level, InteractionHand interactionHand, BlockPos blockPos, Direction direction, CallbackInfoReturnable<InteractionResult> cir) {
		if (level instanceof ITickerLevel) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
	
	@Inject(at = @At("HEAD"), method = "lambda$registerEvents$3", cancellable = true)
	private static void preUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
		if (level instanceof ITickerLevel) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
