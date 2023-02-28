package tfc.smallerunits.mixin.quality;

import com.mojang.blaze3d.vertex.Tesselator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.client.render.util.SUTesselator;

@Mixin(Tesselator.class)
public class TesselatorMixin {
	private static final SUTesselator SUTesselator = new SUTesselator();
	
	@Inject(at = @At("HEAD"), method = "getInstance", cancellable = true)
	private static void preGetTessel(CallbackInfoReturnable<Tesselator> cir) {
		if (SmallerUnits.tesselScale != 1) {
			SUTesselator.setScale(SmallerUnits.tesselScale);
			cir.setReturnValue(SUTesselator);
		}
	}
}
