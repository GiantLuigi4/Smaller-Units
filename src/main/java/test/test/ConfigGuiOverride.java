package test.test;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ConfigGuiOverride extends GuiConfig {
    public ConfigGuiOverride(GuiScreen parentScreen, String modid, String title) {
        this(parentScreen, modid, false, false, title, ConfigManager.getModConfigClasses(modid));
    }
    private static List<IConfigElement> collectConfigElements(Class<?>[] configClasses) {
        List<IConfigElement> toReturn;
        if(configClasses.length == 1) {
            toReturn = ConfigElement.from(configClasses[0]).getChildElements();
        }
        else {
            toReturn = new ArrayList<IConfigElement>();
            for(Class<?> clazz : configClasses) {
                toReturn.add(ConfigElement.from(clazz));
            }
        }
        toReturn.sort(Comparator.comparing(e -> I18n.format(e.getLanguageKey())));
        return toReturn;
    }
    public ConfigGuiOverride(GuiScreen parentScreen, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, Class<?>... configClasses) {
        this(parentScreen, collectConfigElements(configClasses), modID, null, allRequireWorldRestart, allRequireMcRestart, title, null);
    }
    public ConfigGuiOverride(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        super(parentScreen, configElements, modID, configID, allRequireWorldRestart, allRequireMcRestart, title);
    }
    public ConfigGuiOverride(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title) {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title);
    }
    public ConfigGuiOverride(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
        super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
    }
    public ConfigGuiOverride(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, @Nullable String configID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, @Nullable String titleLine2) {
        super(parentScreen, configElements, modID, configID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
    }
    ArrayList<float[]> color = new ArrayList<>();
    int pressedY=0;
    public HashMap<String,IConfigElement> elements=new HashMap<>();
    public GuiConfigEntries entries;
    public ConfigGuiOverride(GuiConfig config) {
        this(config.parentScreen, config.modID, config.title);
        entries=new GuiConfigEntries(config,mc) {
            @Override
            public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
                entryList.drawScreen(mouseXIn,mouseYIn,partialTicks);
                this.listEntries=entryList.listEntries;
                this.scrollBarX=entryList.scrollBarX;
                this.top=entryList.top;
                this.left=entryList.left;
                this.right=entryList.right;
                this.height=entryList.height;
                this.width=entryList.width;
                this.bottom=entryList.bottom;
                this.amountScrolled=entryList.getAmountScrolled();
                if (this.visible) {
                    this.drawBackground();
                    int i = this.getScrollBarX();
                    int j = i + 6;
                    this.bindAmountScrolled();
                    GlStateManager.disableLighting();
                    GlStateManager.disableFog();
                    Tessellator tessellator = Tessellator.getInstance();
                    BufferBuilder bufferbuilder = tessellator.getBuffer();
                    // Forge: background rendering moved into separate method.
                    this.drawContainerBackground(tessellator);
                    int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
                    int l = this.top + 4 - (int)this.amountScrolled;

                    if (this.hasListHeader) {
                        this.drawListHeader(k, l, tessellator);
                    }

                    this.drawSelectionBox(k, l, mouseXIn, mouseYIn, partialTicks);


                    int handleMY=mouseY;
                    if (pressedY!=-1) {
                        handleMY=pressedY;
                    }
                    Iterator iter=configElements.iterator();
                    int i2=0;
                    while (iter.hasNext()) {
                        i2+=1;
                        IConfigElement element=((IConfigElement)iter.next());
                        if (element.getType().toString().equals(Property.Type.COLOR.toString())) {
                            color.set(Integer.parseInt((String)element.getDefault()),drawAndHandleColorSlider(i2*entryList.getSlotHeight()+8,mouseX,handleMY,color.get(Integer.parseInt((String)element.getDefault())),org.lwjgl.input.Mouse.isButtonDown(0)));
                        }
                    }


                    GlStateManager.disableDepth();
                    this.overlayBackground(0, this.top, 255, 255);
                    this.overlayBackground(this.bottom, this.height, 255, 255);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
                    GlStateManager.disableAlpha();
                    GlStateManager.shadeModel(7425);
                    GlStateManager.disableTexture2D();
                    int i1 = 4;
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    bufferbuilder.pos((double)this.left, (double)(this.top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
                    bufferbuilder.pos((double)this.right, (double)(this.top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
                    bufferbuilder.pos((double)this.right, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                    bufferbuilder.pos((double)this.left, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                    tessellator.draw();
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    bufferbuilder.pos((double)this.left, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                    bufferbuilder.pos((double)this.right, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                    bufferbuilder.pos((double)this.right, (double)(this.bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
                    bufferbuilder.pos((double)this.left, (double)(this.bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
                    tessellator.draw();
                    int j1 = this.getMaxScroll();

                    if (j1 > 0) {
                        int k1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                        k1 = MathHelper.clamp(k1, 32, this.bottom - this.top - 8);
                        int l1 = (int)this.amountScrolled * (this.bottom - this.top - k1) / j1 + this.top;

                        if (l1 < this.top)
                        {
                            l1 = this.top;
                        }

                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                        bufferbuilder.pos((double)i, (double)this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                        bufferbuilder.pos((double)j, (double)this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                        bufferbuilder.pos((double)j, (double)this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                        bufferbuilder.pos((double)i, (double)this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                        tessellator.draw();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                        bufferbuilder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)j, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                        bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                        tessellator.draw();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                        bufferbuilder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                        bufferbuilder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                        bufferbuilder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                        bufferbuilder.pos((double)i, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                        tessellator.draw();
                    }

                    this.renderDecorations(mouseXIn, mouseYIn);
                    GlStateManager.enableTexture2D();
                    GlStateManager.shadeModel(7424);
                    GlStateManager.enableAlpha();
                    GlStateManager.disableBlend();
                    i2=0;
                    for (IConfigEntry entry : this.listEntries) {
                        i2+=1;
                        if (i2*entryList.getSlotHeight()+8<=mouseYIn&&mouseYIn<=i2*entryList.getSlotHeight()+8+entryList.getSlotHeight()) {
                            ArrayList<String> tooltip = new ArrayList<>();
                            for (String str : entry.getConfigElement().getComment().split("\n")) {
                                tooltip.add(str);
                            }
                            if (tooltip.size()>=2) {
                                drawToolTip(tooltip,mouseXIn,mouseYIn);
                                GlStateManager.resetColor();
                                GlStateManager.disableLighting();
                            }
                        }
                    }
                }
            }
        };
        try {
            for (IConfigElement entry:configElements) {
                SmallerUnitsMod.log.log(Level.INFO,entry.getName());
                if (entry.getName().equals("storage")) {
                    elements.put(entry.getName(),entry);
                }
            }
        } catch (Exception err) {}
        configElements.remove(elements.get("storage"));
        elements.remove("storage");
        addColorProperty("Frame Border",Config.colorListHelper.getColor(Config.strg.colorframe));
        addColorProperty("Frame Color",Config.colorListHelper.getColor(Config.strg.colorbg));
        addColorProperty("Slider Border",Config.colorListHelper.getColor(Config.strg.colorsliderframe));
        addColorProperty("Slider Color",Config.colorListHelper.getColor(Config.strg.colorsliderbg));
        addColorProperty("Slider Handle Frame",Config.colorListHelper.getColor(Config.strg.colorslidercollisionframe));
        addColorProperty("Slider Handle Color",Config.colorListHelper.getColor(Config.strg.colorslidercollision));
        addColorProperty("Text Color",Config.colorListHelper.getColor(Config.strg.colortext));
    }

    public void addColorProperty(String name,Color col) {
        //https://docs.oracle.com/javase/1.5.0/docs/api/java/awt/Color.html#RGBtoHSB(int,%20int,%20int,%20float[])
        color.add(Color.RGBtoHSB(col.getRed(),col.getGreen(),col.getBlue(),null));
        Property prop=constructColorProperty(name,color.size()-1);
        elements.put(prop.getName(),new ConfigElement(prop));
        configElements.add(new ConfigElement(prop));
    }

    public Property constructColorProperty(String name,int color) {
        Property prop=new Property(name,""+color, Property.Type.COLOR);
        prop.setMinValue(0);
        prop.setMaxValue(255);
        prop.setHasSlidingControl(true);
        return prop;
    }

    @Override
    protected <T extends GuiButton> T addButton(T buttonIn) {
        return super.addButton(buttonIn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.entries.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 8, 16777215);
        String title2 = this.titleLine2;

        if (title2 != null) {
            int strWidth = mc.fontRenderer.getStringWidth(title2);
            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");
            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                title2 = mc.fontRenderer.trimStringToWidth(title2, width - 6 - ellipsisWidth).trim() + "...";
            this.drawCenteredString(this.fontRenderer, title2, this.width / 2, 18, 16777215);
        }

        for (int i = 0; i < this.buttonList.size(); ++i) {
            ((GuiButton)this.buttonList.get(i)).drawButton(this.mc, mouseX, mouseY, partialTicks);
        }

        for (int j = 0; j < this.labelList.size(); ++j) {
            ((GuiLabel)this.labelList.get(j)).drawLabel(this.mc, mouseX, mouseY);
        }

        this.btnUndoAll.enabled = this.entryList.areAnyEntriesEnabled(this.chkApplyGlobally.isChecked()) && this.entryList.hasChangedEntry(this.chkApplyGlobally.isChecked());
        this.btnDefaultAll.enabled = this.entryList.areAnyEntriesEnabled(this.chkApplyGlobally.isChecked()) && !this.entryList.areAllEntriesDefault(this.chkApplyGlobally.isChecked());
        this.entries.drawScreenPost(mouseX, mouseY, partialTicks);
        if (this.undoHoverChecker.checkHover(mouseX, mouseY))
            this.drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.undoAll").split("\n")), mouseX, mouseY);
        if (this.resetHoverChecker.checkHover(mouseX, mouseY))
            this.drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.resetAll").split("\n")), mouseX, mouseY);
        if (this.checkBoxHoverChecker.checkHover(mouseX, mouseY))
            this.drawToolTip(Arrays.asList(I18n.format("fml.configgui.tooltip.applyGlobally").split("\n")), mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        Config.strg.colorframe=getColorForSave(0);
        Config.strg.colorbg=getColorForSave(1);
        Config.strg.colorsliderframe=getColorForSave(2);
        Config.strg.colorsliderbg=getColorForSave(3);
        Config.strg.colorslidercollisionframe=getColorForSave(4);
        Config.strg.colorslidercollision=getColorForSave(5);
        Config.strg.colortext=getColorForSave(6);
        ConfigManager.sync(SmallerUnitsMod.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
        super.onGuiClosed();
    }

    public int[] getColorForSave(int id) {
        return Config.colorListHelper.genList(Color.getHSBColor(color.get(id)[0],color.get(id)[1],color.get(id)[2]));
    }

    public float[] drawAndHandleColorSlider(int y, int mouseX, int mouseY, float[] currentColor, boolean pressed) {
        GlStateManager.disableTexture2D();
        float[] color=currentColor.clone();
        try {
            int left=entryList.labelX+entryList.maxLabelTextWidth+10;
            int top=-entryList.getAmountScrolled()+y;
            int width=entryList.controlWidth-4;
            int height=entryList.slotHeight-4;
            Color col=Color.BLACK;
            Color col2=Color.LIGHT_GRAY;
            Minecraft.getMinecraft().draw(left-1,top-1,0,0,width+2,height+2,col2.getRed(),col2.getGreen(),col2.getBlue(),255);
            Minecraft.getMinecraft().draw(left,top,0,0,width,height,col.getRed(),col.getGreen(),col.getBlue(),255);
            for (float value=0; value<=1;value+=0.005) {
                Color col3=Color.getHSBColor(value,1,1);
                Minecraft.getMinecraft().draw((int)(left+(value*width)),top,0,0,(int)(width-(value*width)),height/3+1,col3.getRed(),col3.getGreen(),col3.getBlue(),255);
                if (mouseX>=(int)(left+(value*width))&&
                    mouseX<=left+width&&
                    mouseY>=top&&
                    mouseY<=top+((height/3)*1)&&
                    pressed) {
                    color[0]=value;
                    pressedY=mouseY;
                }
                col3=Color.getHSBColor(color[0],value-0.005f,1);
                Minecraft.getMinecraft().draw((int)(left+(value*width)),top+((height/3)*1),0,0,(int)(width-(value*width)),height/3+1,col3.getRed(),col3.getGreen(),col3.getBlue(),255);
                if (mouseX>=(int)(left+(value*width))&&
                    mouseX<=left+width&&
                    mouseY>=top+((height/3)*1)&&
                    mouseY<=top+((height/3)*2)&&
                    pressed) {
                    color[1]=value-0.005f;
                    pressedY=mouseY;
                }
                col3=Color.getHSBColor(color[0],color[1],value-0.01f);
                Minecraft.getMinecraft().draw((int)(left+(value*width)),top+((height/3)*2),0,0,(int)(width-(value*width)),height/3+1,col3.getRed(),col3.getGreen(),col3.getBlue(),255);
                if (mouseX>=(int)(left+(value*width))&&
                    mouseX<=left+width&&
                    mouseY>=top+((height/3)*2)&&
                    mouseY<=top+((height/3)*3)&&
                    pressed) {
                    color[2]=value-0.01f;
                    pressedY=mouseY;
                }
            }
            Minecraft.getMinecraft().draw(left,top+((height/3)*2),0,0,width,1,col2.getRed(),col2.getGreen(),col2.getBlue(),255);
            Minecraft.getMinecraft().draw(left,top+((height/3)*1),0,0,width,1,col2.getRed(),col2.getGreen(),col2.getBlue(),255);
            int leftPreview=left+width+entryList.getSlotHeight()+entryList.getSlotHeight()+entryList.getSlotHeight()-8;
            if ((leftPreview>=entryList.scrollBarX||leftPreview-entryList.getSlotHeight()<=entryList.scrollBarX)&&entryList.getMaxScroll()>=1) {
                leftPreview+=10;
            }
            Minecraft.getMinecraft().draw(leftPreview,top-1,0,0,entryList.getSlotHeight()-2,entryList.getSlotHeight()-2,col2.getRed(),col2.getGreen(),col2.getBlue(),255);
            Color col4=Color.getHSBColor(color[0],color[1],color[2]);
            Minecraft.getMinecraft().draw(leftPreview+1,top,0,0,entryList.getSlotHeight()-4,entryList.getSlotHeight()-4,col4.getRed(),col4.getGreen(),col4.getBlue(),255);
            Color col5=Color.DARK_GRAY;
            Color col6=Color.getHSBColor(color[0],1,1);
            Color col7=Color.getHSBColor(color[0],color[1],1);
            Color col8=Color.getHSBColor(color[0],color[1],color[2]);
            Minecraft.getMinecraft().draw((int)(left+(color[0]*width)-1),top-1,0,0,3,height/3+2,col5.getRed(),col5.getGreen(),col5.getBlue(),255);
            Minecraft.getMinecraft().draw((int)(left+(color[0]*width)),top,0,0,1,height/3,col6.getRed(),col6.getGreen(),col6.getBlue(),255);

            Minecraft.getMinecraft().draw((int)(left+(color[1]*width)-1),top-1+(height/3),0,0,3,height/3+2,col5.getRed(),col5.getGreen(),col5.getBlue(),255);
            Minecraft.getMinecraft().draw((int)(left+(color[1]*width)),top+(height/3),0,0,1,height/3,col7.getRed(),col7.getGreen(),col7.getBlue(),255);

            Minecraft.getMinecraft().draw((int)(left+((color[2]+0.01f)*width)-1),top+((height/3)*2),0,0,3,height/3+2,col5.getRed(),col5.getGreen(),col5.getBlue(),255);
            Minecraft.getMinecraft().draw((int)(left+((color[2]+0.01f)*width)),top+1+((height/3)*2),0,0,1,height/3,col8.getRed(),col8.getGreen(),col8.getBlue(),255);
        } catch (Exception err) {}
        if (!pressed) {
            pressedY=-1;
        }
        GlStateManager.enableTexture2D();
        return color;
    }
}
