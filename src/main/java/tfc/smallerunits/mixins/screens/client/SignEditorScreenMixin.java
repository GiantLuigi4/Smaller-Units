package tfc.smallerunits.mixins.screens.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.tileentity.SignTileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.screens.CUpdateLittleSignPacket;
import tfc.smallerunits.utils.world.client.FakeClientWorld;

@Mixin(EditSignScreen.class)
public class SignEditorScreenMixin {
	@Shadow
	@Final
	private SignTileEntity tileSign;
	
	@Shadow
	@Final
	private String[] field_238846_r_;
	
	@Inject(at = @At("HEAD"), method = "onClose", cancellable = true)
	public void preSendCommandBlockInfo(CallbackInfo ci) {
		if (tileSign.getWorld() instanceof FakeClientWorld) {
			ci.cancel();
			Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
			UnitTileEntity te = ((FakeClientWorld) tileSign.getWorld()).owner;
			if (te == null) return;
			CUpdateLittleSignPacket packet = new CUpdateLittleSignPacket(te.getPos(), this.tileSign.getPos(), this.field_238846_r_[0], this.field_238846_r_[1], this.field_238846_r_[2], this.field_238846_r_[3]);
			Smallerunits.NETWORK_INSTANCE.sendToServer(packet);
			this.tileSign.setEditable(true);
		}
	}
}
