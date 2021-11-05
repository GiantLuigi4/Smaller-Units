package tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;

public abstract class SURenderable {
	public abstract void markAllUnused();
	
	public abstract void uploadTerrain(RenderType renderType, BufferBuilder bufferBuilder);
	
	public abstract void render(MatrixStack matrixStack);
	
	public abstract void delete();
	
	public abstract boolean isValid();
}
