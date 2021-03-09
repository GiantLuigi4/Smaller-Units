package com.tfc.smallerunits;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class SmallerUnitsItemModelLoader implements IModelLoader {
	public SmallerUnitsItemModelLoader() {
	}
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
	
	}
	
	@Override
	public IModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
		return null;
	}
	
	public static class SmallerUnitsItemModel extends BakedItemModel {
		
		
		public SmallerUnitsItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms, ItemOverrideList overrides, boolean untransformed, boolean isSideLit) {
			super(quads, particle, transforms, overrides, untransformed, isSideLit);
			
		}
		
		/**
		 * @deprecated Forge: Use {@link IForgeBakedModel#handlePerspective(ItemCameraTransforms.TransformType, MatrixStack)} instead
		 */
		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}
	}
}
