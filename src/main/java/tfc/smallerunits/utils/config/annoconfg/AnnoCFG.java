package tfc.smallerunits.utils.config.annoconfg;

import tfc.smallerunits.utils.config.annoconfg.annotation.format.CFGSegment;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.Config;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.Name;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.Skip;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.Default;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.DoubleRange;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.IntRange;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.LongRange;
import tfc.smallerunits.utils.config.annoconfg.builder.CategoryBuilder;
import tfc.smallerunits.utils.config.annoconfg.builder.CfgBuilder;
import tfc.smallerunits.utils.config.annoconfg.handle.UnsafeHandle;
import tfc.smallerunits.utils.config.annoconfg.util.EnumType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

// TODO: finish this
public class AnnoCFG {
	private final HashMap<String, ConfigEntry> handles = new HashMap<>();
	
	private static final ArrayList<AnnoCFG> configs = new ArrayList<>();
	
	tfc.smallerunits.utils.config.annoconfg.Config internal;
	
	public AnnoCFG(Class<?> clazz) {
		Config configDescriptor = clazz.getAnnotation(Config.class);
		
		String translationRoot = configDescriptor.path().replace("/", "_") + configDescriptor.namespace() + ":" + configDescriptor.type().name().toLowerCase();
		
		CfgBuilder builder = CfgBuilder.automatic(translationRoot);
		builder.setSaveFunction(this::onConfigChange);
		
		CategoryBuilder category = builder.rootCategory();
		setup(translationRoot, "", builder, category, clazz);
		configs.add(this);
		internal = builder.build();
		
		String pth = configDescriptor.path();
		if (!pth.isEmpty()) pth = pth + "/";
		switch (configDescriptor.type()) {
			case SERVER -> create(configDescriptor.type(), pth + configDescriptor.namespace() + "_server.json");
			case CLIENT -> create(configDescriptor.type(), pth + configDescriptor.namespace() + "_client.json");
			case COMMON -> create(configDescriptor.type(), pth + configDescriptor.namespace() + "_common.json");
			default -> throw new RuntimeException("wat");
		}
	}
	
	public void setup(String translationRoot, String dir, CfgBuilder builder, CategoryBuilder category, Class<?> clazz) {
		if (dir.startsWith(".")) dir = dir.substring(1);
		
		for (Field field : clazz.getFields()) {
			if (field.canAccess(null)) {
				Skip skip = field.getAnnotation(Skip.class);
				if (skip != null) continue;
				
				Name name = field.getAnnotation(Name.class);
				
				String nameStr = field.getName();
				if (name != null) nameStr = name.value();
				
				String translationName = translationRoot + "/" + dir.replace(".", "/") + "/" + nameStr.toLowerCase();
				
				Supplier<?> value;
				
				Default defaultValue = field.getAnnotation(Default.class);
				switch (EnumType.forClass(field.getType())) {
					case INT -> {
						IntRange range = field.getAnnotation(IntRange.class);
						int v = defaultValue.valueI();
						if (range != null) {
							int min = range.minV();
							int max = range.maxV();
							
							value = category.intRange(translationName, min, v, max);
						} else {
							value = category.intValue(translationName, v);
						}
					}
					case LONG -> {
						LongRange range = field.getAnnotation(LongRange.class);
						long v = defaultValue.valueL();
						if (range != null) {
							long min = range.minV();
							long max = range.maxV();
							
							value = category.longRange(translationName, min, v, max);
						} else {
							value = category.longValue(translationName, v);
						}
					}
					case DOUBLE -> {
						DoubleRange range = field.getAnnotation(DoubleRange.class);
						double v = defaultValue.valueD();
						if (range != null) {
							double min = range.minV();
							double max = range.maxV();
							
							value = category.doubleRange(translationName, min, v, max);
						} else {
							value = category.doubleValue(translationName, v);
						}
					}
					case BOOLEAN -> {
						value = category.boolValue(translationName, defaultValue.valueBoolean());
					}
					default -> throw new RuntimeException("Invalid config entry (type: " + EnumType.forClass(clazz) + "): " + nameStr + " in " + clazz.getName());
				}
				
				Object o = null;
				try {
					o = field.get(null);
				} catch (Throwable ignored) {
				}
				UnsafeHandle handle = new UnsafeHandle(field);
				o = handle.get();
				handle.set(o);
				
				//noinspection FunctionalExpressionCanBeFolded
				handles.put(dir + "." + nameStr, new ConfigEntry(
						handle, value::get
				));
			}
		}
		
		// TODO: check if the nested class is a direct nesting
		for (Class<?> nestMember : clazz.getClasses()) {
			if (nestMember == clazz) continue;
			if (!nestMember.getName().startsWith(clazz.getName())) continue;
			CFGSegment segment = nestMember.getAnnotation(CFGSegment.class);
			if (segment == null) {
				System.out.println(nestMember);
				throw new RuntimeException("NYI: default name");
			}
			String name = segment.value();
			String translationName = translationRoot + "/" + dir.replace(".", "/") + "/" + name.toLowerCase();
			translationName = translationName.replace("//", "/");
			
			CategoryBuilder builder2 = category.subcategory(translationName);
			setup(translationRoot, dir + "." + name, builder, builder2, nestMember);
			category.finish(builder2);
		}
	}
	
	public void onConfigChange() {
		for (String s : handles.keySet()) {
			ConfigEntry entry = handles.get(s);
			entry.handle.set(entry.supplier.get());
		}
	}
	
	public void create(ConfigSide type, String file) {
		//#if FABRIC
		java.io.File fl = new java.io.File(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir() + "/" + file);
		if (fl.exists()) internal.read(fl);
		else internal.write(fl);
		
		onConfigChange();
		//#else
		//$$net.minecraftforge.fml.ModLoadingContext.get().registerConfig(switch (type) {
		//$$	case SERVER -> net.minecraftforge.fml.config.ModConfig.Type.SERVER;
		//$$	case COMMON -> net.minecraftforge.fml.config.ModConfig.Type.COMMON;
		//$$	case CLIENT -> net.minecraftforge.fml.config.ModConfig.Type.CLIENT;
		//$$}, (((tfc.smallerunits.utils.config.annoconfg.forge.ForgeConfig) internal).getConfigSpec()), file);
		//#endif
	}
}
