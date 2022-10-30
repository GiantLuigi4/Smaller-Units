package tfc.smallerunits.mixin.data.access;

import com.mojang.blaze3d.vertex.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.client.access.VertexBufferAccessor;

@Mixin(VertexBuffer.class)
public abstract class VertexBufferMixin implements VertexBufferAccessor {
	@Shadow
	protected abstract void bindVertexArray();
	
	@Override
	public void invokeBindVAO() {
		bindVertexArray();
	}
}
