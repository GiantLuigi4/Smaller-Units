package test.test;

import com.google.common.collect.Iterators;
import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

@Mod(
        modid = SmallerUnitsMod.MOD_ID,
        name = SmallerUnitsMod.MOD_NAME,
        version = SmallerUnitsMod.VERSION
)
public class SmallerUnitsMod {

    public static final String MOD_ID = "smaller_units";
    public static final String MOD_NAME = "Smaller Units";
    public static final String VERSION = "0.1";
    public static ArrayList<smallUnit> toDraw = new ArrayList<>();
    public static ArrayList<BlockPos> positions = new ArrayList<>();
    public static Logger log;
    public static Debugger debugger = new Debugger();
//    public ArrayList<smallUnit> drawing = new ArrayList<>();

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static SmallerUnitsMod INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
//        File fi = new File("C:/Users/Owner/.gradle/caches/minecraft/deobfedDeps/mcjty/theoneprobe/TheOneProbe-1.12/1.12-1.4.28-17/TheOneProbe-1.12-1.12-1.4.28-17.pom");
//        fi.mkdirs();
        crashAddition.create();
        log=event.getModLog();
//        log.log(Level.INFO,event.getModConfigurationDirectory());
        log.log(Level.INFO,"ConfigFile:"+event.getSuggestedConfigurationFile());
        ConfigManager.load(MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
//        try {
//            Scanner sc = new Scanner(event.getSuggestedConfigurationFile());
//            while (sc.hasNextLine()) {
//                String line=sc.nextLine();
//                if (line.startsWith("    I:\"Scale Max\"=")) {
//                    Config.scaleMax=Integer.parseInt(line.substring("    I:\"Scale Max\"=".length()));
//                } else if (line.startsWith("    I:\"Scale Min\"=")) {
//                    Config.scaleMin=Integer.parseInt(line.substring("    I:\"Scale Min\"=".length()));
//                }
//            }
//        } catch (Exception err) {
//            SmallerUnitsMod.log.log(Level.INFO,"Config not found, and will be created or reset.");
//        }
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new dataHandler());
        MinecraftForge.EVENT_BUS.register(new GUI());
        MinecraftForge.EVENT_BUS.register(new Config.configChangeListener());
        GameRegistry.registerTileEntity(block.TileEntityCustom.class,new ResourceLocation("smallunits","tileentity"));
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    public static KeyBinding debugCollision = new KeyBinding("smallerunits.utils.debug.collision", Keyboard.KEY_Z,"smallerunits.utils");
    public static KeyBinding debugSelection = new KeyBinding("smallerunits.utils.debug.selection", Keyboard.KEY_V,"smallerunits.utils");
    public ArrayList<Integer> usedKeys=new ArrayList<>();
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            setupClient();
            for (KeyBinding kb:Minecraft.getMinecraft().gameSettings.keyBindings) {
                usedKeys.add(+kb.getKeyCode());
            }
            if (usedKeys.contains(Keyboard.KEY_Z)) {
                debugCollision = new KeyBinding("smallerunits.utils.debug.collision", Keyboard.KEY_NONE,"smallerunits.utils");
            }
            ClientRegistry.registerKeyBinding(debugCollision);

