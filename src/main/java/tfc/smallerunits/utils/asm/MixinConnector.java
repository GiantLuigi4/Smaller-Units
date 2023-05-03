package tfc.smallerunits.utils.asm;

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
	
	//#if FABRIC==1
	public static final boolean isFabric = true;
	//#else
//$$ 	public static final boolean isFabric = false;
	//#endif
	
	static {
		pkgLookup.add("tfc.smallerunits.mixin.compat.");
		classLookup.add("tfc.smallerunits.mixin.dangit.block_pos.RSNetworkNodeMixin");
		
		{
			ArrayList<String> incompat = new ArrayList<>();
			incompat.add("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer");
			incompatibilityMap.put("tfc.smallerunits.mixin.LevelRendererMixinBlocks", incompat);
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
		if (mixinClassName.contains(".fabric.")) return isFabric;
		
		if (classLookup.contains(mixinClassName) || doesPkgNeedLookup(mixinClassName)) {
			ClassLoader loader = MixinConnector.class.getClassLoader();
			// tests if the classloader contains a .class file for the target
			InputStream stream = loader.getResourceAsStream(targetClassName.replace('.', '/') + ".class");
			if (stream != null) {
				try {
					stream.close();
				} catch (Throwable ignored) {
				}
				return true;
			}
			return false;
		}
		if (incompatibilityMap.containsKey(mixinClassName)) {
			ClassLoader loader = MixinConnector.class.getClassLoader();
			// tests if the classloader contains a .class file for the target
			for (String name : incompatibilityMap.get(mixinClassName)) {
				InputStream stream = loader.getResourceAsStream(name.replace('.', '/') + ".class");
				if (stream == null) {
					return true;
				} else {
					try {
						stream.close();
					} catch (Throwable ignored) {
					}
					return false;
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
	
	//#if FABRIC==1
	private static final String updateChunkDesc = "(Lnet/minecraft/class_846$class_851;)Lnet/minecraft/class_846$class_851;";
	private static final MappingInfo renderChunkLayer = new MappingInfo(
			"net/minecraft/class_761",
			"method_3251",
			"(Lnet/minecraft/class_1921;Lnet/minecraft/class_4587;DDDLnet/minecraft/class_1159;)V"
	);
	private static final String getCompiledChunkOwner = "net/minecraft/class_846$class_851";
	private static final MappingInfo getCompiledChunk = new MappingInfo(
			getCompiledChunkOwner,
			"method_3677",
			"()Lnet/minecraft/class_846$class_849;"
	);
	//#else
//$$ 	private static final String updateChunkDesc = "(Lnet/minecraft/class_846$class_851;)Lnet/minecraft/class_846$class_851;";
//$$ 	private static final MappingInfo renderChunkLayer = new MappingInfo(
//$$ 			"net/minecraft/client/renderer/LevelRenderer",
//$$ 			"m_172993_",
//$$ 			"(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLcom/mojang/math/Matrix4f;)V"
//$$ 	);
//$$ 	private static final String getCompiledChunkOwner = "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk";
//$$ 	private static final MappingInfo getCompiledChunk = new MappingInfo(
//$$ 			getCompiledChunkOwner,
//$$ 			"m_112835_",
//$$ 			"()Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;"
//$$ 	);
	//#endif
	
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		Remapper remapper = new Remapper();
		
		if (mixinClassName.equals("tfc.smallerunits.mixin.LevelRendererMixin")) {
			// renderChunkLayer
			String target = remapper.mapMethod(renderChunkLayer);
			String desc = "(" + target.split("\\(")[1];
			target = target.split("\\(")[0];
			
			// getCompiledChunk
			String ref = remapper.mapMethod(getCompiledChunk);
			String refOwner = remapper.mapClass(getCompiledChunkOwner);
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
										"tfc/smallerunits/utils/IHateTheDistCleaner",
										"updateRenderChunk", updateChunkDesc
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
	}
}
