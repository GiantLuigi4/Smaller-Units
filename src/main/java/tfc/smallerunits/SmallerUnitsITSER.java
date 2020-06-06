package tfc.smallerunits;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

public class SmallerUnitsITSER extends ItemStackTileEntityRenderer {
	public SmallerUnitsITSER() {}
	
	@Override
	public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		super.render(itemStackIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		SmallerUnitsTileEntity tileEntity=new SmallerUnitsTileEntity();
		try {
			tileEntity.read(itemStackIn.getOrCreateTag().getCompound("BlockEntityTag"));
		} catch (Exception err) {}
		SmallerUnitTESR.getINSTANCE().render(tileEntity,0,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn);
	}
}