            if (usedKeys.contains(Keyboard.KEY_V)) {
                debugSelection = new KeyBinding("smallerunits.utils.debug.selection", Keyboard.KEY_NONE,"smallerunits.utils");
            }
            ClientRegistry.registerKeyBinding(debugSelection);
        }
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }

    protected static HashMap<BlockPos, ArrayList<smallUnit>> unitsContainer = new HashMap<>();
    protected static HashMap<BlockPos, HashMap<Boolean, world>> worldContainer = new HashMap<>();
    public static ArrayList<smallUnit> getUnits(BlockPos pos) {
        return unitsContainer.get(pos);
    }
    public static void setUnit(BlockPos pos, ArrayList<smallUnit> units) {
        if (unitsContainer.containsKey(pos)) {
            unitsContainer.replace(pos,units);
        } else {
            unitsContainer.put(pos,units);
        }
    }
    public static world getWorld(BlockPos pos,boolean client) {
        HashMap<Boolean,world> worlds = worldContainer.get(pos);
        try {
            return worlds.get(client);
        } catch (NullPointerException err) {
            return null;
        }
    }
    public static void setWorld(BlockPos pos, boolean client, world wo) {
        if (worldContainer.containsKey(pos)) {
            HashMap<Boolean,world> worlds = worldContainer.get(pos);
            if (worlds.containsKey(client)) {
                worlds.replace(client,wo);
            } else {
                worlds.put(client,wo);
            }
            worldContainer.replace(pos,worlds);
        } else {
            HashMap<Boolean,world> worlds = new HashMap<>();
            worlds.put(client,wo);
            worldContainer.put(pos,worlds);
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupClient() {
        ClientRegistry.bindTileEntitySpecialRenderer(block.TileEntityCustom.class, new TileEntitySpecialRenderer<block.TileEntityCustom>() {
//            ArrayList<smallUnit> drawing = new ArrayList<>();
            @Override
            public void render(block.TileEntityCustom te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
                dataHandler.isReading=true;
                block bk1 = (block)ObjectRegistryHandler.bk;
                world wo=null;
                try {
                    wo=SmallerUnitsMod.getWorld(te.getPos(),false);
//                    log.log(Level.INFO,"h");
                } catch (NullPointerException err) {}
//                try {
//                    (block)te.getWorld().getBlockState(te.getPos()).getBlock()
//                } catch (ClassCastException err) {}
//                GlStateManager.pushAttrib();
//                GlStateManager.disableColorMaterial();
//                GlStateManager.disableColorLogic();
//                bk1.readBlocks(te,te.getPos());
                block.TileEntityCustom tile=te;
//                try {
//                    tile=block.tes.get(block.tes.indexOf(te));
//                } catch (IndexOutOfBoundsException err) {}
//                bk1.readBlocks(tile,te.getPos());;
//                log.log(Level.INFO,"drawing.size()");
                ArrayList<smallUnit> drawing = new ArrayList<>();
                drawing=getUnits(te.getPos());
//                try {
//                    log.log(Level.INFO,drawing.size());
//                } catch (NullPointerException err) {}
//                for (int x2=0;x2<2;x2++) {
//                    for (int y2=0;y2<2;y2++) {
//                        for (int z2=0;z2<2;z2++) {
//                            try {
//                                log.log(Level.INFO,drawing.get(x2+y2+z2));
//                            } catch (NullPointerException err) {}
//                            log.log(Level.INFO,tile.getStackInSlot(x2+(y2*tile.getSize())+(z2*tile.getSize()*tile.getSize())));
//                        }
//                    }
//                }
//                if (toDraw.size()>=1) {
//                    drawing.clear();
//                    try {
//                        for (int i=0;i<toDraw.size();i++) {
//                            smallUnit su = toDraw.get(i);
//                            BlockPos bp = positions.get(i);
//                            if(te.getPos().getX()==su.pos.getX()&&te.getPos().getY()==su.pos.getY()&&te.getPos().getZ()==su.pos.getZ()) {
//                                if(te.getPos().getX()==bp.getX()&&te.getPos().getY()==bp.getY()&&te.getPos().getZ()==bp.getZ()) {
//                                    drawing.add(su);
//                                }
//                            }
//                        }
//                    } catch (ConcurrentModificationException err) {}
////                    drawing.clear();
////                    for (smallUnit su:bk1.SmallUnits) {
////                        drawing.add(su);
////                    }
//                }
//                log.log(Level.INFO,alpha);
                if (alpha>=1) {
                    if (true) {
                        IBakedModel mdl = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(net.minecraft.init.Blocks.BEDROCK.getDefaultState());
                        ResourceLocation txture = new ResourceLocation("smallerunits","textures/blocks/manual.png");
                        if (te.isManPlaced) {
                            txture=new ResourceLocation("smallerunits","textures/blocks/auto.png");
                        }
//                        NonDefinedModel mdl = new NonDefinedModel(0,0,0,1,1,1);
//                        bindTexture(txture);
//                        GlStateManager.pushMatrix();
//                        GlStateManager.translate(x,y,z);
//                        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//                        drawBreak(mdl,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(txture.toString()),0, net.minecraft.init.Blocks.BEDROCK);
//                        GlStateManager.popMatrix();
//                        mdl.render(1);
//                        texturelessModel rtmdl = new texturelessModel(mdl,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(txture.toString()));
//                        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),rtmdl);
                    }
                    try {
                        smallUnit sue=null;
                        BlockPos offset=null;
                        try {
                            sue=drawing.get(0);
                            offset=new BlockPos(sue.sc/2,128-sue.sc/2,sue.sc/2);
                            List<Entity> entities = wo.getEntitiesWithinAABB(Entity.class,new AxisAlignedBB(0,0,0,sue.sc,sue.sc,sue.sc));
                            for (Entity ent:entities) {
                                GlStateManager.pushMatrix();
                                GlStateManager.translate(x,y,z);
//                            Minecraft.getMinecraft().getRenderManager().renderEntityStatic(ent,partialTicks,true);
                                GlStateManager.popMatrix();
                            }
                        } catch (Exception err) {}
                        for (smallUnit su:drawing) {
                            BlockPos posInWo=su.sPos.add(offset);
                            IBlockState ste=wo.getBlockState(posInWo);
//                            wo.notifyBlockUpdate(posInWo,ste,ste,1);
//                            ste=wo.getBlockState(posInWo);
//                            log.log(Level.INFO,"h");
//                                    GlStateManager.enableBlend();
//                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                            GlStateManager.disableLighting();
                            if (debugger.collision) {
                                try {
                                    GlStateManager.enableBlend();
                                    GlStateManager.glLineWidth(8.0F);
                                    GlStateManager.disableTexture2D();
                                    GlStateManager.depthMask(true);
                                    ArrayList<AxisAlignedBB> boxes=new ArrayList<>();
                                    ArrayList<AxisAlignedBB> colliders=new ArrayList<>();
                                    try {
                                        su.bk.addCollisionBoxToList(ste.getActualState(wo,posInWo),wo,su.sPos,new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(su.sPos),boxes,null,true);
                                        if(!(su.bk.getRegistryName().toString().equals("minecraft:air"))&&
                                           !su.bk.equals(null)&&
                                           !su.bk.equals(net.minecraft.init.Blocks.AIR)&&
                                           su.pos.equals(te.getPos())) {
                                           colliders.add(ste.getActualState(wo,posInWo).getBoundingBox(wo,su.sPos));
                                        }
                                    } catch (IllegalArgumentException err) {} catch (NullPointerException err) {}
                                    for (AxisAlignedBB box:boxes) {
                                        AxisAlignedBB bb = box;
                                        AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
                                        bb1.offset(su.pos);
                                        EntityPlayer player=Minecraft.getMinecraft().player;
                                        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                                        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                                        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                                        Minecraft.getMinecraft().renderGlobal.drawSelectionBoundingBox(bb1.grow(0.000003).offset(su.pos).offset(-d3, -d4, -d5), 1f, 0f, 0f, 1f);
                                    }
                                    for (AxisAlignedBB box:colliders) {
                                        AxisAlignedBB bb = box.offset(su.sPos);
                                        AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
                                        bb1.offset(su.pos);
                                        GlStateManager.glLineWidth(6.0F);
                                        EntityPlayer player=Minecraft.getMinecraft().player;
                                        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                                        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                                        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                                        Minecraft.getMinecraft().renderGlobal.drawSelectionBoundingBox(bb1.grow(0.000000001).offset(su.pos).offset(-d3, -d4, -d5), (255f/255f), (127.5f/255f), 0f, 1f);
                                    }
                                    //                            Minecraft.getMinecraft().renderGlobal.drawSelectionBoundingBox(new AxisAlignedBB(0,1,0,1,2,1).grow(0.0020000000949949026D).offset(-d3, -d4, -d5), 1f, 0f, 0f, 0.4f);
                                    GlStateManager.depthMask(true);
                                    GlStateManager.enableTexture2D();
//                                GlStateManager.disableBlend();
                                } catch (Exception err) {}
                            }
                            if (debugger.selection) {
                                try {
                                    GlStateManager.enableBlend();
                                    GlStateManager.glLineWidth(4.0F);
                                    GlStateManager.disableTexture2D();
                                    GlStateManager.depthMask(true);
                                    ArrayList<AxisAlignedBB> boxes=new ArrayList<>();
                                    if(!(su.bk.getRegistryName().toString().equals("minecraft:air"))&&
                                       !su.bk.equals(null)&&
                                       !su.bk.equals(net.minecraft.init.Blocks.AIR)&&
                                       su.pos.equals(te.getPos())) {
                                       boxes.add(ste.getActualState(wo,posInWo).getBoundingBox(wo,posInWo).offset(su.sPos));
                                    }
                                    for (AxisAlignedBB box:boxes) {
                                        AxisAlignedBB bb = box;
                                        AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
                                        bb1.offset(su.pos);
                                        EntityPlayer player=Minecraft.getMinecraft().player;
                                        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                                        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                                        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                                        Minecraft.getMinecraft().renderGlobal.drawSelectionBoundingBox(bb1.grow(0.000006).offset(su.pos).offset(-d3, -d4, -d5), 0f, 1f, 0f, 1f);
                                    }
                                    GlStateManager.depthMask(true);
                                    GlStateManager.enableTexture2D();
                                } catch (Exception err) {}
                            }

                            GlStateManager.pushMatrix();
                            GlStateManager.enableAlpha();
                            GlStateManager.enableBlend();
                            GlStateManager.resetColor();
//                            RenderHelper.setColorBuffer(0,0,0,0);
                            GlStateManager.disableFog();
                            GlStateManager.disableNormalize();
                            GlStateManager.disableOutlineMode();
                            GlStateManager.enableDepth();
                            GlStateManager.depthMask(true);
//                            GlStateManager.disableLighting();
                            GlStateManager.disableColorLogic();
//                            GlStateManager.disableAlpha();
//                            GlStateManager.disableBlend();
//                            for (GlStateManager.SourceFactor ftr:GlStateManager.SourceFactor.values()) {
//                                try {
//                                    GlStateManager.blendFunc(ftr,GlStateManager.DestFactor.valueOf(ftr.name()));
//                                } catch (IllegalArgumentException err) {}
//                            }
//                            GlStateManager.blendFunc(GlStateManager.SourceFactor.ZERO,GlStateManager.DestFactor.ONE);
//                            GlStateManager.colorMaterial(0,0);
//                            GlStateManager.disableBlend();
//                            GlStateManager.alphaFunc(1, 0f);
//                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
                            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//                            GL14.glBlendEquation(GL14.GL_FUNC_ADD);
                            GlStateManager.enableBlend();
//                            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.CONSTANT_ALPHA);
                            GlStateManager.colorMask(true, true, true, true);
                            GlStateManager.enableAlpha();
//                            GlStateManager.disableAlpha();
                            RenderHelper.enableStandardItemLighting();
                            GlStateManager.disableLighting();
                            GlStateManager.enableRescaleNormal();
                            try {
                                GlStateManager.translate(x+(su.sPos.getX()+0.5f)/su.sc,y+(su.sPos.getY()+0.5f)/su.sc,z+(su.sPos.getZ()+0.5f)/su.sc);
                            } catch (NullPointerException err) {
                                drawing.clear();
                            }
                            GlStateManager.scale(1f/su.sc,1f/su.sc,1f/su.sc);
                            int tintF = te.getWorld().getBiome(te.getPos()).getFoliageColorAtPos(te.getPos());
                            tintF = te.getWorld().getBiome(te.getPos()).getModdedBiomeFoliageColor(tintF);
                            int tintW = te.getWorld().getBiome(te.getPos()).getWaterColor();
                            Block block = su.bk;
                            int meta = su.meta;
                            boolean drawItem = false;
                            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                            try {
                                if (!block.getRenderType(block.getStateFromMeta(meta)).equals(EnumBlockRenderType.INVISIBLE)&&
                                    !block.getRenderType(block.getStateFromMeta(meta)).equals(EnumBlockRenderType.ENTITYBLOCK_ANIMATED)&&
                                    !(block.getRegistryName().toString().equals("minecraft:air"))&&
                                    !block.equals(null)&&
                                    !block.equals(net.minecraft.init.Blocks.AIR)&&
                                    su.pos.equals(te.getPos())
                                ) {
                                    drawItem=true;
                                }
//                                try {
//                                    log.log(Level.INFO,"s");
//                                    BlockFluidRenderer bflr = new BlockFluidRenderer(Minecraft.getMinecraft().getBlockColors());
////                                    bflr.renderFluid(bk1.wo,su.bk.getStateFromMeta(meta),su.sPos,(BlockFluidBase)fl.getBlock());
//                                } catch (Exception err) {}
//                                if (block instanceof BlockLiquid) {
//                                    Fluid fl = FluidRegistry.getFluid(block.getRegistryName().getPath());
//                                    fluidModel mdl = new fluidModel(0,16,0,15,0,16,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fl.getBlock().getRegistryName().toString()),0);
//                                    drawModel(mdl,0,getWorld(),tile.getPos(),su.sc);
//                                    drawItem=true;
//                                }
                                IBakedModel mdl = null;
                                ItemStack stk = null;
                                int tint=-1;
                                try {
                                    mdl = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(ste.getActualState(wo,posInWo));
                                    stk = new ItemStack(net.minecraft.init.Blocks.TORCH);
                                } catch (IllegalArgumentException err) {} catch (NullPointerException err) {}
                                if (block instanceof BlockLiquid || block instanceof BlockFluidBase) {
                                    Fluid fl = FluidRegistry.lookupFluidForBlock(block);
                                    mdl = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(net.minecraft.init.Blocks.SNOW_LAYER.getStateFromMeta(14-meta));
//                                    log.log(Level.INFO,block.getStateFromMeta(meta).toString());
//                                    if (block.getStateFromMeta(meta).toString().contains("flowing")) {
//                                        mdl = new texturelessModel(mdl,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fl.getFlowing().toString()));
//                                    } else {
                                        mdl = new texturelessModel(mdl,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fl.getStill().toString()),0);
                                        if (fl.equals(FluidRegistry.WATER)) {
                                            tint=tintW;
                                            mdl = new texturelessModel(mdl,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fl.getStill().toString()),-1);
                                        }
//                                    }
//                                    try {
//                                        mdl = new ModelFluid(fl).bake(new IModelState() {
//                                            @Override
//                                            public Optional<TRSRTransformation> apply(Optional<? extends IModelPart> part) {
//                                                return part.map(new Function<IModelPart, TRSRTransformation>() {
//                                                    @Override
//                                                    public TRSRTransformation apply(IModelPart iModelPart) {
//                                                        return new TRSRTransformation(EnumFacing.SOUTH);
//                                                    }
//                                                });
//                                            }
//                                        }, DefaultVertexFormats.ITEM,
//                                        new Function<ResourceLocation, TextureAtlasSprite>() {
//                                            @Override
//                                            public TextureAtlasSprite apply(ResourceLocation resourceLocation) {
//                                                return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(resourceLocation.toString());
//                                            }
//                                        });
//                                    } catch (NullPointerException err) {
//                                    }
//                                    mdl = new texturelessModel(mdl,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fl.getStill().toString()));
//                                    mdl = new fluidModel(0,16,0,15,0,16,Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fl.getStill().toString()),0);
                                } else if (block instanceof BlockLeaves || block instanceof BlockBush || block instanceof BlockVine) {
                                    tint=tintF;
//                                    useWorld=true;
                                } else if (block instanceof BlockGrass) {
                                    int tintG = te.getWorld().getBiome(te.getPos()).getGrassColorAtPos(te.getPos());
                                    BlockPos posOff=new BlockPos((double)su.sPos.getX()/Math.abs(su.sPos.getX()),0,0);
                                    BlockPos posOff2=new BlockPos(-(double)(su.sc-su.sPos.getX())/Math.abs((su.sc-su.sPos.getX())),0,0);
                                    BlockPos posOff3=new BlockPos(0,0,-(double)(su.sc-su.sPos.getZ())/Math.abs((su.sc-su.sPos.getZ())));
                                    BlockPos posOff4=new BlockPos(0,0,(double)(su.sPos.getZ())/Math.abs(su.sPos.getZ()));
                                    int tintG2 = te.getWorld().getBiome(te.getPos().add(posOff)).getGrassColorAtPos(te.getPos().add(posOff));
                                    int tintG3 = te.getWorld().getBiome(te.getPos().add(posOff2)).getGrassColorAtPos(te.getPos().add(posOff2));
                                    int tintG4 = te.getWorld().getBiome(te.getPos().add(posOff3)).getGrassColorAtPos(te.getPos().add(posOff3));
                                    int tintG5 = te.getWorld().getBiome(te.getPos().add(posOff4)).getGrassColorAtPos(te.getPos().add(posOff4));
                                    tintG = te.getWorld().getBiome(te.getPos()).getModdedBiomeGrassColor(tintG);
                                    tintG2 = te.getWorld().getBiome(te.getPos().add(posOff)).getModdedBiomeGrassColor(tintG2);
                                    tintG3 = te.getWorld().getBiome(te.getPos().add(posOff2)).getModdedBiomeGrassColor(tintG3);
                                    tintG4 = te.getWorld().getBiome(te.getPos().add(posOff3)).getModdedBiomeGrassColor(tintG4);
                                    tintG5 = te.getWorld().getBiome(te.getPos().add(posOff4)).getModdedBiomeGrassColor(tintG5);
                                    Color tint1=new Color(tintG);
                                    Color tint2=new Color(tintG2);
                                    double dist = new BlockPos(su.sPos.getX(),0,0).distanceSq(new BlockPos(su.sc,0,0));
                                    dist*=dist;
                                    try {
//                                int num=(int)(su.sc*3);
                                        tintG=new Color(
                                                (int)(((tint1.getRed()*Math.sqrt(dist))+tint2.getRed()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getGreen()*Math.sqrt(dist))+tint2.getGreen()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getBlue()*Math.sqrt(dist))+tint2.getBlue()*1)/(Math.sqrt(dist)+1))
                                        ).getRGB();
                                    } catch (Exception err) {}
                                    tint2=new Color(tintG3);
                                    dist=new BlockPos(su.sPos.getX(),0,0).distanceSq(new BlockPos(-1,0,0));
                                    dist*=dist;
                                    tint1=new Color(tintG);
                                    try {
//                                int num=(int)(su.sc*3);
                                        tintG=new Color(
                                                (int)(((tint1.getRed()*Math.sqrt(dist))+tint2.getRed()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getGreen()*Math.sqrt(dist))+tint2.getGreen()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getBlue()*Math.sqrt(dist))+tint2.getBlue()*1)/(Math.sqrt(dist)+1))
                                        ).getRGB();
                                    } catch (Exception err) {}
                                    tint2=new Color(tintG4);
                                    dist=new BlockPos(0,0,su.sPos.getZ()).distanceSq(new BlockPos(0,0,-1));
                                    dist*=dist;
                                    tint1=new Color(tintG);
                                    try {
//                                int num=(int)(su.sc*3);
                                        tintG=new Color(
                                                (int)(((tint1.getRed()*Math.sqrt(dist))+tint2.getRed()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getGreen()*Math.sqrt(dist))+tint2.getGreen()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getBlue()*Math.sqrt(dist))+tint2.getBlue()*1)/(Math.sqrt(dist)+1))
                                        ).getRGB();
                                    } catch (Exception err) {}
                                    tint2=new Color(tintG5);
                                    dist=new BlockPos(0,0,su.sPos.getZ()).distanceSq(new BlockPos(0,0,su.sc));
                                    dist*=dist;
                                    tint1=new Color(tintG);
                                    try {
//                                int num=(int)(su.sc*3);
                                        tintG=new Color(
                                                (int)(((tint1.getRed()*Math.sqrt(dist))+tint2.getRed()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getGreen()*Math.sqrt(dist))+tint2.getGreen()*1)/(Math.sqrt(dist)+1)),
                                                (int)(((tint1.getBlue()*Math.sqrt(dist))+tint2.getBlue()*1)/(Math.sqrt(dist)+1))
                                        ).getRGB();
                                    } catch (Exception err) {}
                                    tint=tintG;
//                                    tint=0;
//                                    useWorld=true;
                                } else if (block instanceof BlockRedstoneWire) {
                                    tint=new Color((meta*10)+75,0,0).getRGB();
                                }
//                                GlStateManager.disableLighting();
                                if  (tint==-1) {
                                    GlStateManager.enableLighting();
                                    RenderHelper.enableStandardItemLighting();
//                                    RenderHelper.enableGUIStandardItemLighting();
//                                    RenderHelper.enableGUIStandardItemLighting();
                                }
//                                if (block.getRenderType(block.getStateFromMeta(meta)).equals(EnumBlockRenderType.LIQUID)) {
//                                    int[] points = new int[] {
//                                            0,0,0,
//                                            1,0,0,
//                                            1,1,0,
//                                            0,1,0
//                                    };
//                                    mdl=new modelBakery(new BakedQuad(points, biomeColor.getRGB(), EnumFacing.NORTH, Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(block.getRegistryName().getNamespace()+":"+block.getRegistryName().getPath())));
//                                }
                                if (block.hasTileEntity()) {
                                    try {
                                        if (drawItem) {
                                            drawModel(mdl,tint,meta,block,getWorld(),tile.getPos(),su.sc,wo,posInWo);
                                        }
                                        try {
                                            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                                            TileEntityRendererDispatcher.instance.getRenderer(block.createTileEntity(wo,ste)).render(block.createTileEntity(wo,ste),(su.sPos.getX())/te.getSize()-0.5,(su.sPos.getY())/te.getSize()-0.5,(su.sPos.getZ())/te.getSize()-0.5,partialTicks,destroyStage,alpha);
                                            GlStateManager.enableTexture2D();
//                                            GlStateManager.enableLighting();
                                            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                                            GlStateManager.depthMask(true);
                                        } catch (RuntimeException err) {}
                                    } catch (NullPointerException err) {
                                        try {
                                            if (drawItem) {
                                                drawModel(mdl,tint,meta,block,getWorld(),tile.getPos(),su.sc,wo,posInWo);
                                            }
                                        } catch (IllegalArgumentException err2) {
                                            if (drawItem) {
                                                drawModel(mdl,tint,meta,block,getWorld(),tile.getPos(),su.sc,wo,posInWo);
                                            }
                                        }
                                    }
                                } else {
                                    try {
                                        if (drawItem) {
                                            drawModel(mdl,tint,meta,block,getWorld(),tile.getPos(),su.sc,wo,posInWo);
                                        }
                                    } catch (IllegalArgumentException err) {
                                        if (drawItem) {
                                            drawModel(mdl,tint,meta,block,getWorld(),tile.getPos(),su.sc,wo,posInWo);
//                                            Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),mdl);
                                        }
                                    }
                                }
                            } catch (IllegalArgumentException err) {
                                if (drawItem) {
                                    Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(block.getStateFromMeta(0)));
                                }
                            }
                            if (bk1.subPos.toString().equals(su.sPos.toString())) {
//                                log.log(Level.INFO,bk1.subPos+","+su.sPos);
//                                log.log(Level.INFO,bk1.subPos.toString().equals(su.sPos.toString()));
//                                log.log(Level.INFO,destroyStage);
                                if (drawItem) {
                                    if (destroyStage <= 0) {
                                    ResourceLocation destroy=null;
                                    try {
                                        destroy=(DESTROY_STAGES[destroyStage]);
                                    } catch (IndexOutOfBoundsException err) {
                                        destroy=(new ResourceLocation("textures/blocks/ice.png"));
                                    }
//                                        drawBreak(Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(block.getStateFromMeta(meta)),Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(destroy.toString()),meta,block);
//                                        texturelessModel rtmdl = new texturelessModel(Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(block.getStateFromMeta(meta)),Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(destroy.toString()));
//                                        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),rtmdl);
                                    }
                                }
                            }
                            GlStateManager.popMatrix();
                        }
                    } catch (ConcurrentModificationException err) {} catch (StackOverflowError err) {} catch (NullPointerException err) {}

                }
