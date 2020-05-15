package test.test;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class modelBakery implements IBakedModel {
    public modelBakery(BakedQuad qd) {
        ArrayList<BakedQuad> qds = new ArrayList<BakedQuad>();
        qds.add(qd);
        bqds = qds;
    }
    public modelBakery(BakedQuad qd,TextureAtlasSprite sprite) {
        BakedQuad qd2 = new BakedQuadRetextured(qd,sprite);
        ArrayList<BakedQuad> qds = new ArrayList<BakedQuad>();
        qds.add(qd2);
        bqds = qds;
    }
    public modelBakery(BakedQuad qd,int tint) {
        BakedQuad qd2=new BakedQuad(qd.getVertexData(),tint,null,qd.getSprite(),qd.shouldApplyDiffuseLighting(),qd.getFormat());
//        if (qd.getTintIndex()==0) {
//            qd2=new BakedQuad(qd.getVertexData(),tint,qd.getFace(),qd.getSprite());
//        }
        ArrayList<BakedQuad> qds = new ArrayList<BakedQuad>();
        qds.add(qd2);
        bqds = qds;
    }
    protected List<BakedQuad> bqds = null;

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return bqds;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return bqds.get(0).getSprite();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}
