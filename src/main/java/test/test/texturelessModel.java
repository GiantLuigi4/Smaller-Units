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

public class texturelessModel implements IBakedModel {
    public texturelessModel(IBakedModel mdlOriginal,TextureAtlasSprite sprt) {
        ArrayList<BakedQuad> qds = new ArrayList<>();
        EnumFacing[] facings = new EnumFacing[] {
                null,
                EnumFacing.DOWN,
                EnumFacing.UP,
                EnumFacing.NORTH,
                EnumFacing.SOUTH,
                EnumFacing.EAST,
                EnumFacing.WEST
        };
        for (EnumFacing facing:facings) {
            for (BakedQuad qd:mdlOriginal.getQuads(null,facing,0)) {
                BakedQuad newQD=new BakedQuadRetextured(qd,sprt);
                int[] override = newQD.getVertexData();
                qds.add(new BakedQuad(override, newQD.getTintIndex(), newQD.getFace(), newQD.getSprite(), newQD.shouldApplyDiffuseLighting(), newQD.getFormat()));
            }
        }
        remappedQDS=qds;
    }
    public texturelessModel(IBakedModel mdlOriginal,TextureAtlasSprite sprt,int tint) {
        ArrayList<BakedQuad> qds = new ArrayList<>();
        EnumFacing[] facings = new EnumFacing[] {
                null,
                EnumFacing.DOWN,
                EnumFacing.UP,
                EnumFacing.NORTH,
                EnumFacing.SOUTH,
                EnumFacing.EAST,
                EnumFacing.WEST
        };
        for (EnumFacing facing:facings) {
            for (BakedQuad qd:mdlOriginal.getQuads(null,facing,0)) {
                BakedQuad newQD=new BakedQuadRetextured(qd,sprt);
                int[] override = newQD.getVertexData();
                qds.add(new BakedQuad(override, tint, newQD.getFace(), newQD.getSprite(), newQD.shouldApplyDiffuseLighting(), newQD.getFormat()));
            }
        }
        remappedQDS=qds;
    }

    ArrayList<BakedQuad> remappedQDS;

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        ArrayList<BakedQuad> toReturn = new ArrayList<>();
        try {
            for (BakedQuad qd:remappedQDS) {
                if (qd.getFace().equals(side)) {
                    toReturn.add(qd);
                }
            }
            return toReturn;
        } catch (NullPointerException err) {
        }
//        throw new RuntimeException(new NullPointerException());
        return remappedQDS;
//        return toReturn;
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
