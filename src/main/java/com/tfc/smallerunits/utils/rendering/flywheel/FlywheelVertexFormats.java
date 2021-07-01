package com.tfc.smallerunits.utils.rendering.flywheel;

import com.jozufozu.flywheel.backend.gl.attrib.CommonAttributes;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;

public class FlywheelVertexFormats {
	public static final VertexFormat BLOCK = VertexFormat.builder()
			.addAttributes(
					CommonAttributes.VEC3,
					CommonAttributes.RGBA,
					CommonAttributes.UV,
					CommonAttributes.LIGHT,
					CommonAttributes.NORMAL
//					CommonAttributes.FLOAT
			).build();
}
