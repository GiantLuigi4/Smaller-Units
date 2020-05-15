package test.test;

import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;

public class dataHandler {

    public static boolean isReading = false;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent evt) {
//        evt.setMessage(new TextComponentString(evt.getMessage().getFormattedText()+"§4|"+evt.getMessage().getSiblings().get(0)));
////        Test.log.log(Level.INFO,evt.getMessage().getUnformattedText());
//        String msg = evt.getMessage().getUnformattedText();
////        evt.setCanceled(true);
//        String[] msgParts=msg.split("≈",120);
//        int x=0;
//        int sx=0;
//        int y=0;
//        int sy=0;
//        int z=0;
//        int sz=0;
//        int meta=0;
//        int sc=0;
//        Block block=null;
//        for (String msgPart:msgParts) {
////            Test.log.log(Level.INFO,msgPart);
//            if(msgPart.startsWith("block")) {
//                block = Block.getBlockFromName(msgPart.substring(("block").length(),msgPart.length()));
//            } else if (msgPart.startsWith("posX")) {
//                x = Integer.parseInt(msgPart.substring(("posX").length(),msgPart.length()));
//            } else if (msgPart.startsWith("posY")) {
//                y = Integer.parseInt(msgPart.substring(("posY").length(),msgPart.length()));
//            } else if (msgPart.startsWith("posZ")) {
//                z = Integer.parseInt(msgPart.substring(("posZ").length(),msgPart.length()));
//            } else if (msgPart.startsWith("sPosX")) {
//                sx = Integer.parseInt(msgPart.substring(("sPosX").length(),msgPart.length()));
//            } else if (msgPart.startsWith("sPosY")) {
//                sy = Integer.parseInt(msgPart.substring(("sPosY").length(),msgPart.length()));
//            } else if (msgPart.startsWith("sPosZ")) {
//                sz = Integer.parseInt(msgPart.substring(("sPosZ").length(),msgPart.length()));
//            } else if (msgPart.startsWith("scale")) {
//                sc = Integer.parseInt(msgPart.substring(("scale").length(),msgPart.length()));
//            } else if (msgPart.startsWith("meta")) {
//                meta = Integer.parseInt(msgPart.substring(("meta").length(),msgPart.length()));
//            } else if (msgPart.startsWith("done")) {
//                smallUnit unit = new smallUnit(block,sc,meta,new BlockPos(x,y,z),new BlockPos(sx,sy,sz));
//                if (!isReading) {
//                    int i= SmallerUnitsMod.toDraw.indexOf(unit);
//                    if (!SmallerUnitsMod.toDraw.contains(unit)) {
//                        SmallerUnitsMod.log.log(Level.INFO,"done");
//                        SmallerUnitsMod.toDraw.add(unit);
//                        SmallerUnitsMod.positions.add(unit.pos);
//                    } else {
//                        SmallerUnitsMod.toDraw.set(i,unit);
//                        SmallerUnitsMod.positions.set(i,unit.pos);
//                    }
////                    if (block.equals("minecraft:air")) {
////                        Test.toDraw.remove(i);
////                        Test.positions.remove(i);
////                    }
//                }
//                evt.setCanceled(true);
//            }
//        }
    }
    public static ArrayList<placeEvent> placeEvents = new ArrayList<>();
    @SubscribeEvent
    public void onChat(ServerChatEvent evt) {
//        evt.setComponent(new TextComponentString(evt.getComponent().getFormattedText()+"text"));
//        String msg = evt.getMessage();
//        String[] msgParts=msg.split("≈",120);
//        String bp="";
//        String item="";
//        int meta=0;
//        int slot=0;
//        for (String msgPart:msgParts) {
//            SmallerUnitsMod.log.log(Level.INFO,msgPart);
//            if (msgPart.startsWith("item")) {
//                item=(msgPart.substring(("item").length(),msgPart.length()));
//                evt.setCanceled(true);
//            } else if (msgPart.startsWith("meta")) {
//                meta = Integer.parseInt(msgPart.substring(("meta").length(),msgPart.length()));
//            } else if (msgPart.startsWith("bp")) {
//                bp = (msgPart.substring(("bp").length(),msgPart.length()));
//            } else if (msgPart.startsWith("slot")) {
//                slot = Integer.parseInt(msgPart.substring(("slot").length(),msgPart.length()));
//            }
//        }
//        placeEvents.add(new placeEvent(bp,item,meta,slot));
    }

//    @SubscribeEvent
//    public void BreakBlock(BlockEvent.BreakEvent evt) {
//        try {
//            evt.setCanceled(((block.TileEntityCustom)evt.getWorld().getTileEntity(evt.getPos())).isEmpty());
//        } catch (ClassCastException err) {} catch (NullPointerException err) {}
//    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (!FMLClientHandler.instance().isGUIOpen(GuiScreen.class)) {
            if (org.lwjgl.input.Keyboard.isKeyDown(SmallerUnitsMod.debugCollision.getKeyCode())) {
                SmallerUnitsMod.debugger.collision=!SmallerUnitsMod.debugger.collision;
            }
            if (org.lwjgl.input.Keyboard.isKeyDown(SmallerUnitsMod.debugSelection.getKeyCode())) {
                SmallerUnitsMod.debugger.selection=!SmallerUnitsMod.debugger.selection;
            }
        }
    }

    @SubscribeEvent
    public void onGUIOpenEvent(GuiOpenEvent evt) {
        SmallerUnitsMod.log.log(Level.INFO,evt.getGui());
        if (evt.getGui() instanceof GuiConfig) {
            if (!(evt.getGui() instanceof ConfigGuiOverride)) {
                if (((GuiConfig)evt.getGui()).modID.equals(SmallerUnitsMod.MOD_ID)) {
                    evt.setGui(new ConfigGuiOverride((GuiConfig)evt.getGui()));
                }
            }
        }
    }
}
