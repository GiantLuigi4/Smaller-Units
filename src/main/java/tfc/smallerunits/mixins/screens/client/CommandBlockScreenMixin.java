package tfc.smallerunits.mixins.screens.client;

import net.minecraft.client.gui.screen.CommandBlockScreen;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.networking.screens.CUpdateLittleCommandBlockPacket;
import tfc.smallerunits.utils.world.client.FakeClientWorld;

@Mixin(CommandBlockScreen.class)
public class CommandBlockScreenMixin {
	@Shadow
	private CommandBlockTileEntity.Mode commandBlockMode;
	
	@Shadow
	private boolean conditional;
	
	@Shadow
	private boolean automatic;
	
	@Shadow
	@Final
	private CommandBlockTileEntity commandBlock;
	
	@Inject(at = @At("HEAD"), method = "func_195235_a", cancellable = true)
	public void preSendCommandBlockInfo(CommandBlockLogic p_195235_1_, CallbackInfo ci) {
		if (commandBlock.getWorld() instanceof FakeClientWorld) {
			FakeClientWorld wld = (FakeClientWorld) commandBlock.getWorld();
			Smallerunits.NETWORK_INSTANCE.sendToServer(
					new CUpdateLittleCommandBlockPacket(
							wld.owner.getPos(),
							new BlockPos(p_195235_1_.getPositionVector()),
							((AbstractCommandBlockScreenAccessor) this).SmallerUnits_getCmdTxtField().getText(),
							this.commandBlockMode,
							p_195235_1_.shouldTrackOutput(),
							this.conditional,
							this.automatic
					)
			);
		}
	}
}
