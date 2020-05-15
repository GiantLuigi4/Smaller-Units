package test.test;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class NonDefinedModel extends ModelBase {
    public ModelRenderer render = new ModelRenderer(this,0,0).setTextureSize(16,16);
//    public TextureAtlasSprite sprt;

    public NonDefinedModel(float x,float y,float z,int width,int height,int depth) {
        this.render.addBox(x,y,z,width,height,depth);
//        this.sprt=sprite;
    }
    public void render(float scale) {
        this.render.render(1f/scale);
    }
}
