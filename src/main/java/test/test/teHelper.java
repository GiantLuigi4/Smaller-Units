package test.test;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

public class teHelper {
    public static ItemStack storeTEInStack(ItemStack stack, TileEntity te) {
        NBTTagCompound nbttagcompound = te.writeToNBT(new NBTTagCompound());

        if (stack.getItem() == Items.SKULL && nbttagcompound.hasKey("Owner")) {
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
            stack.setTagCompound(nbttagcompound3);
            return stack;
        } else {
            stack.setTagInfo("BlockEntityTag", nbttagcompound);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagString("(+NBT)"));
            nbttagcompound1.setTag("Lore", nbttaglist);
            stack.setTagInfo("display", nbttagcompound1);
            return stack;
        }
    }

    public static IBlockState getStateFromStack(ItemStack stk2, block.TileEntityCustom tile, BlockPos subPos) {
        IBlockState ste2=null;
        if (stk2.getItem() instanceof ItemSeeds) {
            ItemSeeds sds = (ItemSeeds)stk2.getItem();
            ste2=sds.getPlant(tile.wo,subPos);
        } else if (stk2.getItem() instanceof ItemSeedFood) {
            ItemSeedFood sds = (ItemSeedFood)stk2.getItem();
            ste2=sds.getPlant(tile.wo,subPos);
        } else if (stk2.getItem() instanceof ItemBucket) {
            ItemBucket bck = (ItemBucket) stk2.getItem();
            FluidBucketWrapper fbw = new FluidBucketWrapper(stk2);
            try {
                ste2 = fbw.getFluid().getFluid().getBlock().getDefaultState();
            } catch (NullPointerException err) {}
        } else if (stk2.getItem() instanceof ItemBlock) {
            ItemBlock blk = (ItemBlock)stk2.getItem();
            ste2=blk.getBlock().getStateFromMeta(stk2.getItemDamage());
        } else if (stk2.getItem() instanceof ItemBlockSpecial) {
            ItemBlockSpecial blk = (ItemBlockSpecial)stk2.getItem();
            ste2=blk.getBlock().getStateFromMeta(stk2.getItemDamage());
        } else if (stk2.getItem().equals(Items.REDSTONE)) {
            Block blk = Blocks.REDSTONE_WIRE;
            ste2=blk.getStateFromMeta(stk2.getItemDamage());
        } else if (stk2.getItem().equals(Items.FLINT_AND_STEEL)) {
            Block blk = Blocks.FIRE;
            ste2=blk.getStateFromMeta(stk2.getItemDamage());
        } else {
            Block blk = Block.getBlockFromName(stk2.getDisplayName());
            try {
                ste2=blk.getStateFromMeta(stk2.getItemDamage());
            } catch (NullPointerException err) {
                try {
                    ste2=blk.getDefaultState();
                } catch (NullPointerException err2) {
                    ste2=Blocks.BEDROCK.getDefaultState();
                }
            }
        }
        return ste2;
    }
}
