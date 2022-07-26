package tfc.smallerunits;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import tfc.smallerunits.client.render.TileRendererHelper;

public class SUItemRenderer extends BlockEntityWithoutLevelRenderer {
	public SUItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}
	
	@Override
	public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
		int upb = 4;
		if (pStack.hasTag()) {
			CompoundTag tag = pStack.getTag();
			if (tag.contains("upb", Tag.TAG_INT)) {
				upb = tag.getInt("upb");
			}
		}
		pPoseStack.pushPose();
		TileRendererHelper.drawUnit(
				new BlockPos(0, 0, 0), upb,
				true, false, true,
				pBuffer.getBuffer(RenderType.solid()),
				pPoseStack, pPackedLight,
				0, 0, 0
		);
		
		String text = "1/" + upb;
		int scale = upb;
		pPoseStack.translate(2 / 4f, 0.5, 1);
		pPoseStack.scale(1f / scale, 1f / scale, 1f / scale);
		pPoseStack.translate(0, 0.8, 0);
		pPoseStack.mulPose(new Quaternion(180, 0, 0, true));
		pPoseStack.scale(1f / 4, 1f / 4, 1f / 4);
		pPoseStack.translate(-Minecraft.getInstance().font.width("1/" + upb) / 2f, 0, 0);
		Minecraft.getInstance().font.draw(
				pPoseStack, text,
				1, 1, 4210752
		);
		pPoseStack.translate(0, 0, -0.1f);
		Minecraft.getInstance().font.draw(
				pPoseStack, text,
				0, 0, 16777215
		);
		pPoseStack.popPose();
//		if (pTransformType.equals(ItemTransforms.TransformType.GUI)) {
//			pPoseStack.mulPose(new Quaternion(180, 45f,0,true));
//			pPoseStack.mulPose(new Quaternion(-22.5f,0,0,true));
//			pPoseStack.translate(0, -1.3f, -1.1f);
//			pPoseStack.scale(0.125f, 0.125f, 0.125f);
//			pPoseStack.scale(0.5f, 0.5f, 0.5f);
//		} else {
//			pPoseStack.mulPose(new Quaternion(180,0,0,true));
//			pPoseStack.translate(0, -0.25f - 1.35f / 32, -1.1f);
//			pPoseStack.scale(0.125f, 0.125f, 0.125f);
//			pPoseStack.scale(0.333333f, 0.333333f, 0.333333f);
//		}
//		Minecraft.getInstance().font.draw(
//				pPoseStack,
//				text, 1, 1, 4210752
//		);
//		pPoseStack.translate(0, 0, -0.1f);
//		Minecraft.getInstance().font.draw(
//				pPoseStack,
//				text, 0, 0, 16777215
//		);
	}
}