//                packet.isReading=false;
//                if (partialTicks>=0.5) {
//                    incRefInd=false;
////                    log.log(Level.INFO,"pt:"+partialTicks);
//                }
            }
        });
    }

    public void drawModel(IBakedModel mdl,int tint,int meta,Block block,World wo,BlockPos pos,double scale,world wo2,BlockPos posInWo) {
        EnumFacing[] facings = new EnumFacing[7];
        facings[0]=EnumFacing.WEST;
        facings[1]=EnumFacing.NORTH;
        facings[2]=EnumFacing.EAST;
        facings[3]=EnumFacing.SOUTH;
        facings[4]=EnumFacing.UP;
        facings[5]=EnumFacing.DOWN;
        facings[6]=null;
        RenderItem ri=Minecraft.getMinecraft().getRenderItem();
//        ri.renderItem(new ItemStack(block,1),mdl);
        for (EnumFacing facing:facings)
            for (BakedQuad qd:mdl.getQuads(block.getStateFromMeta(meta).getActualState(wo2,posInWo),facing,pos.toLong())) {
//                log.log(Level.INFO,qd.getTintIndex());
                modelBakery bkry = new modelBakery(qd);
                ItemStack stack = new ItemStack(Item.getItemFromBlock(net.minecraft.init.Blocks.OBSIDIAN),1);
                if (!stack.isEmpty()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                    if (bkry.isBuiltInRenderer()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                        GlStateManager.disableRescaleNormal();
                        stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                    } else {
                        boolean drawNorm=true;
                        if (net.minecraftforge.common.ForgeModContainer.allowEmissiveItems) {
                            net.minecraftforge.client.ForgeHooksClient.renderLitItem(ri, bkry, tint, stack);
//                            drawNorm=false;
                        }
                        if (drawNorm) {
                            Tessellator tessellator = Tessellator.getInstance();
                            BufferBuilder bufferbuilder = tessellator.getBuffer();
                            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

                            int tintDraw=-1;
                            if (qd.getTintIndex()!=-1) {
                                tintDraw=tint;
                            }
                            if (qd.getTintIndex()==0) {
                                tintDraw=Minecraft.getMinecraft().getBlockColors().colorMultiplier(block.getStateFromMeta(meta), wo, pos, qd.getTintIndex());
//                                int tintDraw2=Minecraft.getMinecraft().getBlockColors().colorMultiplier(block.getStateFromMeta(meta), wo, pos.add(new BlockPos(1,1,1)), qd.getTintIndex());
//                                tintDraw=(int)((tintDraw*scale+tintDraw2*(16-scale))/scale);
                            }
                            ri.renderQuads(bufferbuilder, bkry.getQuads(null, null, 0L), tintDraw, stack);
                            tessellator.draw();
                        }
                    }
                    GlStateManager.popMatrix();
                }
            }
//        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),mdl);
    }
    public void drawModel(IBakedModel mdl,int tint,World wo,BlockPos pos,double scale) {
        EnumFacing[] facings = new EnumFacing[7];
        facings[0]=EnumFacing.WEST;
        facings[1]=EnumFacing.NORTH;
        facings[2]=EnumFacing.EAST;
        facings[3]=EnumFacing.SOUTH;
        facings[4]=EnumFacing.UP;
        facings[5]=EnumFacing.DOWN;
        facings[6]=null;
        RenderItem ri=Minecraft.getMinecraft().getRenderItem();
        for (EnumFacing facing:facings)
            for (BakedQuad qd:mdl.getQuads(null,null,0)) {
//                log.log(Level.INFO,qd.getTintIndex());
                modelBakery bkry = new modelBakery(qd,tint);
                ItemStack stack = new ItemStack(Item.getItemFromBlock(net.minecraft.init.Blocks.OBSIDIAN),1);
                if (!stack.isEmpty()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                    if (bkry.isBuiltInRenderer()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.enableRescaleNormal();
                        stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
                    } else {
                        if (net.minecraftforge.common.ForgeModContainer.allowEmissiveItems) {
                            try {
                                net.minecraftforge.client.ForgeHooksClient.renderLitItem(ri, bkry, tint, stack);
                            } catch (Exception err) {}
//                            return;
                        }
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferbuilder = tessellator.getBuffer();
                        try {
                            bufferbuilder.begin(7, DefaultVertexFormats.ITEM);
                        } catch (IllegalStateException err) {}
                        GlStateManager.enableAlpha();
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE,GlStateManager.DestFactor.ZERO);

                        int tintDraw=-1;
                        if (qd.getTintIndex()!=-1) {
                            tintDraw=tint;
//                            tintDraw=Minecraft.getMinecraft().getBlockColors().colorMultiplier(null, wo, pos, qd.getTintIndex());
                        }
//                        for (EnumFacing enumfacing : EnumFacing.values()) {
//                            ri.renderQuads(bufferbuilder, bkry.getQuads(null, enumfacing, 0L), tintDraw, stack);
//                        }

                        try {
                            ri.renderQuads(bufferbuilder, bkry.getQuads(null, null, 0L), tintDraw, stack);
                        } catch (ArrayIndexOutOfBoundsException err) {}
                        tessellator.draw();
                    }
                    GlStateManager.popMatrix();
                }
            }
