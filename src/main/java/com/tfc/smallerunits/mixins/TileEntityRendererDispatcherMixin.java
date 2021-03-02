package com.tfc.smallerunits.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tfc.smallerunits.SmallerUnitsConfig;
import com.tfc.smallerunits.SmallerUnitsTESR;
import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.utils.rendering.BufferCache;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRendererDispatcherMixin<E extends TileEntity> {
	@Inject(at = @At("HEAD"), method = "renderTileEntity(Lnet/minecraft/tileentity/TileEntity;FLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;)V", cancellable = true)
	public void renderTileEntity(E tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, CallbackInfo ci) {
		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get() && tileEntityIn instanceof UnitTileEntity) {
			SmallerUnitsTESR.render((UnitTileEntity) tileEntityIn, partialTicks, matrixStackIn, new BufferCache(bufferIn, matrixStackIn), LightTexture.packLight(tileEntityIn.getWorld().getLightFor(LightType.BLOCK, tileEntityIn.getPos()), tileEntityIn.getWorld().getLightFor(LightType.SKY, tileEntityIn.getPos())), OverlayTexture.NO_OVERLAY);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntityRenderer;", cancellable = true)
	public void getRenderer(E tileEntityIn, CallbackInfoReturnable<TileEntityRenderer<E>> cir) {
		if (SmallerUnitsConfig.CLIENT.useExperimentalRenderer.get() && tileEntityIn instanceof UnitTileEntity) {
			cir.setReturnValue((TileEntityRenderer<E>) SmallerUnitsTESR.INSTANCE);
		}
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
