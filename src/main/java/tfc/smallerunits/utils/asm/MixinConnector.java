package tfc.smallerunits.utils.asm;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MixinConnector implements IMixinConfigPlugin {
	private static final ArrayList<String> classLookup = new ArrayList<>();
	private static final HashMap<String, ArrayList<String>> incompatibilityMap = new HashMap<>();
	
	static {
		classLookup.add("tfc.smallerunits.mixin.compat.ChiselAndBitMeshMixin");
		classLookup.add("tfc.smallerunits.mixin.compat.sodium.SodiumLevelRendererMixin");
		classLookup.add("tfc.smallerunits.mixin.compat.sodium.RenderSectionManagerMixin");
		
		{
			ArrayList<String> incompat = new ArrayList<>();
			incompat.add("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer");
			incompatibilityMap.put("tfc.smallerunits.mixin.LevelRendererMixin", incompat);
		}
	}
	
	@Override
	public void onLoad(String mixinPackage) {
	}
	
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (classLookup.contains(mixinClassName)) {
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
						return false;
					} catch (Throwable ignored) {
						return false;
					}
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
	
	}
	
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		// TODO: transform level renderer class here, for compatibility purposes
	}
}
