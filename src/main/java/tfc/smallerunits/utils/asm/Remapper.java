package tfc.smallerunits.utils.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;

// so ASM is encouraged with fabric, yeah?
// so why isn't this a thing in FAPI by itself?
public class Remapper {
	//@formatter:off
	//#if FABRIC==1
	net.fabricmc.loader.api.MappingResolver resolver;
	public Remapper() { this(net.fabricmc.loader.api.FabricLoader.getInstance().getMappingResolver()); }
	public Remapper(net.fabricmc.loader.api.MappingResolver resolver) {
		this.resolver = resolver;
	}
	public String mapClass(String clz) {
		return resolver.mapClassName("intermediary", clz.replace("/", ".")).replace(".", "/");
	}
	public String mapMethod(MappingInfo info) {
		return resolver.mapMethodName("intermediary", info.owner().replace("/", "."), info.method(), info.desc()) + mapDesc(info.desc());
	}
	public String mapField(MappingInfo info) {
		return resolver.mapFieldName("intermediary", info.owner().replace("/", "."), info.method(), info.desc()) + mapType(info.desc());
	}
	//#else
//$$ 	public Remapper() { }
//$$ 	public String mapClass(String clz) { return clz; }
//$$ 	public String mapMethod(MappingInfo info) { return net.minecraftforge.coremod.api.ASMAPI.mapMethod(info.method()); }
//$$ 	public String mapField(MappingInfo info) { return net.minecraftforge.coremod.api.ASMAPI.mapField(info.method()); }
	//#endif
	//@formatter:on
	
	public String mapType(String desc) {
		if (desc.startsWith("L")) return "L" + mapClass(desc.substring(1, desc.length() - 1)) + ";";
		return desc;
	}
	
	public String mapDesc(String desc) {
		if (desc.startsWith("(")) {
			desc = desc.substring(1);
			ArrayList<String> pieces = new ArrayList<>();
			while (!desc.startsWith(")")) {
				if (desc.startsWith("L")) {
					String[] split = desc.split(";", 2);
					desc = split[1];
					pieces.add(split[0] + ";");
				} else {
					pieces.add(String.valueOf(desc.charAt(0)));
					desc = desc.substring(1);
				}
			}
			desc = desc.substring(1);
			
			StringBuilder output = new StringBuilder("(");
			for (String piece : pieces)
				output.append(mapType(piece));
			output.append(")");
			output.append(mapType(desc));
			
			return output.toString();
		}
		
		return mapType(desc);
	}
	
	public AbstractInsnNode buildMethodCall(String owner, String methodName, String desc) {
		String method = mapMethod(new MappingInfo(owner, methodName, desc));
		desc = "(" + method.split("\\(")[1];
		return new MethodInsnNode(Opcodes.INVOKESTATIC, owner, methodName, desc, false);
	}
}
