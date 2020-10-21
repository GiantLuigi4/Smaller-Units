package tfc.smallerunits;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class SmallerUnitsITSER extends ItemStackTileEntityRenderer {
	public SmallerUnitsITSER() {}
	
	@Override
	public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		super.render(itemStackIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		CompoundNBT defaultNBT=new CompoundNBT();
		defaultNBT.putString("world","0,0,0,Block{minecraft:stone}");
		defaultNBT.putInt("upb",8);
		if (itemStackIn.getOrCreateTag().getCompound("BlockEntityTag").toString().equals(defaultNBT.toString())) {
			Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(Blocks.BLACK_CONCRETE.asItem()), ItemCameraTransforms.TransformType.NONE,combinedLightIn,combinedOverlayIn,matrixStackIn,bufferIn);
		} else {
			SmallerUnitsTileEntity tileEntity=new SmallerUnitsTileEntity();
			tileEntity.isEnchanted=itemStackIn.hasEffect();
			tileEntity.useManual=true;
			try {
				tileEntity.read(itemStackIn.getOrCreateTag().getCompound("BlockEntityTag"));
			} catch (Exception err) {}
			SmallerUnitTESR.getINSTANCE().render(tileEntity,0,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn);
		}
	}
}
