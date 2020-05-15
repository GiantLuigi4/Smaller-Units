package test.test;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ItemPlacer extends ItemBlock {
    public ItemPlacer(Block block) {
        super(block);
    }

    @Override
    public void setTileEntityItemStackRenderer(@Nullable TileEntityItemStackRenderer teisr) {
        super.setTileEntityItemStackRenderer(new TileEntityItemStackRenderer(){
            @Override
            public void renderByItem(ItemStack itemStackIn, float partialTicks) {
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);

                ArrayList<AxisAlignedBB> boxes=new ArrayList<>();
                boxes.add(new AxisAlignedBB(0,0,0,1,1,1));

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
                float red=0f;
                float green=0f;
                float blue=0f;
                float alpha=0f;
                for (AxisAlignedBB bb:boxes) {
                    float minX=(float)bb.minX;
                    float maxX=(float)bb.maxX;
                    float minY=(float)bb.minY;
                    float maxY=(float)bb.maxY;
                    float minZ=(float)bb.minZ;
                    float maxZ=(float)bb.maxZ;
                    Minecraft.getMinecraft().renderGlobal.drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
                }
                tessellator.draw();

                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableFog();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
//                super.renderByItem(itemStackIn, partialTicks);
            }
        });
    }
}
