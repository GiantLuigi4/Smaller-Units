package tfc.smallerunits;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import tfc.smallerunits.client.render.TileRendererHelper;

public class SUItemRenderer extends BlockEntityWithoutLevelRenderer {
	public SUItemRenderer() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack pStack, ItemDisplayContext $$1, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
		int upb = 4;
		if (pStack.hasTag()) {
			CompoundTag tag = pStack.getTag();
			if (tag.contains("upb", Tag.TAG_INT)) {
				upb = tag.getInt("upb");
			}
		}
		pPoseStack.pushPose();
		
		/* draw indicator */
		VertexConsumer consumer = pBuffer.getBuffer(RenderType.solid());
		TileRendererHelper.drawUnit(
				null, new BlockPos(0, 0, 0), upb,
				false, false, true,
				consumer,
				pPoseStack, pPackedLight,
				0, 0, 0
		);
		
		/* draw text */
		String text = "1/" + upb;
		int scale = upb;
		pPoseStack.translate(2 / 4f, 0.5, 1);
		pPoseStack.scale(1f / 4, 1f / 4, 1f / 4);
		pPoseStack.translate(-0.1, 0.8, 0.1);
		pPoseStack.mulPose(new Quaternionf().fromAxisAngleDeg(1, 0, 0, 180));
		pPoseStack.scale(1f / 4, 1f / 4, 1f / 4);
		pPoseStack.translate(-Minecraft.getInstance().font.width("1/" + upb) / 2f, 0, 0);
		// TODO: look at 1.19, I don't remember how this actually looked
		Minecraft.getInstance().font.drawInBatch(text, 1f, 1f, 4079166, false, pPoseStack.last().pose(),
				pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight, false
		);
		pPoseStack.translate(0, 0, -0.1f);
		Minecraft.getInstance().font.drawInBatch(text, 0f, 0f, 16514043, false, pPoseStack.last().pose(),
				pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight, false
		);
		pPoseStack.popPose();
		
		// TODO: what?
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
