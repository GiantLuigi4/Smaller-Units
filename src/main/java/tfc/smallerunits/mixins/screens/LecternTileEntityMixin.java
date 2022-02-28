package tfc.smallerunits.mixins.screens;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.helpers.ClientUtils;
import tfc.smallerunits.helpers.ContainerMixinHelper;
import tfc.smallerunits.utils.accessor.IHaveOwner;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

@Mixin(LecternTileEntity.class)
public class LecternTileEntityMixin {
	@Shadow
	@Final
	private IInventory inventory;
	
	@Inject(at = @At("TAIL"), method = "<init>()V")
	public void postInit(CallbackInfo ci) {
		((IHaveOwner) inventory).SmallerUnits_setOwner((TileEntity) (Object) this);
	}
	
	@Mixin(targets = "net.minecraft.tileentity.LecternTileEntity$1")
	public static class LecternInventoryMixin implements IHaveOwner {
		@Unique
		TileEntity te;
		
		@Override
		public TileEntity SmallerUnits_getOwner() {
			return te;
		}
		
		@Override
		public void SmallerUnits_setOwner(TileEntity te) {
			this.te = te;
		}
		
		@Inject(at = @At("HEAD"), method = "Lnet/minecraft/tileentity/LecternTileEntity$1;isUsableByPlayer(Lnet/minecraft/entity/player/PlayerEntity;)Z", cancellable = true)
		public void preCheckUsability(PlayerEntity p_70300_1_, CallbackInfoReturnable<Boolean> cir) {
			LecternTileEntity te;
			{
				TileEntity tile = SmallerUnits_getOwner();
				if (!(tile instanceof LecternTileEntity)) return;
				te = (LecternTileEntity) tile;
			}
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
				if (world.getTileEntity(pos) != te) {
					cir.setReturnValue(false); // close if the tile entity does not exist
					return;
				}
				if (!te.hasBook()) {
					cir.setReturnValue(false); // close if no book is present
					return;
				}
				cir.setReturnValue(ContainerMixinHelper.checkReach(p_70300_1_, pos));
			}
		}
	}
}
