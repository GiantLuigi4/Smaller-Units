package test.test;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.List;

public class world extends World {
    public World wo;
    public int scale;
    public BlockPos offset;
    public world(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,World world,int sc,BlockPos off) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
        wo=world;
        scale=sc;
        offset=off;
    }
    public world(boolean client) {
        super(null, null, null, null, client);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    Chunk ck = new Chunk(this,0,0);

    @Override
    public Chunk getChunk(BlockPos pos) {
        return super.getChunk(pos);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return ck;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return false;
    }

    @Override
    public boolean isBlockLoaded(BlockPos pos) {
        boolean loadedX=pos.getX()>=-16&&pos.getX()<=(16*2);
        boolean loadedZ=pos.getZ()>=-16&&pos.getZ()<=(16*2);
        return loadedX&&loadedZ;
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        try {
            player.getEntityWorld().playSound(player, pos.getX()/scale+offset.getX(), pos.getY()/scale+offset.getY(), pos.getZ()/scale+offset.getZ(), soundIn, category, volume/scale, pitch*scale);
        } catch (Exception err) {}
//        wo.playSound(player, pos, soundIn, category, volume/scale, pitch*scale);
    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        try {
            player.getEntityWorld().playSound(player, x/scale+offset.getX(), y/scale+offset.getY(), z/scale+offset.getZ(), soundIn, category, volume/scale, pitch*scale);
        } catch (Exception err) {}
    }

    @Override
    public void playSound(double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {
        wo.playSound(x/scale+offset.getX(), y/scale+offset.getY(), z/scale+offset.getZ(), soundIn, category, volume/scale, pitch*scale, distanceDelay);
    }

    @Override
    public void playRecord(BlockPos blockPositionIn, @Nullable SoundEvent soundEventIn) {
        wo.playRecord(blockPositionIn, soundEventIn);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
        SmallerUnitsMod.log.log(Level.INFO,bb.offset(offset).shrink(scale));
        return entityIn.getEntityWorld().getEntitiesWithinAABBExcludingEntity(entityIn, bb.offset(offset).shrink(scale));
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        return super.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }
}
