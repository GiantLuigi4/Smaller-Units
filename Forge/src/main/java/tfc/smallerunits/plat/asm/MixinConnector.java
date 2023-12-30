package tfc.smallerunits.plat.asm;

import net.minecraftforge.coremod.api.ASMAPI;
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
		
		{
			ArrayList<String> incompat = new ArrayList<>();
			incompat.add("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer");
			incompatibilityMap.put("tfc.smallerunits.mixin.LevelRendererMixinBlocks", incompat);
		}
//		{
//			ArrayList<String> incompat = new ArrayList<>();
//			incompat.add("qouteall.imm_ptl.core.network.PacketRedirection");
//			incompatibilityMap.put("tfc.smallerunits.plat.mixin.core.network.ConnectionMixin", incompat);
//		}
		{
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.LevelRendererMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.ModCompatMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.CModCompatMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			dependencies.put("tfc.smallerunits.plat.mixin.compat.optimization.flywheel.TickerClientLevelMixin", "com.jozufozu.flywheel.api.FlywheelWorld");
			
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
					return false;
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
//				mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixinBlocks") ||
//						mixinClassName.equals("tfc.smallerunits.mixin.core.gui.client.expansion.DebugScreenOverlayMixin")
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
		if (mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixin")) {
			String target = ASMAPI.mapMethod("m_172993_"); // renderChunkLayer
			String desc = "(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V"; // TODO: I'd like to not assume Mojmap
			
			String refOwner = "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk";
			String ref = ASMAPI.mapMethod("m_112835_"); // getCompiledChunk
			String refDesc = "()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;";
			for (MethodNode method : targetClass.methods) {
				if (method.name.equals(target) && method.desc.equals(desc)) {
//					AbstractInsnNode targetNode = null;
					// TODO: try to find a way to figure out a more specific target
					ArrayList<AbstractInsnNode> targetNodes = new ArrayList<>();
					for (AbstractInsnNode instruction : method.instructions) {
						if (instruction instanceof MethodInsnNode methodNode) {
							if (methodNode.owner.equals(refOwner) && methodNode.name.equals(ref) && methodNode.desc.equals(refDesc)) {
								targetNodes.add(methodNode);
//								targetNode = methodNode;
//								break;
							}
						}
					}
//					if (targetNode != null) {
					for (AbstractInsnNode targetNode : targetNodes) {
						InsnList list = new InsnList();
						list.add(ASMAPI.buildMethodCall(
								"tfc/smallerunits/plat/util/PlatformUtilsClient",
								"updateRenderChunk",
								"(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;", ASMAPI.MethodType.STATIC)
						);
						method.instructions.insertBefore(targetNode, list);
					}
//					}
				}
			}
		}
	}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
//		if (
//				mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixin") ||
//						mixinClassName.equals("tfc.smallerunits.mixin.core.gui.client.expansion.DebugScreenOverlayMixin")
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
