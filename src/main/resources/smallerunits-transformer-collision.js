// I happened to already have written this
// https://github.com/LorenzoPapi/PortalGunReborn/blob/master/src/main/resources/pgr-ICollisionReader-transformer.js
function initializeCoreMod() {
    var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
    var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
    var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
    var LineNumberNode = Java.type("org.objectweb.asm.tree.LineNumberNode");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var InvokeDynamicInsnNode = Java.type("org.objectweb.asm.tree.InvokeDynamicInsnNode");
    var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var mappedMethodName = ASMAPI.mapMethod("func_226666_b_");

    var TraceMethodVisitor = Java.type('org.objectweb.asm.util.TraceMethodVisitor');
    var Textifier = Java.type('org.objectweb.asm.util.Textifier');

	return {
		'coremodmethod': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.world.ICollisionReader',
				'methodName': mappedMethodName,
				'methodDesc': '(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/stream/Stream;'
			},
            'transformer': function(node) {
//				if (!FMLEnvironment.dist.isProduction)
//					print("Transforming method " + node.name);
				var arrayLength = node.instructions.size();
				var targetInstruction = null;
				for (var i = 0; i < arrayLength; i++) {
					var insn = node.instructions.get(i);
					if (insn instanceof MethodInsnNode) {
						if (insn.name.equals("stream")) {
							targetInstruction = insn;
						}
					}
				}

				var list = new InsnList();
				list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/util/stream/Stream"));
				list.add(new VarInsnNode(Opcodes.ALOAD, 1));
				list.add(new VarInsnNode(Opcodes.ALOAD, 2));
				list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "tfc/smallerunits/coremod/CollisionReaderCoremod", "append", "(Ljava/util/stream/Stream;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/stream/Stream;"));
				node.instructions.insert(targetInstruction, list);

//				if (!FMLEnvironment.dist.isProduction) {
//					var visitor = new TraceMethodVisitor(new Textifier());
//					for(var iter = node.instructions.iterator(); iter.hasNext();){
//						iter.next().accept(visitor);
//					}
//					print(visitor.p.getText());
//				}

				return node;
            }
		}
	}
}