//        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),mdl);
    }
    public void drawBreak(IBakedModel mdl, TextureAtlasSprite sprite, int meta, Block block) {
//        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),mdl);
//        for (EnumFacing facing:EnumFacing.values()) {
            for (BakedQuad qd:mdl.getQuads(block.getStateFromMeta(meta),null,0L)) {
                modelBakery bkry = new modelBakery(qd,sprite);
                Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(net.minecraft.init.Blocks.TORCH),bkry);
            }
//        }
    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {
      /*
          public static final MySpecialBlock mySpecialBlock = null; // placeholder for special block below
      */
    }

    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items {
      /*
          public static final ItemBlock mySpecialBlock = null; // itemblock for the block above
          public static final MySpecialItem mySpecialItem = null; // placeholder for special item below
      */
    }

    public static class tab extends CreativeTabs {
        Block[] blocks;
        public tab(String label, Block[] blocks) {
            super(label);
            this.blocks=blocks;
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(new ItemCoal(),0);
        }

        int lastRefresh=0;
        int lastIndex=0;
        @Override
        public ItemStack getIcon() {
//            blocks = Iterators.toArray(Block.REGISTRY.iterator(), Block.class);
            Random rand = new Random(lastIndex*lastRefresh);
            return new ItemStack(Item.getItemFromBlock(blocks[rand.nextInt(blocks.length)]),1);
        }

    }
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        block.registerModels(event);
    }

    public static ArrayList<ItemStack> subs=new ArrayList<>();
    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    public static CreativeTabs tabSubs;
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        /**
         * Listen for the register event for creating custom items
         */
        public static Block bk = new block(Material.GROUND, MapColor.DIRT);
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
//            if (event.getPhase().equals(EventPriority.LOW)) {
//            CreativeTabs tab = new tab("Placeholders",Iterators.toArray(Block.REGISTRY.iterator(), Block.class));
            tabSubs = new tab("Small Units",Iterators.toArray(Block.REGISTRY.iterator(), Block.class));
                event.getRegistry().register(new ItemPlacer(bk){
                    CreativeTabs tab;
                    @Override
                    protected boolean isInCreativeTab(CreativeTabs targetTab) {
                        return targetTab.equals(tab);
                    }
                    @Override
                    public Item setCreativeTab(CreativeTabs tab) {
                        this.tab=tab;
                        return super.setCreativeTab(tab);
                    }
//                    @Override
//                    public void setTileEntityItemStackRenderer(@Nullable TileEntityItemStackRenderer teisr) {
////                        super.setTileEntityItemStackRenderer(teisr);
//                        super.setTileEntityItemStackRenderer(new TileEntityItemStackRenderer(){
//                            @Override
//                            public void renderByItem(ItemStack itemStackIn) {
////                                for (itemStackIn.getTagCompound())
//                                test.test.block.TileEntityCustom te = (test.test.block.TileEntityCustom)bk.createTileEntity(null,null);
//                                te.readFromNBT(itemStackIn.getSubCompound("BlockEntityTag"));
//                                TileEntityRendererDispatcher.instance.render(te,0,0,0,0);
////                                super.renderByItem(itemStackIn);
//                            }
//                        });
//                    }

                    @Override
                    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
                        if (tab.equals(this.tab)) {
                            for (ItemStack sub:subs) {
                                try {
                                    //                                    log.log(Level.INFO,sub.getTagCompound());
                                    String[] itemsContents = sub.getTagCompound().toString().split("b,id:\"",20000);
                                    String name="";
                                    for (String strung:itemsContents) {
                                        String stringed="";
                                        //                                        log.log(Level.INFO,strung);
                                        boolean bool=true;
                                        if (!strung.contains("BlockEntityTag")) {
                                            //                                    strung=strung.substring("b,id:\"".length());
                                            for (int i=0;i<strung.length()&&bool;i+=1) {
                                                if (strung.charAt(i)=='\"') {
                                                    bool=false;
                                                } else {
                                                    stringed+=strung.charAt(i);
                                                }
                                            }
                                            name+=stringed+", ";
                                            //                                            log.log(Level.INFO,name);
                                        }
                                    }
                                    sub.setStackDisplayName(name);
                                    items.add(sub);
                                } catch (NullPointerException err) {}
                            }
                            super.getSubItems(tab, items);
                        }
                    }
                    @Override
                    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
                        if  (worldIn.isRemote) {
                            if (!subs.contains(player.getHeldItem(hand))) {
                                subs.add(player.getHeldItem(hand));
                            }
                        }
                        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                    }
                }.setCreativeTab(tabSubs).setRegistryName("smallUnits:su"));
