package tfc.smallerunits;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import tfc.smallerunits.Utils.SmallUnit;

import java.util.*;

public class SmallerUnitTESR extends TileEntityRenderer<SmallerUnitsTileEntity> {
	private static SmallerUnitTESR INSTANCE;
	
	public SmallerUnitTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		INSTANCE=this;
	}
	
	public static SmallerUnitTESR getINSTANCE(){return INSTANCE;}
	
	@Override
	public void render(SmallerUnitsTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		matrixStackIn.push();
		tileEntityIn.read(tileEntityIn.serializeNBT());
		int sc=tileEntityIn.serializeNBT().getInt("upb");
		if (sc==0) {
			sc=4;
		}
		Collection<SmallUnit> units=tileEntityIn.containedWorld.unitHashMap.values();
		matrixStackIn.scale(1f/sc,1f/sc,1f/sc);
		for (SmallUnit unit:units) {
			matrixStackIn.push();
			BlockState state=unit.s;
			matrixStackIn.translate(unit.x,unit.y,unit.z);
			Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn);
			matrixStackIn.pop();
		}
		matrixStackIn.pop();
	}
}
