package tfc.smallerunits.plat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfc.smallerunits.utils.config.annoconfg.ConfigEntry;
import tfc.smallerunits.utils.config.annoconfg.ConfigSide;
import tfc.smallerunits.utils.config.annoconfg.annotation.format.*;
import tfc.smallerunits.utils.config.annoconfg.annotation.value.*;
import tfc.smallerunits.utils.config.annoconfg.handle.UnsafeHandle;
import tfc.smallerunits.utils.config.annoconfg.util.ConfigEnum;
import tfc.smallerunits.utils.config.annoconfg.util.EnumType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class AnnoCFG {
	private ForgeConfigSpec mySpec;
	
	private final HashMap<String, ConfigEntry> handles = new HashMap<>();
	
	private static final ArrayList<AnnoCFG> configs = new ArrayList<>();
	
	public AnnoCFG(Class<?> clazz) {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::onConfigChange);
		ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
		setup("", configBuilder, clazz);
		configs.add(this);
		
		Config configDescriptor = clazz.getAnnotation(Config.class);
		if (configDescriptor != null) {
			String pth = configDescriptor.path();
			String extra = configDescriptor.extra();
			if (!pth.isEmpty()) pth = pth + "/";
			if (!extra.isEmpty() && !extra.startsWith("/")) extra = "_" + extra;
			switch (configDescriptor.type()) {
				case SERVER -> create(ModConfig.Type.SERVER, pth + ModLoadingContext.get().getActiveNamespace() + extra + "_server.toml");
				case CLIENT -> create(ModConfig.Type.CLIENT, pth + ModLoadingContext.get().getActiveNamespace() + extra + "_client.toml");
				case COMMON -> create(ModConfig.Type.COMMON, pth + ModLoadingContext.get().getActiveNamespace() + extra + "_common.toml");
				default -> throw new RuntimeException("wat");
			}
		}
	}
	
	protected void setupCommentsAndTranslations(AnnotatedElement element, ForgeConfigSpec.Builder builder, String... additionalLines) {
		Translation translation = element.getAnnotation(Translation.class);
		Comment comment = element.getAnnotation(Comment.class);
		
		StringBuilder builder1 = new StringBuilder();
		if (comment != null) {
			for (int i = 0; i < comment.value().length; i++) {
				String s = comment.value()[i];
				builder1.append(s);
				if (i != comment.value().length - 1)
					builder1.append("\n");
			}
		}
		for (String additionalLine : additionalLines) builder1.append(additionalLine);
		if (!builder1.isEmpty())
			builder.comment(builder1.toString());
		
		if (translation != null) {
			builder.translation(translation.value());
		}
	}
	
	public void setup(String dir, ForgeConfigSpec.Builder builder, Class<?> clazz) {
		if (dir.startsWith(".")) dir = dir.substring(1);
		
		for (Field field : clazz.getFields()) {
			if (field.canAccess(null)) {
				Skip skip = field.getAnnotation(Skip.class);
				if (skip != null) continue;
				
				Name name = field.getAnnotation(Name.class);
				
				String nameStr = field.getName();
				if (name != null) nameStr = name.value();
				if (field.getType().equals(IntBounds.Bound.class)) {
					IntBounds bounds = field.getAnnotation(IntBounds.class);
					setupCommentsAndTranslations(field, builder,
							"Default: [" + bounds.minV() + ", " + bounds.midV() + ", " + bounds.maxV() + "]",
							"Range: [" + bounds.rangeMin() + ", " + bounds.rangeMax() + "]"
					);
				} else {
					setupCommentsAndTranslations(field, builder);
				}
				
				Supplier<?> value;
				
				Default defaultValue = field.getAnnotation(Default.class);
				switch (EnumType.forClass(field.getType())) {
					case INT -> {
						IntRange range = field.getAnnotation(IntRange.class);
						int v = defaultValue.valueI();
						if (range != null) {
							int min = range.minV();
							int max = range.maxV();
							
							value = builder.defineInRange(nameStr, v, min, max);
						} else {
							value = builder.define(nameStr, v);
						}
					}
					case LONG -> {
						LongRange range = field.getAnnotation(LongRange.class);
						long v = defaultValue.valueL();
						if (range != null) {
							long min = range.minV();
							long max = range.maxV();
							
							value = builder.defineInRange(nameStr, v, min, max);
						} else {
							value = builder.define(nameStr, v);
						}
					}
					case DOUBLE -> {
						DoubleRange range = field.getAnnotation(DoubleRange.class);
						double v = defaultValue.valueD();
						if (range != null) {
							double min = range.minV();
							double max = range.maxV();
							
							value = builder.defineInRange(nameStr, v, min, max);
						} else {
							value = builder.define(nameStr, v);
						}
					}
					case BOOLEAN -> {
						boolean b = defaultValue.valueBoolean();
						value = builder.define(nameStr, b);
					}
					default -> {
						IntBounds bounds = field.getAnnotation(IntBounds.class);
						if (bounds != null) {
							Supplier<String> src = builder.define(
									nameStr, "[" + bounds.minV() + ", " + bounds.midV() + ", " + bounds.maxV() + "]", (text) -> {
										try {
											String txt = text.toString();
											txt = txt.substring(1, txt.length() - 1);
											String[] split = txt.split(",");
											int[] ints = new int[3];
											for (int i = 0; i < split.length; i++)
												ints[i] = Integer.parseInt(split[i].trim());
											if (ints[0] > ints[1]) return false;
											if (ints[1] > ints[2]) return false;
										} catch (Throwable ignored) {
											return false;
										}
										return true;
									}
							);
							value = () -> {
								String txt = src.get();
								txt = txt.substring(1, txt.length() - 1);
								String[] split = txt.split(",");
								int[] ints = new int[3];
								for (int i = 0; i < split.length; i++) ints[i] = Integer.parseInt(split[i].trim());
								return new IntBounds.Bound(ints[0], ints[1], ints[2]);
							};
						} else if (ConfigEnum.class.isAssignableFrom(field.getType())) {
							try {
								ConfigEnum[] o = (ConfigEnum[]) field.getType().getDeclaredMethod("values").invoke(null);
								
								Supplier<String> src = builder.define(
										nameStr, o[defaultValue.valueI()].getConfigName(), (text) -> {
											for (ConfigEnum configEnum : o)
												if (text.equals(configEnum.getConfigName()))
													return true;
											return false;
										}
								);
								value = () -> {
									String txt = src.get();
									for (ConfigEnum configEnum : o)
										if (txt.equals(configEnum.getConfigName()))
											return configEnum;
									return null;
								};
							} catch (Throwable err) {
								throw new RuntimeException(err);
							}
						} else {
							throw new RuntimeException("NYI " + field.getType());
						}
					}
				}
				
				Object o;
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
			
			setupCommentsAndTranslations(nestMember, builder);
			
			builder.push(name);
			setup(dir + "." + name, builder, nestMember);
			builder.pop();
		}
		
		mySpec = builder.build();
	}
	
	public void onConfigChange(ModConfigEvent event) {
		if (
				event.getConfig().getSpec().equals(mySpec) ||
						event.getConfig().getSpec() == mySpec
		) {
			for (String s : handles.keySet()) {
				ConfigEntry entry = handles.get(s);
				entry.update();
			}
		}
	}
	
	public void create(ModConfig.Type type, String file) {
		ModLoadingContext.get().registerConfig(type, mySpec, file);
	}
}
