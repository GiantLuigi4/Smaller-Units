package test.test;

import jdk.nashorn.internal.objects.annotations.Getter;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;

@net.minecraftforge.common.config.Config(modid = SmallerUnitsMod.MOD_ID)
public class Config {

    @net.minecraftforge.common.config.Config.Name("General Config")
    @net.minecraftforge.common.config.Config.LangKey("a")
    @net.minecraftforge.common.config.Config.Comment("This is a comment")
    public static String comment1="";

    @net.minecraftforge.common.config.Config.Name("Scale Min")
    @net.minecraftforge.common.config.Config.LangKey("ab")
    @net.minecraftforge.common.config.Config.Comment("§2The smallest size you can make a unit.\n§cFor example, 4 would be 1/4.\nDefault:2")
    @net.minecraftforge.common.config.Config.SlidingOption
    @net.minecraftforge.common.config.Config.RangeInt(min=1,max=16)
    public static int scaleMin=2;

    @net.minecraftforge.common.config.Config.Name("Scale Max")
    @net.minecraftforge.common.config.Config.LangKey("b")
    @net.minecraftforge.common.config.Config.Comment("§2The largest size you can make a unit.\n§cFor example, 4 would be 1/4.\nShould be a larger number than the min size.\nDefault:8")
    @net.minecraftforge.common.config.Config.SlidingOption
    @net.minecraftforge.common.config.Config.RangeInt(min=1,max=16)
    public static int scaleMax=8;

    @net.minecraftforge.common.config.Config.Name("3D GUI Config")
    @net.minecraftforge.common.config.Config.LangKey("c")
    @net.minecraftforge.common.config.Config.Comment("This is a comment")
    public static String comment2="";

//    @net.minecraftforge.common.config.Config.Name("GUI Precision§4¶@")
//    @net.minecraftforge.common.config.Config.LangKey("cb")
//    @net.minecraftforge.common.config.Config.Comment("§2This changes the quality of the GUI\n§2Lower numbers mean higher quality, §4but lower framerate.\nDefault:0.01")
//    @net.minecraftforge.common.config.Config.SlidingOption
//    @net.minecraftforge.common.config.Config.RangeDouble(min=0.01,max=0.1)
//    public static double GUIQual=0.01;

    @net.minecraftforge.common.config.Config.Name("Font Quality")
    @net.minecraftforge.common.config.Config.LangKey("d")
    @net.minecraftforge.common.config.Config.Comment("§2This changes the quality of the font\n§2Higher numbers mean higher quality, §4but lower framerate.\nDefault:1")
    @net.minecraftforge.common.config.Config.SlidingOption
    @net.minecraftforge.common.config.Config.RangeDouble(min=1,max=8)
    public static double FontQualit=1;

    @net.minecraftforge.common.config.Config.Name("General GUI Config")
    @net.minecraftforge.common.config.Config.LangKey("e")
    @net.minecraftforge.common.config.Config.Comment("This is a comment")
    public static String comment3="";

    @net.minecraftforge.common.config.Config.Name("storage")
    @net.minecraftforge.common.config.Config.LangKey("zzzzzzzzzzzzzz")
    @net.minecraftforge.common.config.Config.Comment("§This is to store values set in game.")
    public static dataStorage strg=new dataStorage();

    public static class dataStorage {
        public int[] colorbg = colorListHelper.genList(Color.LIGHT_GRAY);
        public int[] colortext = colorListHelper.genList(Color.BLACK);
        public int[] colorframe = colorListHelper.genList(Color.GRAY);
        public int[] colorsliderbg = colorListHelper.genList(Color.DARK_GRAY);
        public int[] colorsliderframe = colorListHelper.genList(Color.BLACK);
        public int[] colorslidercollision = colorListHelper.genList(Color.LIGHT_GRAY);
        public int[] colorslidercollisionframe = colorListHelper.genList(Color.GRAY);
    }
    public static class colorListHelper {
        public static int[] genList(Color col1) {
            return new int[]{col1.getRed(),col1.getGreen(),col1.getBlue()};
        }
        public static Color getColor(int[] ints) {
            return new Color(ints[0],ints[1],ints[2]);
        }
    }

    public static class configChangeListener {
        @SubscribeEvent
        public void changeConfig(ConfigChangedEvent event) {
            SmallerUnitsMod.log.log(Level.INFO,event);
            if (event.getModID().equals(SmallerUnitsMod.MOD_ID)) {
                SmallerUnitsMod.log.log(Level.INFO,scaleMin+","+scaleMin);
                ConfigManager.sync(event.getModID(), net.minecraftforge.common.config.Config.Type.INSTANCE);
//                SmallerUnitsMod.log.log(Level.INFO,event.getConfigID());
            }
        }
    }
}
