package tfc.smallerunits.mixins.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.helpers.ContainerMixinHelper;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

@Mixin(LockableLootTileEntity.class)
public class LockableLootTileEntityMixin {
	@Inject(at = @At("HEAD"), method = "isUsableByPlayer", cancellable = true)
	public void preCheckUsability(PlayerEntity p_70300_1_, CallbackInfoReturnable<Boolean> cir) {
		//noinspection ConstantConditions
		TileEntity te = ((TileEntity) (Object) this);
		World world = te.getWorld();
		if (world == null) return;
		
		boolean isFakeWorld = false;
		if (world instanceof FakeServerWorld) isFakeWorld = true;
		else if (world.isRemote && ClientUtils.checkFakeClientWorld(world)) isFakeWorld = true;
		
		if (isFakeWorld) {
			Object o = ContainerMixinHelper.getOwner(world);
			if (o == null) {
				cir.setReturnValue(false);
				return;
			}
			
			BlockPos pos = te.getPos();
			//noinspection ConstantConditions
			if (world.getTileEntity(pos) != te) {
				cir.setReturnValue(false);
				return;
			}
			cir.setReturnValue(ContainerMixinHelper.checkReach(p_70300_1_, pos));
		}
	}
}
