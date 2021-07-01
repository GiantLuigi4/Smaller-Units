package com.tfc.smallerunits.renderer;

import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;

import java.util.List;

public class SmallerUnitsProgram extends WorldProgram {
	public SmallerUnitsProgram(Program program, List<IProgramExtension> extensions) {
		super(program, extensions);
	}
}
