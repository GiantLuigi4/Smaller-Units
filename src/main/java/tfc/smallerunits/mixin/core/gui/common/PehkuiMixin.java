package tfc.smallerunits.mixin.core.gui.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.utils.PositionalInfo;
import tfc.smallerunits.utils.platform.PlatformUtils;
import virtuoel.pehkui.util.ScaleUtils;

// TODO: this is temporary
//#if FABRIC==1
@Mixin(ScaleUtils.class)
//#else
//$$ @Mixin(value = ScaleUtils.class, remap = false)
//#endif
public class PehkuiMixin {
	@Inject(at = @At("TAIL"), method = "getBlockReachScale(Lnet/minecraft/world/entity/Entity;F)F", cancellable = true)
	private static void modifyReach(Entity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
		if (entity instanceof LivingEntity livingEntity) {
			AttributeInstance instance = PlatformUtils.getReachAttrib(livingEntity);
			if (instance == null) return;
			AttributeModifier modifier = instance.getModifier(PositionalInfo.SU_REACH_UUID);
			if (modifier == null) return;
			cir.setReturnValue((float) (cir.getReturnValueF() * modifier.getAmount()));
		}
	}
}
