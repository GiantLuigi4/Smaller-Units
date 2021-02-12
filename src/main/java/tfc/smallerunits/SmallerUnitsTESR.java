package tfc.smallerunits;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.LightType;
import net.minecraftforge.client.model.data.EmptyModelData;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.Unit;

import java.util.Random;

public class SmallerUnitsTESR extends TileEntityRenderer<UnitTileEntity> {
	public SmallerUnitsTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(UnitTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
//		BufferBuilder bufferBuilder = new BufferBuilder(13853);
//		bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
//
//		bufferBuilder.finishDrawing();
//		VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
//		buffer.upload(bufferBuilder);
		
		matrixStackIn.push();
		matrixStackIn.scale(1f/tileEntityIn.unitsPerBlock,1f/tileEntityIn.unitsPerBlock,1f/tileEntityIn.unitsPerBlock);
//		int mainWorldBlockLight = LightTexture.getLightBlock(combinedLightIn);
//		int mainWorldSkyLight = LightTexture.getLightSky(combinedLightIn);
		for (Unit value : tileEntityIn.world.blockMap.values()) {
			matrixStackIn.push();
			matrixStackIn.translate(value.pos.getX(),value.pos.getY(),value.pos.getZ());
//			int blockLight = Math.max(tileEntityIn.world.getLightFor(LightType.BLOCK,value.pos),mainWorldBlockLight);
			Minecraft.getInstance().getBlockRendererDispatcher().renderModel(
					value.state, value.pos, tileEntityIn.world,
					matrixStackIn, bufferIn.getBuffer(RenderTypeLookup.getChunkRenderType(value.state)),
					true, new Random(value.pos.toLong()),
					EmptyModelData.INSTANCE
			);
			matrixStackIn.pop();
		}
		matrixStackIn.pop();
	}
}
