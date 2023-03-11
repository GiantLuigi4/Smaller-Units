package tfc.smallerunits.mixin.quality.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.api.PositionUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.platform.PlatformUtils;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ReachEntityAttributes.class)
public class ReachEntityAttributesMixin {
	@Inject(at = @At("RETURN"), method = "getPlayersWithinReach(Ljava/util/function/Predicate;Lnet/minecraft/world/level/Level;IIID)Ljava/util/List;")
	private static void postGetPlayersInReach(
			Predicate<Player> viewerPredicate, Level world,
			int x, int y, int z, double baseReachDistance,
			CallbackInfoReturnable<List<Player>> cir
	) {
		if (world instanceof ITickerLevel level) {
			Vec3 vec = PositionUtils.getParentVec(new BlockPos(x, y, z), level);
			Level parent = level.getParent();
			List<Player> players = cir.getReturnValue();
			for (Player player : parent.players()) {
				if (viewerPredicate.test(player)) {
					double d = PlatformUtils.getReach(player);
					if (vec.closerThan(player.position(), d)) {
						players.add(player);
					}
				}
			}
		}
	}
}
