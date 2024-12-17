package tfc.smallerunits.mixin.core.gui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.PositionalInfo;

@Mixin(AbstractSignEditScreen.class)
public class SignsAreApparentlyStupid {
    @Shadow
    @Final
    private SignBlockEntity sign;

    @Inject(at = @At("HEAD"), method = "isValid", cancellable = true)
    public void preCheckValid(CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (sign.isRemoved()) return;

        LivingEntity $$0 = minecraft.player;
        if ($$0.level() instanceof ITickerLevel) {
            AttributeInstance instance = PlatformUtils.getReachAttrib($$0);
            if (instance == null) return;
            AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);
            if (modifier != null) {
                BlockEntity be = this.sign;
                if (be.getLevel().getBlockEntity(be.worldPosition) != be) {
                    cir.setReturnValue(false);
                } else {
                    cir.setReturnValue(!($$0.distanceToSqr((double)be.worldPosition.getX() + 0.5D, (double)be.worldPosition.getY() + 0.5D, (double)be.worldPosition.getZ() + 0.5D) > (64.0D * modifier.getAmount())));
                }
            }

            BlockEntity be = this.sign;
            if (be.getLevel().getBlockEntity(be.worldPosition) != be) {
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(!($$0.distanceToSqr((double)be.worldPosition.getX() + 0.5D, (double)be.worldPosition.getY() + 0.5D, (double)be.worldPosition.getZ() + 0.5D) > 64.0D));
            }
        }
    }
}
