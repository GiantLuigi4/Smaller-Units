package tfc.smallerunits.mixins;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.helpers.ContainerMixinHelper;
import tfc.smallerunits.utils.accessor.IAmContainer;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

@Mixin(Container.class)
public class ContainerMixin implements IAmContainer {
	@Unique
	public boolean canCloseNaturally = true;
	
	@Inject(at = @At("HEAD"), method = "isWithinUsableDistance", cancellable = true)
	private static void preCheckUsableDistance(IWorldPosCallable p_216963_0_, PlayerEntity p_216963_1_, Block p_216963_2_, CallbackInfoReturnable<Boolean> cir) {
		World world;
		BlockPos pos;
		{
			World[] wld = new World[1];
			BlockPos[] pose = new BlockPos[1];
			p_216963_0_.consume((world1, pos1) -> {
				wld[0] = world1;
				pose[0] = pos1;
			});
			world = wld[0];
			pos = pose[0];
		}
		if (world instanceof FakeServerWorld) {
			if (!world.getBlockState(pos).isIn(p_216963_2_)) cir.setReturnValue(false);
			// TODO: reach check
			cir.setReturnValue(true);
		} else if (world.isRemote && ClientUtils.checkFakeClientWorld(world)) {
			if (!world.getBlockState(pos).isIn(p_216963_2_)) cir.setReturnValue(false);
			cir.setReturnValue(ContainerMixinHelper.checkReach(p_216963_1_, pos));
		}
	}
	
	public void SmallerUnits_setCanCloseNaturally(boolean canCloseNaturally) {
		this.canCloseNaturally = canCloseNaturally;
	}
	
	public boolean SmallerUnits_canCloseNaturally() {
		return canCloseNaturally;
	}
}
