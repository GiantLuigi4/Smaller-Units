package tfc.smallerunits.utils.compat.vr.vivecraft;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static tfc.smallerunits.Smallerunits.LOGGER;

public class MinecriftDetector {
	public static boolean testClient() {
		boolean isVivecraftPresent = false;
		if (ClientBrandRetriever.getClientModName().equals("vivecraft")) {
			boolean vivecraftPresence = false;
			
			try {
				Class<?> clazz = Class.forName("org.vivecraft.api.VRData");
				if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
				Method m = clazz.getMethod("getController", int.class);
				if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new ReflectiveOperationException("disable");
				
				clazz = Class.forName("org.vivecraft.api.VRData$VRDevicePose");
				if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
				m = clazz.getMethod("getPosition");
				if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new ReflectiveOperationException("disable");
				m = clazz.getMethod("getDirection");
				if (!(!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new ReflectiveOperationException("disable");
				
				clazz = Class.forName("org.vivecraft.gameplay.VRPlayer");
				if (!Modifier.isPublic(clazz.getModifiers())) throw new RuntimeException("disable");
				m = clazz.getMethod("get");
				if (!(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())))
					throw new ReflectiveOperationException("disable");
				Field f = clazz.getField("vrdata_world_render");
				if (!(Modifier.isPublic(f.getModifiers()))) throw new RuntimeException("disable");
				
				LOGGER.info("Vivecraft detected; enabling support");
				vivecraftPresence = true;
				
				ViveSettings.init();
			} catch (ReflectiveOperationException err) {
				err.printStackTrace();
				LOGGER.warn("Vivecraft detected; however, the version of vivecraft which is present does not match with what smaller units expects");
				
				String detectedVivecraftVersion = "null";
				try {
					Field f = Minecraft.class.getField("minecriftVerString");
					detectedVivecraftVersion = (String) f.get(Minecraft.getInstance());
				} catch (Throwable ignored) {
				}
				LOGGER.warn("Found: " + detectedVivecraftVersion + ", Expected: " + "Vivecraft 1.16.5 jrbudda-7-5 1.16.5");
				ModLoader.get().addWarning(
						new ModLoadingWarning(
								ModLoadingContext.get().getActiveContainer().getModInfo(),
								ModLoadingStage.CONSTRUCT, "smallerunits.vivecraft.support.version.error"
						)
				);
			}
			isVivecraftPresent = vivecraftPresence;
		}
		return isVivecraftPresent;
	}
}
