package tfc.smallerunits;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;
import tfc.smallerunits.utils.SmallUnit;
import tfc.smallerunits.utils.UnitPallet;

import java.util.List;
import java.util.Random;

public class SmallerUnitISTER extends ItemStackTileEntityRenderer {
	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		super.func_239207_a_(stack, p_239207_2_, matrixStack, buffer, combinedLight, combinedOverlay);
		
		CompoundNBT nbt = stack.getOrCreateTag().getCompound("BlockEntityTag");
		
		int unitsPerBlock = nbt.getInt("upb");
		
		UnitPallet pallet = new UnitPallet(nbt.getCompound("containedUnits"));
		
		matrixStack.push();
		matrixStack.scale(1f / unitsPerBlock, 1f / unitsPerBlock, 1f / unitsPerBlock);
		for (SmallUnit value : pallet.posUnitMap.values()) {
			matrixStack.push();
			matrixStack.translate(value.pos.getX(), value.pos.getY(), value.pos.getZ());
			Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(
					value.state, matrixStack,
					buffer, combinedLight, combinedOverlay,
					EmptyModelData.INSTANCE
			);
//			IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(value.state);
//			IVertexBuilder builder = buffer.getBuffer(RenderTypeLookup.getChunkRenderType(value.state));
//			for (Direction direction : Direction.values()) {
//				List<BakedQuad> quadList = model.getQuads(value.state, direction, new Random(value.pos.toLong()));
//				for (BakedQuad bakedQuad : quadList) {
//					builder.addQuad(matrixStack.getLast(),bakedQuad,1,1,1,combinedLight,combinedOverlay);
//				}
//			}
//			List<BakedQuad> quadList = model.getQuads(value.state, null, new Random(value.pos.toLong()));
//			for (BakedQuad bakedQuad : quadList) {
//				builder.addQuad(matrixStack.getLast(),bakedQuad,1,1,1,combinedLight,combinedOverlay);
//			}
			matrixStack.pop();
		}
		matrixStack.pop();
	}
}
