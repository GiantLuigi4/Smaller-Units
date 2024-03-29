package tfc.smallerunits.plat.asm;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MixinConnector implements IMixinConfigPlugin {
	private static final ArrayList<String> classLookup = new ArrayList<>();
	private static final ArrayList<String> pkgLookup = new ArrayList<>();
	private static final HashMap<String, ArrayList<String>> incompatibilityMap = new HashMap<>();
	private static final HashMap<String, String> dependencies = new HashMap<>();
	
	static {
		pkgLookup.add("tfc.smallerunits.mixin.compat.");
		pkgLookup.add("tfc.smallerunits.plat.mixin.compat.");
		classLookup.add("tfc.smallerunits.mixin.dangit.block_pos.RSNetworkNodeMixin");
		
		{
			ArrayList<String> incompat = new ArrayList<>();
			incompat.add("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer");
			incompatibilityMap.put("tfc.smallerunits.mixin.LevelRendererMixinBlocks", incompat);
			
			incompat = new ArrayList<>();
			incompat.add("virtuoel.pehkui.api.ScaleTypes");
			incompatibilityMap.put("tfc.smallerunits.mixin.core.gui.server.dist.AbstractContainerMenuMixin", incompat);
			incompatibilityMap.put("tfc.smallerunits.mixin.core.gui.server.dist.AbstractFurnaceBlockEntityMixin", incompat);
			incompatibilityMap.put("tfc.smallerunits.mixin.core.gui.server.dist.BrewingStandBlockEntityMixin", incompat);
			incompatibilityMap.put("tfc.smallerunits.mixin.core.gui.server.dist.EnderChestBlockEntityMixin", incompat);
			incompatibilityMap.put("tfc.smallerunits.mixin.core.gui.server.dist.ItemCombinerMenuMixin", incompat);
			incompatibilityMap.put("tfc.smallerunits.mixin.core.gui.server.dist.RandomizableContainerBlockEntityMixin", incompat);
		}
		
		{
			dependencies.put("tfc.smallerunits.plat.mixin.compat.fabric.RenderWorld", "net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView");
			// tbh I'm pretty sure these should always be present if SU's present
			dependencies.put("tfc.smallerunits.plat.mixin.compat.fabric.networking.ClientPlayNetworkingMixin", "net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.fabric.networking.ServerPlayNetworking", "net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.fabric.networking.ServerMixin", "net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.fabric.networking.ServerPlayNetworkingAddonMixin", "net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.fabric.networking.ClientPlayNetworkingAddonMixin", "tfc.smallerunits.plat.mixin.core.network.ClientPlayNetworkAddon");
		}
		{
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.CModCompatMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.LevelRendererMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.ModCompatMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.TickerClientLevelMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
		}
		{
			dependencies.put("tfc.smallerunits.mixin.compat.optimization.sodium.UnitCapabilityHandlerMixin", "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer");
			dependencies.put("tfc.smallerunits.mixin.compat.optimization.sodium.LevelMixin", "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer");
		}
	}
	
	@Override
	public void onLoad(String mixinPackage) {
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	public boolean doesPkgNeedLookup(String name) {
		for (String s : pkgLookup) {
			if (name.startsWith(s)) return true;
		}
		return false;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (dependencies.containsKey(mixinClassName)) {
			ClassLoader loader = MixinConnector.class.getClassLoader();
			// tests if the classloader contains a .class file for the target
			InputStream stream = loader.getResourceAsStream(dependencies.get(mixinClassName).replace('.', '/') + ".class");
			if (stream != null) {
				try {
					stream.close();
					return true;
				} catch (Throwable ignored) {
				}
			} else return false;
		}
		
		if (incompatibilityMap.containsKey(mixinClassName)) {
			ClassLoader loader = MixinConnector.class.getClassLoader();
			// tests if the classloader contains a .class file for the target
			for (String name : incompatibilityMap.get(mixinClassName)) {
				InputStream stream = loader.getResourceAsStream(name.replace('.', '/') + ".class");
				if (stream == null) {
				} else {
					try {
						stream.close();
						return false;
					} catch (Throwable ignored) {
						return false;
					}
				}
			}
		}
		
		if (classLookup.contains(mixinClassName) || doesPkgNeedLookup(mixinClassName)) {
			ClassLoader loader = MixinConnector.class.getClassLoader();
			// tests if the classloader contains a .class file for the target
			InputStream stream = loader.getResourceAsStream(targetClassName.replace('.', '/') + ".class");
			if (stream != null) {
				try {
					stream.close();
					return true;
				} catch (Throwable ignored) {
					return true;
				}
			}
			return false;
		}
		
		return true;
	}
	
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}
	
	@Override
	public List<String> getMixins() {
		return null;
	}
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
//		if (
//				mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixin") ||
//						mixinClassName.equals("tfc.smallerunits.mixin.core.PacketUtilsMixin") ||
//						mixinClassName.equals("tfc.smallerunits.mixin.data.regions.ChunkMapMixin")
//		) {
//			try {
//				FileOutputStream outputStream = new FileOutputStream(targetClass.name.substring(targetClass.name.lastIndexOf("/") + 1) + "-pre.class");
//				ClassWriter writer = new ClassWriter(0);
//				targetClass.accept(writer);
//				outputStream.write(writer.toByteArray());
//				outputStream.flush();
//				outputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
		Remapper remapper = new Remapper(FabricLoader.getInstance().getMappingResolver());
		
		if (mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixin")) {
			// renderChunkLayer
			String target = remapper.mapMethod(new Remapper.MappingInfo(
					"net/minecraft/class_761",
					"method_3251",
					"(Lnet/minecraft/class_1921;Lnet/minecraft/class_4587;DDDLnet/minecraft/class_1159;)V"
			));
			String desc = "(" + target.split("\\(")[1];
			target = target.split("\\(")[0];
			
			// getCompiledChunk
			String refOwner = "net/minecraft/class_846$class_851";
			String ref = remapper.mapMethod(new Remapper.MappingInfo(
					refOwner,
					"method_3677",
					"()Lnet/minecraft/class_846$class_849;"
			));
			refOwner = remapper.mapClass(refOwner);
			String refDesc = "(" + ref.split("\\(")[1];
			ref = ref.split("\\(")[0];
			
			for (MethodNode method : targetClass.methods) {
				if (method.name.equals(target) && method.desc.equals(desc)) {
					// TODO: try to find a way to figure out a more specific target
					ArrayList<AbstractInsnNode> targetNodes = new ArrayList<>();
					for (AbstractInsnNode instruction : method.instructions) {
						if (instruction instanceof MethodInsnNode methodNode) {
							if (methodNode.owner.equals(refOwner) && methodNode.name.equals(ref) && methodNode.desc.equals(refDesc)) {
								targetNodes.add(methodNode);
							}
						}
					}
					for (AbstractInsnNode targetNode : targetNodes) {
						InsnList list = new InsnList();
						list.add(
								remapper.buildMethodCall(
										"tfc/smallerunits/plat/util/PlatformUtilsClient",
										"updateRenderChunk",
										"(Lnet/minecraft/class_846$class_851;)Lnet/minecraft/class_846$class_851;"
								)
						);
						method.instructions.insertBefore(targetNode, list);
					}
				}
			}
		}
	}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
//		if (
//				mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixin") ||
//						mixinClassName.equals("tfc.smallerunits.mixin.core.PacketUtilsMixin") ||
//						mixinClassName.equals("tfc.smallerunits.mixin.data.regions.ChunkMapMixin")
//		) {
//			try {
//				FileOutputStream outputStream = new FileOutputStream(targetClass.name.substring(targetClass.name.lastIndexOf("/") + 1) + "-post.class");
//				ClassWriter writer = new ClassWriter(0);
//				targetClass.accept(writer);
//				outputStream.write(writer.toByteArray());
//				outputStream.flush();
//				outputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
	}
}