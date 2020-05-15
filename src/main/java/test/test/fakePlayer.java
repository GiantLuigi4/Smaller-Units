package test.test;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class fakePlayer extends EntityPlayer {
    EntityPlayer player;
    int scale;
    public fakePlayer(World worldIn,GameProfile gameProfileIn,EntityPlayer py,int sc) {
        super(worldIn, gameProfileIn);
        this.player=py;
        this.scale=sc;
    }
    public fakePlayer(World worldIn) {
        super(worldIn, null);
    }

    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {
        player.openGui(mod, modGuiId, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
    }

    @Override
    public boolean isSpectator() {
        return player.isSpectator();
    }

    @Override
    public boolean isCreative() {
        return player.isCreative();
    }
}
