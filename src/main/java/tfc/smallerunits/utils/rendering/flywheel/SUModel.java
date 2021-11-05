package tfc.smallerunits.utils.rendering.flywheel;

import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.BufferBuilderReader;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;

import java.nio.ByteBuffer;

public class SUModel implements IModel {
	VertexFormat format;
	ByteBuffer byteBuffer = null;
	BufferBuilderReader reader;
	int vertices;
	
	public SUModel(VertexFormat block, BufferBuilder unwrap) {
		format = block;
		if (unwrap.isDrawing()) unwrap.finishDrawing();
		reader = new BufferBuilderReader(unwrap);
		this.vertices = reader.getVertexCount();
	}
	
	public SUModel(VertexFormat block, ByteBuffer unwrap, int vertices) {
		format = block;
		byteBuffer = unwrap;
		this.vertices = vertices;
	}
	
	@Override
	public void buffer(VecBuffer buffer) {
		int vertexCount = this.vertexCount();
		if (byteBuffer != null) {
			for (int i = 0; i < vertexCount; ++i) {
				buffer.putVec3(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
				buffer.putVec3(byteBuffer.getFloat(), byteBuffer.getFloat(), byteBuffer.getFloat());
				buffer.putVec2(byteBuffer.get(), byteBuffer.get());
			}
		} else {
			for (int i = 0; i < vertexCount; ++i) {
				float x = reader.getX(i), y = reader.getY(i), z = reader.getZ(i);
				byte nx = reader.getNX(i), ny = reader.getNY(i), nz = reader.getNZ(i);
				float u = reader.getU(i), v = reader.getV(i);
				byte r = reader.getR(i), g = reader.getG(i), b = reader.getB(i), a = reader.getA(i);
				int light = this.reader.getLight(i);
				
				buffer.putVec3(x, y, z);
				buffer.putColor(r, g, b, a);
				buffer.putVec2(u, v);
				{
					byte block = (byte) (LightTexture.getLightBlock(light) << 4);
					byte sky = (byte) (LightTexture.getLightSky(light) << 4);
					buffer.putVec2(block, sky);
				}
				buffer.putVec3((byte) Math.abs(nx), ny, nz);

//				buffer.putVec3(x, y, z);
//				buffer.putVec3(nx, ny, nz);
//				buffer.putVec2(u, v);
//				buffer.putColor(r, g, b, a);
//				byte block = (byte) (LightTexture.getLightBlock(light) << 4);
//				byte sky = (byte) (LightTexture.getLightSky(light) << 4);
//				buffer.putVec2(block, sky);
			}
		}
	}
	
	@Override
	public int vertexCount() {
		return vertices;
	}
	
	@Override
	public VertexFormat format() {
		return FlywheelVertexFormats.BLOCK;
	}
}
