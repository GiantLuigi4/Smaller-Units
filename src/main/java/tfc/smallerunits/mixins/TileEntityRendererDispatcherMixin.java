package tfc.smallerunits.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.helpers.BufferCacheHelper;
import tfc.smallerunits.utils.rendering.BufferCache;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRendererDispatcherMixin<E extends TileEntity> {
	IRenderTypeBuffer SmallerUnits_buffer = null;
	BufferCache SmallerUnits_bufferCache = null;
	
	@Inject(at = @At("HEAD"), method = "renderTileEntity(Lnet/minecraft/tileentity/TileEntity;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V", cancellable = true)
	public void renderTileEntity(E tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, CallbackInfo ci) {
		if (SmallerUnits_buffer == null || BufferCacheHelper.needsRefresh) {
			SmallerUnits_buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
			SmallerUnits_bufferCache = new BufferCache(SmallerUnits_buffer, matrixStackIn);
			BufferCacheHelper.needsRefresh = false;
		}
		SmallerUnits_bufferCache.stack = matrixStackIn;
		BufferCacheHelper.cache = SmallerUnits_bufferCache;
		if (SmallerUnitsConfig.CLIENT.useExperimentalRendererPt2.get() && tileEntityIn instanceof UnitTileEntity)
			return;
//		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get() && tileEntityIn instanceof UnitTileEntity) {
//			SmallerUnitsTESR.render((UnitTileEntity) tileEntityIn, partialTicks, matrixStackIn, SmallerUnits_bufferCache, LightTexture.packLight(tileEntityIn.getWorld().getLightFor(LightType.BLOCK, tileEntityIn.getPos()), tileEntityIn.getWorld().getLightFor(LightType.SKY, tileEntityIn.getPos())), OverlayTexture.NO_OVERLAY);
//			ci.cancel();
//		}
	}
	
	@Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntityRenderer;", cancellable = true)
	public void getRenderer(E tileEntityIn, CallbackInfoReturnable<TileEntityRenderer<E>> cir) {
		if (SmallerUnitsConfig.CLIENT.useExperimentalRendererPt2.get() && tileEntityIn instanceof UnitTileEntity)
			cir.setReturnValue(null);
//		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get() && tileEntityIn instanceof UnitTileEntity)
//			cir.setReturnValue((TileEntityRenderer<E>) SmallerUnitsTESR.INSTANCE);
	}
}

//TODO: fix this and use it instead
//@Mixin(WorldRenderer.class)
//public class WorldRendererMixin {
//	@Inject(at = @At("TAIL"), method = "updateCameraAndRender(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V")
//	public void renderWorldLast(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline, ActiveRenderInfo activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn, Matrix4f projectionIn, CallbackInfo ci) {
//		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get())
//			RenderingHandler.onRenderWorldLast(matrixStackIn, projectionIn);
//	}
//}
