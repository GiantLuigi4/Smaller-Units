package test.test;

        import net.minecraft.block.material.Material;
        import net.minecraft.block.state.IBlockState;
        import net.minecraft.client.Minecraft;
        import net.minecraft.client.gui.GuiScreen;
        import net.minecraft.client.gui.GuiSlider;
        import net.minecraft.client.renderer.BufferBuilder;
        import net.minecraft.client.renderer.GlStateManager;
        import net.minecraft.client.renderer.RenderHelper;
        import net.minecraft.client.renderer.Tessellator;
        import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
        import net.minecraft.entity.player.EntityPlayer;
        import net.minecraft.item.Item;
        import net.minecraft.util.EnumHand;
        import net.minecraft.util.math.AxisAlignedBB;
        import net.minecraft.util.math.BlockPos;
        import net.minecraft.util.math.RayTraceResult;
        import net.minecraft.util.math.Vec3d;
        import net.minecraftforge.client.event.RenderGameOverlayEvent;
        import net.minecraftforge.client.event.RenderHandEvent;
        import net.minecraftforge.client.event.RenderWorldLastEvent;
        import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
        import org.apache.logging.log4j.Level;

        import java.awt.*;
        import java.awt.image.BufferedImage;
        import java.util.ArrayList;

public class GUI {
    public int currentSize=Config.scaleMin;
    public int prevSize=Config.scaleMin;
    public float prevTicks=0;
    public boolean isOpen=false;
    public BlockPos GUIPos=new BlockPos(10,255,10);
    @SubscribeEvent
    public void overlayEvent(RenderWorldLastEvent event) {
//        if (event.equals(RenderGameOverlayEvent.ElementType.HELMET)) {
        EntityPlayer player=Minecraft.getMinecraft().player;
        if (player.getHeldItem(EnumHand.MAIN_HAND).getItem().equals(Item.getByNameOrId("smallunits:su"))) {
            if (!isOpen) {
                GUIPos=player.getPosition().add(new BlockPos(0,player.getEyeHeight(),-2));
            }
            isOpen=true;
            float partialTicks=event.getPartialTicks();
            Vec3d eyePos=player.getPositionEyes(partialTicks);
//        Vec3d guiLook=new Vec3d(GUIPos).normalize().(eyePos.normalize()).normalize();
            Vec3d playerLook=player.getLook(partialTicks);
            ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
            ArrayList<Color> cols = new ArrayList<>();
            ArrayList<String> types = new ArrayList<>();
//            double dist=100d/((player.getDistanceSq(GUIPos)));
//            dist=Math.abs(1/dist);
//        if (dist<=0.01) {
//            dist=0.01;
//        } else if (dist>=0.05) {
//            dist=0.05;
//        }
//            double quality = Config.GUIQual;
            for (float i=0.9f;i<=1f;i+=0.1f) {
                try {
//                    SmallerUnitsMod.log.log(Level.INFO,i);
                    Color col=Color.MAGENTA;
                    if (i>=0.9) {
                        boxes.add(new AxisAlignedBB(-i,-i,0.001,i,i,-0.001));
                        col=Config.colorListHelper.getColor(Config.strg.colorframe);
                        types.add("screenFrame");
                    } else {
                        boxes.add(new AxisAlignedBB(-i,-i,0.01,i,i,-0.01));
                        col=Config.colorListHelper.getColor(Config.strg.colorbg);
                        types.add("screen");
                    }
//                    Color col = new Color(1-(i*0.9f),1-(i*0.9f),1-(i*0.9f));
                    cols.add(col);
                } catch (Exception err) {
                    cols.add(Color.MAGENTA);
                }
            }
            double x=-0.8;
            double y=0.6;
            double width=0.5;
            double height=0.25;
            double thickness=0.025;
            boxes.add(new AxisAlignedBB(x,y,-thickness/1.1f,x+width,y+height,thickness/1.1f));
            cols.add(Config.colorListHelper.getColor(Config.strg.colorsliderbg));
            types.add("optionBG");
            boxes.add(new AxisAlignedBB(x,y,-thickness,x+width,y+height,thickness));
            cols.add(Config.colorListHelper.getColor(Config.strg.colorsliderframe));
            types.add("optionBox");

//        currentSize-=1;
//        if (currentSize<=1) {
//            currentSize=8;
//        }
//        currentSize=8;

//        x=(((prevSize+((currentSize-prevSize)*partialTicks)-Config.scaleMin))/(8d-(Config.scaleMin-8d)))*(8d/Config.scaleMin)-0.79;
            x=((prevSize+(currentSize-prevSize)*partialTicks)-Config.scaleMin) / ((Config.scaleMax - Config.scaleMin)*2.3d)-0.789d;
            y=0.61;
            width=0.05;
            height=0.23;
            thickness=0.03;
            boxes.add(new AxisAlignedBB(x,y,-thickness/1.1f,x+width,y+height,thickness/1.1f));
            cols.add(Config.colorListHelper.getColor(Config.strg.colorslidercollision));
            types.add("optionBG");
            boxes.add(new AxisAlignedBB(x,y,-thickness,x+width,y+height,thickness));
            cols.add(Config.colorListHelper.getColor(Config.strg.colorslidercollisionframe));
            types.add("optionSlider");
            if (partialTicks<=prevTicks) {
                prevSize=currentSize;
                prevTicks=0;
            } else {
                prevTicks=partialTicks;
            }

            GlStateManager.pushMatrix();
//        double rotationYaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * (double)partialTicks;
//        double rotationPitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * (double)partialTicks;
            double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
            double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
            double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
//        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);
//        GlStateManager.rotate((float)rotationYaw+180,0,1f,0);
//        GlStateManager.rotate((float)rotationPitch,1f,0,0);
//        GlStateManager.rotate((float)rotationYaw+(float)rotationPitch/2f,0,0,-1);
            GlStateManager.translate(-d3+0.5f,-d4+0.5f,-d5+0.5f);
            GlStateManager.translate(GUIPos.getX(),GUIPos.getY(),GUIPos.getZ());
//        GlStateManager.rotate((float)guiLook.x,1,0,0);
//        GlStateManager.rotate((float)guiLook.y,0,1,0);
//        GlStateManager.rotate((float)guiLook.z,0,1,1);
//        GlStateManager.rotate((float)rotationYaw+180,0,-1f,0);
//        GlStateManager.rotate((float)rotationPitch,-1f,0,0);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.enableLighting();
            GlStateManager.enableFog();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(true);
            Vec3d range = (eyePos.add(playerLook.scale(player.REACH_DISTANCE.getDefaultValue())));


            String scaletext="scale:1/"+currentSize;
            Color bgCol=new Color(0,0,0,0);
            Color pxlCol=Config.colorListHelper.getColor(Config.strg.colortext);
            double scale=Config.FontQualit;
            y=0.3;
            x=-0.8;
            BufferedImage fontScanner = new BufferedImage((int)(scaletext.length()*6*scale),(int)(15*scale),BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=(Graphics2D)fontScanner.getGraphics();
//            g.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON));
            g.setColor(bgCol);
            g.fillRect(0,0,fontScanner.getWidth(),fontScanner.getHeight());
            g.scale(scale,scale);
            g.setColor(pxlCol);
            g.drawString(scaletext,0,(int)(fontScanner.getHeight()/(1.25*scale)));
            for (int ix=fontScanner.getWidth()-1;ix>=0;ix-=1) {
                for (int iy=fontScanner.getHeight()-1;iy>=0;iy-=1) {
                    try {
                        if ((ix==fontScanner.getWidth()-1||iy==fontScanner.getHeight()-1||ix==0||iy==0)&&false) {
                            int tx=ix;
                            int ty=(fontScanner.getHeight()-iy);
                            double fillThickness=0.015;
                            Color col=Color.RED;
                            cols.add(col);
                            boxes.add(new AxisAlignedBB((tx/100f)/scale,(ty/100f)/scale,fillThickness,((tx+01f)/100f)/scale,((ty+01f)/100f)/scale,-fillThickness).offset(new Vec3d(x,y,0)));
                            types.add("text");
                        }
                        if (!new Color(fontScanner.getRGB(ix,iy)).equals(bgCol)) {
                            int tx=ix;
                            int ty=(fontScanner.getHeight()-iy);
                            Color col=new Color(fontScanner.getRGB(ix,iy));
                            try {
                                col=new Color(col.getRed(),col.getBlue(),col.getGreen(),fontScanner.getColorModel().getAlpha(fontScanner.getRaster().getDataElements(ix, iy, null)));
                            } catch (Exception err) {}
                            double fillThickness=0.015*(1f/((col.getRGB()-pxlCol.getRGB())+1));
                            boxes.add(new AxisAlignedBB((tx/100f)/scale,(ty/100f)/scale,fillThickness,((tx+01f)/100f)/scale,((ty+01f)/100f)/scale,-fillThickness).offset(new Vec3d(x,y,0)));
                            cols.add(col);
                            types.add("text");
                        }
                    } catch (ArrayIndexOutOfBoundsException err) {}
                }
            }
//        GlStateManager.enableLighting();
//        RenderHelper.enableStandardItemLighting();
//        RenderHelper.enableGUIStandardItemLighting();
            for (int i=0;i<boxes.size();i++) {
//                SmallerUnitsMod.log.log(Level.INFO,bb);
//                BlockPos blockpos = movingObjectPositionIn.getBlockPos();
//                IBlockState iblockstate = this.world.getBlockState(blockpos);

//                if (iblockstate.getMaterial() != Material.AIR && this.world.getWorldBorder().contains(blockpos))
//                {
//                    bb=bb.offset(player.getPositionEyes(partialTicks).scale(0.5));
//                SmallerUnitsMod.log.log(Level.INFO,bb.offset(-d3, -d4, -d5));
                AxisAlignedBB bb=boxes.get(i);
                RayTraceResult traceResult=bb.offset(GUIPos).offset(0.5,0.5,0.5).calculateIntercept(eyePos,range);
//                    SmallerUnitsMod.log.log(Level.INFO,bb.offset(GUIPos));
//                    SmallerUnitsMod.log.log(Level.INFO,eyePos);
//                    SmallerUnitsMod.log.log(Level.INFO,range);
                if (traceResult!=null) {
                    Vec3d hitvec=traceResult.hitVec.add(bb.getCenter());
                    AxisAlignedBB bb2=new AxisAlignedBB(hitvec.x-0.025,hitvec.y-0.025,hitvec.z-0.025,hitvec.x+0.025,hitvec.y+0.025,hitvec.z+0.025);
                    Color col = Color.CYAN;
                    if (types.get(i).equals("optionBox"))
                        col=Color.GREEN.darker();
                    else if (types.get(i).equals("optionSlider"))
                        col=Color.GREEN;
//                        boolean keyPress=false;
//                        boolean mousePress=false;
//                        try {
//                            keyPress=org.lwjgl.input.Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode());
//                        } catch (Exception err) {}
//                        try {
//                            keyPress=org.lwjgl.input.Mouse.isButtonDown(Minecraft.getMinecraft().gameSettings.keyBindAttack.getKeyCode());
//                        } catch (Exception err) {}
                    try {
                        if (org.lwjgl.input.Mouse.isButtonDown(0)) {
                            for (int h=Config.scaleMin;h<=Config.scaleMax;h+=1) {
                                x=(h-Config.scaleMin) / ((Config.scaleMax - Config.scaleMin)*2.3d)-0.789d;
                                y=0.61;
                                width=0.05;
                                height=0.23;
                                thickness=0.03;
                                if (new AxisAlignedBB(x,y,-thickness,x+width,y+height,thickness).offset(GUIPos).offset(0.5,0.5,0.5).calculateIntercept(eyePos,range)!=null) {
                                    currentSize=h;
//                                    player.setAlwaysRenderNameTag(true);
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException err) {}
//                        try {
//                            col=new Color(cols.get(i).getRGB()*-1);
//                        } catch (Exception err) {}
//                    GlStateManager.glLineWidth((float)quality*200);
//                    event.getContext().drawSelectionBoundingBox(bb2.offset(new BlockPos(0,0,0).subtract(GUIPos)).offset(-0.5,-0.5,-0.5).offset(bb.getCenter().scale(-1)), col.getRed()/255f, col.getGreen()/255f, col.getBlue()/255f, 1F);
                    drawBoxFrame(bb2.offset(new BlockPos(0,0,0).subtract(GUIPos)).offset(-0.5,-0.5,-0.5).offset(bb.getCenter().scale(-1)), col.getRed(), col.getGreen(), col.getBlue(), 255);
                }
                Color col=cols.get(i);
//                float lineWidth=(float)(Config.GUIQual*10)/(float)bb.offset(GUIPos).getCenter().distanceTo(eyePos)+0.1f;
//                    GlStateManager.glLineWidth((float)quality*500);
                if (types.get(i).equals("text")) {
//                        GlStateManager.glLineWidth(1.5f);
//                    lineWidth=2.5f;
                    bb = bb.grow(0.000025,0.000025,0.025);
                } else if (types.get(i).equals("optionSlider")||types.get(i).equals("optionBox")) {
//                        GlStateManager.glLineWidth((float)quality*550);
//                        lineWidth*=5;
                    bb = bb.grow(0.0025,0.0025,0.0025);
                } else if (types.get(i).equals("screenFrame")) {
                    bb = bb.grow(0.0025,0.0025,0.0025);
                }
//                if ((5/(float)bb.offset(GUIPos).getCenter().distanceTo(eyePos)+0.1f)<=3f&&!types.get(i).equals("text")) {
////                        lineWidth*=30;
//                }
//                if (types.get(i).equals("text")) {
//                    GlStateManager.glLineWidth(0.01f/(float)bb.offset(GUIPos).getCenter().distanceTo(eyePos)+0.1f);
//                } else {
//                    GlStateManager.glLineWidth(lineWidth*(1+((float)Config.GUIQual*(20f/(float)Config.GUIQual))));
//                }
//                    if (bb.offset(GUIPos).getCenter().distanceTo(eyePos)<=range.distanceTo(eyePos)*2) {
//                if (types.get(i).equals("text")) {
//                    event.getContext().drawSelectionBoundingBox(bb, col.getRed()/255f, col.getGreen()/255f, col.getBlue()/255f, 1-(col.getAlpha()/255f));
//                } else {
//                }
                if (types.get(i).contains("optionBox")||types.get(i).contains("optionSlider")) {
//                    event.getContext().drawSelectionBoundingBox(bb, col.getRed()/255f, col.getGreen()/255f, col.getBlue()/255f, col.getAlpha()/255f);
                    drawBoxFrame(bb,col.getRed(),col.getGreen(),col.getBlue(),col.getAlpha());
                } else {
                    drawBox(bb,col.getRed(),col.getGreen(),col.getBlue(),col.getAlpha());
                }
//                    }
//                }
            }
            GlStateManager.glLineWidth(1);
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableFog();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
//        }
        } else {
            isOpen=false;
        }
//        if (event.isCancelable())
//        event.setCanceled(true);
    }
    public void drawBoxFrame(AxisAlignedBB box,int R,int G,int B,int A) {
        double minX=box.minX;
        double minY=box.minY;
        double minZ=box.minZ;
        double maxX=box.maxX;
        double maxY=box.maxY;
        double maxZ=box.maxZ;
        drawBox(new AxisAlignedBB(minX,minY,minZ,maxX,minY+0.01,maxZ),R,G,B,A);
        drawBox(new AxisAlignedBB(minX,maxY-0.01,minZ,maxX,maxY,maxZ),R,G,B,A);
        drawBox(new AxisAlignedBB(maxX-0.01,minY,minZ,maxX,maxY,maxZ),R,G,B,A);
        drawBox(new AxisAlignedBB(minX,minY,minZ,minX+0.01,maxY,maxZ),R,G,B,A);
    }
    public void drawBox(AxisAlignedBB box,int R,int G,int B,int A) {
        double minX=box.minX;
        double minY=box.minY;
        double minZ=box.minZ;
        double maxX=box.maxX;
        double maxY=box.maxY;
        double maxZ=box.maxZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0,0,minZ);
        draw(minX,minY,0,0,maxX-minX,maxY-minY,R,G,B,A);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0,0,maxZ);
        GlStateManager.rotate(180,1,0,0);
        draw(minX,(-maxY-maxY-maxY-maxY)/4,0,0,maxX-minX,maxY-minY,R,G,B,A);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(minX,0,0);
        GlStateManager.rotate(90,0,1,0);
        draw(minZ,minY,0,0,maxZ-minZ,maxY-minY,R,G,B,A);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(maxX,0,0);
        GlStateManager.rotate(-90,0,1,0);
        draw((-maxZ-maxZ-maxZ-maxZ)/4,minY,0,0,maxZ-minZ,maxY-minY,R,G,B,A);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0,minY,0);
        GlStateManager.rotate(90,0,1,0);
        GlStateManager.rotate(-90,1,0,0);
        draw(minZ,(-maxX-maxX-maxX-maxX)/4,0,0,maxZ-minZ,maxX-minX,R,G,B,A);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0,maxY,0);
        GlStateManager.rotate(90,0,1,0);
        GlStateManager.rotate(90,1,0,0);
        draw(minZ,minX,0,0,maxZ-minZ,maxX-minX,R,G,B,A);
        GlStateManager.popMatrix();
    }

    public void draw(double posX, double posY, int texU, int texV, double width, double height, int red, int green, int blue, int alpha) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        bufferbuilder.pos((double)posX, (double)(posY + height), 0.0D).tex((double)((float)texU * 0.00390625F), (double)((float)(texV + height) * 0.00390625F)).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)(posX + width), (double)(posY + height), 0.0D).tex((double)((float)(texU + width) * 0.00390625F), (double)((float)(texV + height) * 0.00390625F)).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)(posX + width), (double)posY, 0.0D).tex((double)((float)(texU + width) * 0.00390625F), (double)((float)texV * 0.00390625F)).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)posX, (double)posY, 0.0D).tex((double)((float)texU * 0.00390625F), (double)((float)texV * 0.00390625F)).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }
}