//                Block[] blocks = Iterators.toArray(Block.REGISTRY.iterator(), Block.class);
//                for (Block block:blocks) {
//                    log.log(Level.INFO, "block:"+block.getRegistryName());
//                    Item item = Item.getItemFromBlock(block);
//                    log.log(Level.INFO, "block has item");
//                    if ((item instanceof ItemAir)&&(block.getIdFromBlock(block)!=0)) {
//                        event.getRegistry().register(new ItemBlock(block){
//                            CreativeTabs tab;
//                            @Override
//                            protected boolean isInCreativeTab(CreativeTabs targetTab) {
//                                if (targetTab.equals(tab)&&this.getRegistryName().getNamespace().equals("placeholder")) {
//                                    return true;
//                                } else {
//                                    return super.isInCreativeTab(targetTab);
//                                }
//                            }
//
//                            @Override
//                            public Item setCreativeTab(CreativeTabs tab) {
//                                this.tab=tab;
//                                return super.setCreativeTab(tab);
//                            }
//                        }.setTranslationKey("placeholder."+block.getLocalizedName()).setRegistryName("placeholder:"+block.getRegistryName().getNamespace()+"_"+block.getRegistryName().getPath()).setCreativeTab(tab));
//                        log.log(Level.INFO, "block needed item");
//                    }
//                }
//            }
           /*
             event.getRegistry().register(new ItemBlock(Blocks.myBlock).setRegistryName(MOD_ID, "myBlock"));
             event.getRegistry().register(new MySpecialItem().setRegistryName(MOD_ID, "mySpecialItem"));
            */
        }

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(bk);
           /*
             event.getRegistry().register(new MySpecialBlock().setRegistryName(MOD_ID, "mySpecialBlock"));
            */
        }
    }
    /* EXAMPLE ITEM AND BLOCK - you probably want these in separate files
    public static class MySpecialItem extends Item {

    }

    public static class MySpecialBlock extends Block {

    }
    */
}
