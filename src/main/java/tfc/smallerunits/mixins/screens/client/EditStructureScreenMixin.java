package tfc.smallerunits.mixins.screens.client;

import net.minecraft.client.gui.screen.EditStructureScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.networking.screens.CUpdateLittleStructureBlockPacket;
import tfc.smallerunits.utils.world.client.FakeClientWorld;

@Mixin(EditStructureScreen.class)
public abstract class EditStructureScreenMixin {
	@Shadow
	private TextFieldWidget integrityEdit;
	@Shadow
	private TextFieldWidget posXEdit;
	@Shadow
	private TextFieldWidget sizeXEdit;
	@Shadow
	private TextFieldWidget posYEdit;
	@Shadow
	private TextFieldWidget sizeYEdit;
	@Shadow
	private TextFieldWidget posZEdit;
	@Shadow
	private TextFieldWidget sizeZEdit;
	@Shadow
	private TextFieldWidget seedEdit;
	@Shadow
	@Final
	private StructureBlockTileEntity tileStructure;
	@Shadow
	private TextFieldWidget dataEdit;
	@Shadow
	private TextFieldWidget nameEdit;
	
	@Shadow
	protected abstract float parseIntegrity(String p_189819_1_);
	
	@Shadow
	protected abstract int parseCoordinate(String p_189817_1_);
	
	@Shadow
	protected abstract long parseSeed(String p_189821_1_);
	
	@Inject(at = @At("HEAD"), method = "func_210143_a", cancellable = true)
	// send help
	// translation: pre send to server
	public void preSebdTisServer(StructureBlockTileEntity.UpdateCommand mode, CallbackInfoReturnable<Boolean> cir) {
		if (tileStructure.getWorld() instanceof FakeClientWorld) {
			FakeClientWorld wld = (FakeClientWorld) tileStructure.getWorld();
			BlockPos offset = new BlockPos(this.parseCoordinate(this.posXEdit.getText()), this.parseCoordinate(this.posYEdit.getText()), this.parseCoordinate(this.posZEdit.getText()));
			BlockPos size = new BlockPos(this.parseCoordinate(this.sizeXEdit.getText()), this.parseCoordinate(this.sizeYEdit.getText()), this.parseCoordinate(this.sizeZEdit.getText()));
			float integrity = this.parseIntegrity(this.integrityEdit.getText());
			long seed = this.parseSeed(this.seedEdit.getText());
			CUpdateLittleStructureBlockPacket packet = new CUpdateLittleStructureBlockPacket(
					wld.owner.getPos(),
					this.tileStructure.getPos(),
					mode,
					this.tileStructure.getMode(),
					this.nameEdit.getText(),
					offset,
					size,
					this.tileStructure.getMirror(),
					this.tileStructure.getRotation(),
					this.dataEdit.getText(),
					this.tileStructure.ignoresEntities(),
					this.tileStructure.showsAir(),
					this.tileStructure.showsBoundingBox(),
					integrity,
					seed
			);
			Smallerunits.NETWORK_INSTANCE.sendToServer(packet);
			cir.setReturnValue(true);
		}
	}
}
