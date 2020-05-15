package test.test;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ICrashCallable;

//https://github.com/Dyonovan/CrashLogAdditions/blob/master/src/main/java/com/dyonovan/crashlogadditions/CLACrashCallable.java
public class crashAddition implements ICrashCallable {
    public static void create() {
        FMLCommonHandler.instance().registerCrashCallable(new crashAddition());
    }

    @Override
    public String getLabel() {
    return "Smaller Units is present";
    }

    @Override
    public String call() throws Exception {
        return "Please report it here:https://www.curseforge.com/minecraft/mc-mods/smaller-units/issues";
    }
}
