package test.test;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class fluidModel implements IBakedModel {
    public fluidModel(int x1,int x2,int y1,int y2,int z1,int z2,TextureAtlasSprite tas,int tint) {
        int[] points=new int[]{
                x1,y1,z1,
                x1,y1,z2,
                x2,y1,z2,
                x2,y1,z1,
                x1,y1,z1
        };
        BakedQuad qd=new BakedQuad(points,tint,EnumFacing.DOWN,tas);
        qds.add(qd);
        points=new int[]{
                x1,y1,z1,
                x1,y2,z1,
                x2,y2,z1,
                x2,y1,z1,
                x1,y1,z1
        };
        qd=new BakedQuad(points,tint,EnumFacing.DOWN,tas);
        qds.add(qd);
    }

    ArrayList<BakedQuad> qds = new ArrayList<>();

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        ArrayList<BakedQuad> toReturn = new ArrayList<>();
        try {
            for (BakedQuad qd:qds) {
                if (qd.getFace().equals(side)) {
                    toReturn.add(qd);
                }
            }
        } catch (NullPointerException err) {
            return qds;
        }
        return qds;
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
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return null;
    }
}
