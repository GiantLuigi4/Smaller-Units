package tfc.smallerunits.utils.config.annoconfg;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import me.shedaniel.clothconfig2.gui.entries.LongListEntry;
import me.shedaniel.clothconfig2.impl.builders.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.CFGSegment;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.Config;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.Name;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.Skip;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.Default;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.DoubleRange;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.IntRange;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.LongRange;
import tfc.smallerunits.utils.config.annoconfg.handle.UnsafeHandle;
import tfc.smallerunits.utils.config.annoconfg.util.EnumType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO: finish this
public class AnnoCFG {
	private final Function<Screen, Screen> cfgScreen;
	
	private final HashMap<String, ConfigEntry> handles = new HashMap<>();
	
	private static final ArrayList<AnnoCFG> configs = new ArrayList<>();
	
	protected interface EntryAdder {
		void add(AbstractConfigListEntry<?> entry);
	}
	
	public AnnoCFG(Class<?> clazz) {
		Config configDescriptor = clazz.getAnnotation(Config.class);
		
		String translationRoot = configDescriptor.path().replace("/", "_") + configDescriptor.namespace() + ":" + configDescriptor.type().name().toLowerCase();
		
		ConfigBuilder builder = ConfigBuilder.create()
				.setTitle(Component.translatable(translationRoot));
		builder.setSavingRunnable(this::onConfigChange);
		
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable(translationRoot));
		setup(translationRoot, "", builder, category::addEntry, clazz);
		configs.add(this);
		
		String pth = configDescriptor.path();
		if (!pth.isEmpty()) pth = pth + "/";
		switch (configDescriptor.type()) {
			case SERVER -> create(configDescriptor.type(), pth + configDescriptor.namespace() + "_server.toml");
			case CLIENT -> create(configDescriptor.type(), pth + configDescriptor.namespace() + "_client.toml");
			case COMMON -> create(configDescriptor.type(), pth + configDescriptor.namespace() + "_common.toml");
			default -> throw new RuntimeException("wat");
		}
		
		cfgScreen = (parent) -> {
			builder.setParentScreen(parent);
			builder.setEditable(true);
			return builder.build();
		};
		builder.build();
	}
	
	public void setup(String translationRoot, String dir, ConfigBuilder builder, EntryAdder category, Class<?> clazz) {
		if (dir.startsWith(".")) dir = dir.substring(1);
		
		ConfigEntryBuilder builder1 = builder.entryBuilder();
		
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
							
							IntFieldBuilder fieldBuilder = builder1.startIntField(Component.translatable(translationName), v);
							fieldBuilder.setMin(min);
							fieldBuilder.setDefaultValue(v);
							fieldBuilder.setMax(max);
							fieldBuilder.setSaveConsumer((i) -> {
							});
							
							IntegerListEntry entry = fieldBuilder.build();
							entry.setEditable(true);
							category.add(entry);
							value = entry::getValue;
						} else {
							IntFieldBuilder fieldBuilder = builder1.startIntField(Component.translatable(translationName), v);
							fieldBuilder.setDefaultValue(v);
							fieldBuilder.setSaveConsumer((i) -> {
							});
							
							IntegerListEntry entry = fieldBuilder.build();
							entry.setEditable(true);
							category.add(entry);
							value = entry::getValue;
						}
					}
					case LONG -> {
						LongRange range = field.getAnnotation(LongRange.class);
						long v = defaultValue.valueL();
						if (range != null) {
							long min = range.minV();
							long max = range.maxV();
							
							LongFieldBuilder fieldBuilder = builder1.startLongField(Component.translatable(translationName), v);
							fieldBuilder.setMin(min);
							fieldBuilder.setDefaultValue(v);
							fieldBuilder.setMax(max);
							fieldBuilder.setSaveConsumer((i) -> {
							});
							
							LongListEntry entry = fieldBuilder.build();
							entry.setEditable(true);
							category.add(entry);
							value = entry::getValue;
						} else {
							LongFieldBuilder fieldBuilder = builder1.startLongField(Component.translatable(translationName), v);
							fieldBuilder.setDefaultValue(v);
							fieldBuilder.setSaveConsumer((i) -> {
							});
							
							LongListEntry entry = fieldBuilder.build();
							entry.setEditable(true);
							category.add(entry);
							value = entry::getValue;
						}
					}
					case DOUBLE -> {
						DoubleRange range = field.getAnnotation(DoubleRange.class);
						double v = defaultValue.valueD();
						if (range != null) {
							double min = range.minV();
							double max = range.maxV();
							
							DoubleFieldBuilder fieldBuilder = builder1.startDoubleField(Component.translatable(translationName), v);
							fieldBuilder.setMin(min);
							fieldBuilder.setDefaultValue(v);
							fieldBuilder.setMax(max);
							fieldBuilder.setSaveConsumer((i) -> {
							});
							
							DoubleListEntry entry = fieldBuilder.build();
							entry.setEditable(true);
							category.add(entry);
							value = entry::getValue;
						} else {
							DoubleFieldBuilder fieldBuilder = builder1.startDoubleField(Component.translatable(translationName), v);
							fieldBuilder.setDefaultValue(v);
							fieldBuilder.setSaveConsumer((i) -> {
							});
							
							DoubleListEntry entry = fieldBuilder.build();
							entry.setEditable(true);
							category.add(entry);
							value = entry::getValue;
						}
					}
					case BOOLEAN -> {
						BooleanToggleBuilder fieldBuilder = builder1.startBooleanToggle(Component.translatable(translationName), defaultValue.valueBoolean());
						fieldBuilder.setDefaultValue(defaultValue.valueBoolean());
						fieldBuilder.setSaveConsumer((i) -> {
						});
						
						BooleanListEntry entry = fieldBuilder.build();
						entry.setEditable(true);
						category.add(entry);
						value = entry::getValue;
					}
					default -> {
						throw new RuntimeException("Invalid config entry (type: " + EnumType.forClass(clazz) + "): " + nameStr + " in " + clazz.getName());
					}
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
			
			SubCategoryBuilder builder2 = builder1.startSubCategory(Component.translatable(translationName));
			setup(translationRoot, dir + "." + name, builder, builder2::add, nestMember);
			AbstractConfigListEntry<?> entry = builder2.build();
			entry.setEditable(true);
			category.add(entry);
		}
	}
	
	public void onConfigChange() {
		for (String s : handles.keySet()) {
			ConfigEntry entry = handles.get(s);
			entry.handle.set(entry.supplier.get());
		}
	}
	
	public void create(ConfigSide type, String file) {
	}
	
	public Screen getScreen(Screen screen) {
		return cfgScreen.apply(screen);
	}
}
