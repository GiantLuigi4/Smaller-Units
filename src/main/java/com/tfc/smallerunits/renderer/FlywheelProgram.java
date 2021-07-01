package com.tfc.smallerunits.renderer;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.ModelTemplate;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.event.GatherContextEvent;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Stream;

public class FlywheelProgram {
	private static final ResourceLocation UNIT_PROGRAM = new ResourceLocation("smallerunits:unit_program");
	private static final ResourceLocation UNIT_PROGRAM_SPEC = new ResourceLocation("smallerunits:unit_shader");
	
	public static WorldContext<SmallerUnitsProgram> UNIT;
	
	public static void onFlywheelInit(GatherContextEvent event) {
		Backend backend = event.getBackend();
		
		UNIT = backend.register(
				unitContext(backend)
						.withSpecStream(() -> Stream.of(UNIT_PROGRAM_SPEC))
						.withTemplateFactory(ModelTemplate::new)
		);
	}
	
	private static WorldContext<SmallerUnitsProgram> unitContext(Backend backend) {
		return new WorldContext<>(backend, SmallerUnitsProgram::new)
				.withName(UNIT_PROGRAM)
				.withBuiltin(ShaderType.FRAGMENT, new ResourceLocation("flywheel:context/world"), "/builtin.frag")
				.withBuiltin(ShaderType.VERTEX, new ResourceLocation("flywheel:context/world"), "/builtin.vert");
	}
}
