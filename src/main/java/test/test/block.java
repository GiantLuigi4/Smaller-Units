package test.test;

import net.minecraft.block.*;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentWaterWalker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

public class block extends Block implements ITileEntityProvider {
    public block(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
        setRegistryName("smallUnits:su");
        setTranslationKey("smallunits.block.tile.unit");
        setLightOpacity(0);
        Currenthardness=-1.0f;
        setHardness(-1.0f);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

//    @Override
//    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
//        return false;
//    }

    public float Currenthardness=-1.0f;
    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return Currenthardness;
    }

    AxisAlignedBB box=null;
    Vec3d hit=null;
    EnumFacing side=null;
    BlockPos subPos=null;
    IBlockState state=null;
    Block block=null;
    EntityPlayer player=null;
    int scale=2;
    public int readCooldown=60;
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        AxisAlignedBB result = new AxisAlignedBB(0,0,0,0,0,0);
        try {
//        ArrayList<smallUnit> SmallUnits=null;
//        try {
//            Object[] thisObjList=objects.get(posArrayList.indexOf(pos));
//            SmallUnits = (ArrayList<smallUnit>)thisObjList[2];
//        } catch (IndexOutOfBoundsException err) {}
            EntityPlayer py;
            if (player==null) {
                py = Minecraft.getMinecraft().player;
            } else {
                py = player;
            }
            World wo = SmallerUnitsMod.getWorld(pos,false);
            World wo2 = SmallerUnitsMod.getWorld(pos,true);
            world=worldIn;
            TileEntityCustom tile = (TileEntityCustom)worldIn.getTileEntity(pos);
            Vec3d playerLook = py.getLook(0);
            Vec3d player = py.getPositionEyes(0);
//        Test.log.log(Level.INFO,playerLook);
            double distBest = Double.POSITIVE_INFINITY;
            ArrayList<smallUnit> SmallUnits = SmallerUnitsMod.getUnits(pos);
//        SmallUnits=((TileEntityCustom)worldIn.getTileEntity(pos)).getUnits();
            try {
                Vec3d testPos=new Vec3d(playerLook.x,playerLook.y,playerLook.z).add(player);
//            for (double j=0; j<=15; j+=0.025) {
                Vec3d range = (player.add(playerLook.scale(py.REACH_DISTANCE.getDefaultValue())));
                testPos.add(playerLook);
                for (smallUnit su:SmallUnits) {
                    ((TileEntityCustom)worldIn.getTileEntity(su.pos)).setSize(su.sc);
                    if (!su.bk.equals(Blocks.AIR)) {
                        try {
                            BlockPos offset = new BlockPos(su.sc/2,128-su.sc/2,su.sc/2);
                            BlockPos posInWo=su.sPos.add(offset);
                            AxisAlignedBB unscaledBB = su.bk.getStateFromMeta(su.meta).getActualState(wo,posInWo).getBoundingBox(wo,posInWo).offset(su.sPos);
                            try {
                                if (unscaledBB.equals(null)) {
                                    unscaledBB=su.bk.getStateFromMeta(su.meta).getActualState(wo2,posInWo).getBoundingBox(wo2,posInWo).offset(su.sPos);
                                }
                            } catch (Exception err) {
                                unscaledBB=su.bk.getStateFromMeta(su.meta).getActualState(wo2,posInWo).getBoundingBox(wo2,posInWo).offset(su.sPos);
                            }
                            AxisAlignedBB bb = new AxisAlignedBB(unscaledBB.minX/su.sc, unscaledBB.minY/su.sc, unscaledBB.minZ/su.sc, unscaledBB.maxX/su.sc, unscaledBB.maxY/su.sc, unscaledBB.maxZ/su.sc);
                            if (!su.bk.equals(Blocks.AIR)) {
//                                Vec3d vec3d = range.subtract(new Vec3d(bb.maxX,bb.maxY,bb.maxZ));
//                                Vec3d vec3d1 = player.subtract(new Vec3d(bb.minX,bb.minY,bb.minZ));
//                            Test.log.log(Level.INFO,vec3d);
//                            Test.log.log(Level.INFO,vec3d1);
//                            Test.log.log(Level.INFO,bb);
//                            Test.log.log(Level.INFO,);
//                            AxisAlignedBB testBB = bb.expand(vec3d.distanceTo(vec3d1),vec3d.distanceTo(vec3d1),vec3d.distanceTo(vec3d1));
//                            Test.log.log(Level.INFO,testBB);
                                if (bb.offset(su.pos).calculateIntercept(player,range)!=null) {
                                    double dist = Math.abs(new Vec3d(su.pos).add(new Vec3d(su.sPos).scale(1f/su.sc)).distanceTo(player));
//                                double dist2 = su.pos.add(su.sPos).getDistance((int)player.x,(int)player.y,(int)player.z);
                                    if (dist<distBest) {
                                        result=bb.offset(su.pos);
                                        box=result;
                                        distBest=dist;
                                        block=su.bk;
                                        this.state=su.bk.getStateFromMeta(su.meta);
                                        this.scale=su.sc;
                                        hit=bb.offset(su.pos).calculateIntercept(player,range).hitVec.subtract(su.pos.getX(),su.pos.getY(),su.pos.getZ());
                                        side=bb.offset(su.pos).calculateIntercept(player,range).sideHit;
                                        subPos=su.sPos;
                                        double speed=this.state.getBlockHardness(worldIn,subPos)/(Minecraft.getMinecraft().player.getDigSpeed(this.state)*2);
                                        if (speed<=0) {
//                                            Minecraft.getMinecraft().player.getDigSpeed()
                                            speed=0;
                                        }
                                        hardness((float)speed);
                                    }
                                }
                            }
                        } catch (NullPointerException err) {}
                    }
//                }
//                        try {
//                            if (bb.offset(su.pos).contains(testPos)) {
//                                hardness(su.bk.getStateFromMeta(su.meta).getPlayerRelativeBlockHardness(Minecraft.getMinecraft().player,worldIn,pos));
//                                testPos.subtract(playerLook);
//                                hitX=testPos.x;
//                                hitY=testPos.y;
//                                hitZ=testPos.z;
//                                return bb.offset(su.pos);
//                            }
//                        } catch (NullPointerException err) {
//                            return super.getSelectedBoundingBox(state,worldIn,pos);
//                        }
//                    }
//                }
                }
            } catch (ConcurrentModificationException err) {
//            return box;
//                result=new AxisAlignedBB(0,0,0,0,0,0);
            } catch (NullPointerException err) {
//            return box;
//                result=new AxisAlignedBB(0,0,0,0,0,0);
            }
//        SmallerUnitsMod.log.log(Level.INFO,result);
//        SmallerUnitsMod.log.log(Level.INFO,new AxisAlignedBB(0,0,0,0,0,0));
            if (result.equals(new AxisAlignedBB(0,0,0,0,0,0))) {
                Vec3d range = (player.add(playerLook.scale(py.REACH_DISTANCE.getDefaultValue())));
//            for (double j=0; j<=60; j+=0.1) {
//                Vec3d testPos=new Vec3d(playerLook.x*j,playerLook.y*j,playerLook.z*j).add(player);
//                try {
//                    BlockPos testBPos = new BlockPos((int)testPos.x,(int)testPos.y,(int)testPos.z);
//                    IBlockState bk = worldIn.getBlockState(testBPos);
//                    if (!testBPos.equals(pos)&&!bk.equals(Blocks.AIR.getDefaultState())) {
////                    result=bb.offset(su.pos);
//                        box=result;
////                    distBest=dist;
//                        block=Blocks.STONE;
//                        this.state=Blocks.STONE.getDefaultState();
//                        hit=new Vec3d(0,0,0);
//                        side=EnumFacing.UP;
//                        subPos=new BlockPos(0,-1,0);
//                        hardness(-1.0f);
//                        return bk.getBoundingBox(worldIn,testBPos).offset(testBPos);
//                    } else if (!testBPos.equals(pos)) {
//                        return new AxisAlignedBB(0,0,0,0,0,0);
//                    }
//                } catch (NullPointerException err) {
//                    return super.getSelectedBoundingBox(state,worldIn,pos);
//                }
//            }
//            SmallerUnitsMod.log.log(Level.INFO,"OtherBlocks");
                double bestDist=Double.POSITIVE_INFINITY;
                for (int x=-1;x<=1;x++) {
                    for (int y=-1;y<=1;y++) {
                        for (int z=-1;z<=1;z++) {
                            if (!(x!=0&&z!=0&&y!=0)&&!(pos.equals(pos.add(new BlockPos(x,y,z))))) {
                                if (!worldIn.getBlockState(pos.add(new BlockPos(x,y,z))).getBlock().equals(this)&&
                                        !worldIn.getBlockState(pos.add(new BlockPos(x,y,z))).getMaterial().isLiquid()&&
                                        worldIn.getBlockState(pos.add(new BlockPos(x,y,z))).getBoundingBox(worldIn,new BlockPos(x,y,z)).equals(FULL_BLOCK_AABB)&&
                                        !worldIn.getBlockState(pos.add(new BlockPos(x,y,z))).getBlock().equals(Blocks.AIR)) {
                                    AxisAlignedBB bb = worldIn.getBlockState(new BlockPos(x,y,z).add(pos)).getBoundingBox(worldIn,new BlockPos(x,y,z).add(pos));
//                                SmallerUnitsMod.log.log(Level.INFO,bb.offset(new BlockPos(x,y,z).add(pos)));
                                    RayTraceResult intercept=bb.offset(new BlockPos(x,y,z).add(pos)).calculateIntercept(player,range);
                                    if (intercept!=null) {
                                        double dist = Math.abs(new Vec3d(new BlockPos(x,y,z).add(pos)).distanceTo(player));
                                        if (dist<=bestDist) {
                                            Vec3d hitVec=intercept.hitVec;
                                            Vec3d offset = new Vec3d(Math.ceil(hitVec.x*tile.getSize()),Math.ceil(hitVec.y*tile.getSize()),Math.ceil(hitVec.z*tile.getSize())).scale(1f/tile.getSize());
                                            double xOff=0;
                                            double yOff=0;
                                            double zOff=0;
                                            if (intercept.sideHit.equals(EnumFacing.WEST)) {
                                                xOff=(1d/tile.getSize());
                                            }
                                            if (intercept.sideHit.equals(EnumFacing.NORTH)) {
                                                zOff=(1d/tile.getSize());
                                            }
                                            if (intercept.sideHit.equals(EnumFacing.DOWN)) {
                                                yOff=(1d/tile.getSize());
                                            }
                                            result=new AxisAlignedBB(offset.x,offset.y,offset.z,offset.x+(1f/tile.getSize()),offset.y+(1f/tile.getSize()),offset.z+(1f/tile.getSize())).offset(new Vec3d(-(1d/tile.getSize())+xOff,-(1d/tile.getSize())+yOff,-(1d/tile.getSize())+zOff));
                                            Vec3d pos2=offset.subtract(pos.getX(),pos.getY(),pos.getZ()).scale(tile.size);
                                            SmallerUnitsMod.log.log(Level.INFO,pos2);
//                                            SmallerUnitsMod.log.log(Level.INFO,result);
//                                            SmallerUnitsMod.log.log(Level.INFO,pos2);
//                                            this.subPos=new BlockPos(pos2.x-1,pos2.y,pos2.z-1);
                                            this.subPos=new BlockPos(0,-1,0);
//                                            SmallerUnitsMod.log.log(Level.INFO,this.subPos);
                                            this.side=EnumFacing.UP;
//                                            SmallerUnitsMod.log.log(Level.INFO,this.subPos.offset(this.side));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
//        result=new AxisAlignedBB(0,0,0,1,1,1);
//            if (result.equals(new AxisAlignedBB(0,0,0,0,0,0))) {
//                return result;
//            }
            if (result.equals(new AxisAlignedBB(0,0,0,0,0,0))) {
                return result;
            } else {
                return result.shrink(0.0000000000949949026D);
            }
//        return super.getSelectedBoundingBox(state, worldIn, pos);
        } catch (NullPointerException err) {}
        return result.shrink(0.0000000000949949026D);
    }

    @SideOnly(Side.CLIENT)
    public void hardness(float hardness) {
        Currenthardness=hardness;
        setHardness(this.Currenthardness);
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return super.getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return super.getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return super.shouldCheckWeakPower(state, world, pos, side);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
//        return layer.equals(BlockRenderLayer.TRANSLUCENT);
        return false;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return false;
    }
//    test.test.world wo;
//    int tick=0;

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
//        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state) {
//        super.onPlayerDestroy(worldIn, pos, state);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
//        return super.removedByPlayer(state, world, pos, player, willHarvest);
        if (world.isRemote) {
            return false;
        }
        TileEntityCustom tile = (TileEntityCustom)world.getTileEntity(pos);
        boolean canBreak=tile.isEmpty();
        try {
            this.player=player;
//        getSelectedBoundingBox(state,world,pos);
            int x=subPos.getX();
            int y=subPos.getY();
            int z=subPos.getZ();
//        if (true) {
//            return false;
//        }
            int slot = x + (y * scale) + (z * scale * scale);
//        SmallerUnitsMod.log.log(Level.INFO,tile.getStackInSlot(slot).getItem());
//        readBlocks(tile,pos);
//        if (!world.isRemote&&!tile.getStackInSlot(slot).getItem().equals(new ItemAir(Blocks.AIR))) {
//        Object[] thisObjList=objects.get(posArrayList.indexOf(pos));
//        test.test.world wo = (test.test.world)thisObjList[0];
//        for (int x=0;x<tile.getSize();x++) {
//            for (int y = 0; y < tile.getSize(); y++) {
//                for (int z = 0; z < tile.getSize(); z++) {
//                    if (new BlockPos(x,y,z).equals(subPos)) {
            int fortune = EnchantmentHelper.getEnchantmentLevel(EnchantmentWaterWalker.getEnchantmentByID(35),player.getHeldItem(EnumHand.MAIN_HAND));
            ItemStack playerStack = player.getHeldItemMainhand();
            Item playerItem = playerStack.getItem();
            ItemTool playerTool=(ItemTool)ItemPickaxe.getByNameOrId("minecraft:wooden_pickaxe");
            try {
                playerTool=(ItemTool)playerItem;
            } catch (ClassCastException err) {}
            boolean correctTool=false;
            int harvestLevel = 0;
            for (Object string:playerTool.getToolClasses(playerStack).toArray()) {
                try {
                    if (((String)string).equals(this.block.getHarvestTool(this.state))) {
                        correctTool=true;
                        harvestLevel=playerTool.getHarvestLevel(playerStack,(String)string,player,this.state);
                    }
                } catch (Exception err) {}
            }
            int blockHarvest=block.getHarvestLevel(this.state)-1;
            if (!player.isCreative()&&(correctTool||blockHarvest<0)&&harvestLevel>=blockHarvest) {
                List<ItemStack> stks = block.getDrops(wo,subPos.add(new BlockPos(scale/2,128-scale/2,scale/2)),this.state,fortune);
//                if (tile.getStackInSlot(slot).equals(new ItemStack((Item)null))) {
                for (ItemStack stk:stks) {
                    EntityItem itm = new EntityItem(world,pos.getX()+(subPos.getX()/scale),pos.getY()+(subPos.getY()/scale),pos.getZ()+(subPos.getZ()/scale),stk);
                    itm.rotationPitch=new Random().nextInt(360);
                    world.spawnEntity(itm);
                }
//                }
            }
//            world.notifyBlockUpdate(pos,state,state,3);
//                    }
//                }
//            }
//        }
//        }
            ItemStack stk=new ItemStack(Items.BOOK);
            stk.setStackDisplayName("minecraft:air");
            if (block.getBlockHardness(this.state,null,null)!=-1||player.isCreative()) {
                tile.setStackInSlot(slot, stk);
            }
            this.player=null;
            if (canBreak) {
                world.setBlockState(pos,Blocks.AIR.getDefaultState());
                ItemStack stk2 = new ItemStack(this);
                EntityItem itm = new EntityItem(world,pos.getX()+(subPos.getX()/scale),pos.getY()+(subPos.getY()/scale),pos.getZ()+(subPos.getZ()/scale),stk2);
                itm.rotationPitch=new Random().nextInt(360);
                world.spawnEntity(itm);
            }
        } catch (NullPointerException err) {}
        return canBreak;
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

//    ArrayList<Object[]> objects = new ArrayList<>();
//    ArrayList<BlockPos> posArrayList = new ArrayList<>();

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        TileEntityCustom tile = (TileEntityCustom)worldIn.getTileEntity(pos);
        world wo = SmallerUnitsMod.getWorld(pos,worldIn.isRemote);
        tile.shouldRefresh(worldIn,pos,state,state);
        ArrayList<BlockPos> changes = new ArrayList<>();
        try {
            if (wo.equals(null)) {
                wo=new world(worldIn.getSaveHandler(),worldIn.getWorldInfo(),worldIn.provider,worldIn.profiler,worldIn.isRemote,worldIn,((TileEntityCustom)worldIn.getTileEntity(pos)).size,pos);
            }
        } catch (NullPointerException err) {
            wo=new world(worldIn.getSaveHandler(),worldIn.getWorldInfo(),worldIn.provider,worldIn.profiler,worldIn.isRemote,worldIn,((TileEntityCustom)worldIn.getTileEntity(pos)).size,pos);
        }
        try {
            readBlocks(tile,pos);
        } catch (NoSuchMethodError err) {}
//        if (!posArrayList.contains(pos)) {
//            Object[] thisObjList = new Object[3];
//            thisObjList[0]=new world(worldIn.getSaveHandler(),worldIn.getWorldInfo(),worldIn.provider,worldIn.profiler,worldIn.isRemote);
//            thisObjList[1]=new ArrayList<Object>();
//            thisObjList[2]=new ArrayList<smallUnit>();
//            objects.add(thisObjList);
//            posArrayList.add(pos);
//        }
//        Object[] thisObjList=objects.get(posArrayList.indexOf(pos));
//        ArrayList<smallUnit> SmallUnits = (ArrayList<smallUnit>)thisObjList[2];
//        test.test.world wo = (test.test.world)objects.get(posArrayList.indexOf(pos))[0];
        for (int x=0;x<tile.getSize();x++) {
            for (int y = 0; y < tile.getSize(); y++) {
                for (int z = 0; z < tile.getSize(); z++) {
                    int slot = x + (y * tile.getSize()) + (z * tile.getSize() * tile.getSize());
                    BlockPos pos1 = new BlockPos(x+tile.getSize()/2,y+128-tile.getSize()/2,z+tile.getSize()/2);
                    ItemStack bk = tile.getStackInSlot(slot);
                    ItemStack stk = bk;
                    IBlockState ste=Blocks.AIR.getDefaultState();
                    if (wo.getBlockState(pos1).equals(Blocks.AIR.getDefaultState())||!wo.getBlockState(pos1).equals(ste)) {
//                        if (tick<=3||tile.getStackInSlot(slot).getCount()==2) {
                            ste=teHelper.getStateFromStack(stk,tile,pos1);
                            try {
                                wo.setBlockState(pos1, ste);
                                wo.addBlockEvent(pos1,ste.getBlock(),2,2);
                                try {
                                    wo.notifyNeighborsOfStateChange(pos1,ste.getBlock(),true);
                                } catch (Exception err) {}
                                try {
//                                if (tile.wo.isBlockTickPending(pos1,Block.getBlockFromItem(bk.getItem()))) {
                                    if (ste.getBlock().hasTileEntity(ste)) {
                                        TileEntity te = ste.getBlock().createTileEntity(wo,ste);
                                        te.readFromNBT(bk.getSubCompound("BlockEntityTag"));
                                        wo.setTileEntity(pos1,te);
                                    }
//                                }
                                } catch (IllegalArgumentException err) {} catch (NullPointerException err) {}
                            } catch (Exception err) {}
                            stk = new ItemStack(tile.getStackInSlot(slot).getItem(),1);
                            stk.setItemDamage(tile.getStackInSlot(slot).getItemDamage());
                            tile.setStackInSlot(slot,stk);
                            changes.add(pos1);
//                        }
                    }
                }
            }
        }
        saveWorld(tile,changes,worldIn,wo,rand,true);
//        tick+=1;
        changes.clear();
        for (placeEvent evt: dataHandler.placeEvents) {
            try {
                if (evt.blockPos.equals(pos.toString())) {
                    if (!evt.item.equals("smallunits:su")) {
                        tile.setStackInSlot(evt.slot,new ItemStack(Item.getByNameOrId(evt.item),1));
                        dataHandler.placeEvents.set(dataHandler.placeEvents.indexOf(evt),null);
                    }
                }
            } catch (NullPointerException err) {}
//            wo.tick();
        }
//        if (!Test.subs.contains((TileEntityCustom)worldIn.getTileEntity(pos))) {
//            Test.subs.add((TileEntityCustom)worldIn.getTileEntity(pos));
//        }
//        TileEntityCustom te=(TileEntityCustom)worldIn.getTileEntity(pos);
        worldIn.scheduleBlockUpdate(pos,this,1,1);
//        try {
//            Double dist = Minecraft.getMinecraft().player.getPosition().distanceSq(pos.getX(),pos.getY(),pos.getZ());
//            if (readCooldown>=60) {
//                readCooldown=60;
//            }
//            SmallUnits=((TileEntityCustom)worldIn.getTileEntity(pos)).getUnits();
//            if ((readCooldown<=0||needsReread||SmallUnits.size()<tile.getSizeInventory())) {
//        if (worldIn.isRemote) {
//            if (readCooldown>=100) {
//            }
//            readBlocks((TileEntityCustom)worldIn.getTileEntity(pos),pos);
//            readCooldown+=1;
//        }
        SmallerUnitsMod.setUnit(pos,(ArrayList<smallUnit>)SmallUnits.clone());
        SmallerUnitsMod.setWorld(pos,worldIn.isRemote,wo);
//            } else {
//            Test.toDraw.clear();
//                readCooldown-=1;
//            }
//        } catch (NullPointerException err) {} catch (NoSuchMethodError err) {}
        try {
            sendBlocks((TileEntityCustom)worldIn.getTileEntity(pos),pos);
        } catch (ArrayIndexOutOfBoundsException err) {} catch (NoSuchMethodError err) {}
//        disp(state,worldIn,pos,rand);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    public void saveWorld(TileEntityCustom tile,ArrayList<BlockPos> changes,World worldIn,world wo,Random rand,boolean tickFirst){
        for (int x=0;x<tile.getSize();x++) {
            for (int y = 0; y < tile.getSize(); y++) {
                for (int z = 0; z < tile.getSize(); z++) {
                    int slot = x + (y * tile.getSize()) + (z * tile.getSize() * tile.getSize());
//                    if (.slot) {
                    try {
                        if (tickFirst) {
                            BlockPos pos1 = new BlockPos(x+tile.getSize()/2,y+128-tile.getSize()/2,z+tile.getSize()/2);
                            ItemStack bk = tile.getStackInSlot(slot);
                            IBlockState ste = teHelper.getStateFromStack(bk,tile,pos1);
                            for (BlockPos change:changes) {
                                wo.observedNeighborChanged(pos1,ste.getBlock(),change);
                            }
                            if (ste.getBlock().getTickRandomly()) {
                                int modifier = 1;
                                if (ste.getBlock() instanceof BlockFluidBase||
                                    ste.getBlock() instanceof BlockLiquid) {
                                    modifier=300;
                                }
                                if (rand.nextInt(ste.getBlock().tickRate(worldIn)*worldIn.getGameRules().getInt("randomTickSpeed")/modifier)<=worldIn.getGameRules().getInt("randomTickSpeed")) {
                                    ste.getBlock().randomTick(wo,pos1,ste,rand);
                                }
                            } else {
//                                ste.getBlock().randomTick(wo,pos1,ste,rand);
                                ste.getBlock().updateTick(wo,pos1,ste,rand);
                            }
                        }
//                        if (tick>=4) {
                        for (int x2=0;x2<tile.getSize();x2++) {
                            for (int y2 = 0; y2 < tile.getSize(); y2++) {
                                for (int z2 = 0; z2 < tile.getSize(); z2++) {
                                    int slot2 = x2 + (y2 * tile.getSize()) + (z2 * tile.getSize() * tile.getSize());
//                                        if (tile.getStackInSlot(slot2).getCount()==1) {
                                    BlockPos pos2 = new BlockPos(x2+tile.getSize()/2,y2+128-tile.getSize()/2,z2+tile.getSize()/2);
                                    ItemStack stk = new ItemStack(Items.BOOK,1);
//                                    stk.setStackDisplayName((tile.wo.getBlockState(pos2).getBlock().getRegistryName().toString()));
                                    if (wo.getTileEntity(pos2)!=null) {
                                        stk = teHelper.storeTEInStack(stk,wo.getTileEntity(pos2));
                                    }
                                    stk.setStackDisplayName((wo.getBlockState(pos2).getBlock().getRegistryName().toString()));
//                                            if (tile.getStackInSlot(slot2).equals(stk)) {
//                                                stk = new ItemStack(Item.getItemFromBlock(wo.getBlockState(pos2).getBlock()),1);
//                                            } else {
//                                                stk = new ItemStack(Item.getItemFromBlock(wo.getBlockState(pos2).getBlock()),2);
//                                            }
                                    stk.setItemDamage(wo.getBlockState(pos2).getBlock().getMetaFromState(wo.getBlockState(pos2)));
                                    tile.setStackInSlot(slot2,stk);
//                                        }
                                }
                            }
                        }
//                        }
//                        wo.updateBlockTick(pos1,wo.getBlockState(pos1).getBlock(),1,1);
                    } catch (NullPointerException err) {
//                        wo=new world(worldIn.getSaveHandler(),worldIn.getWorldInfo(),worldIn.provider,worldIn.profiler,worldIn.isRemote);
                    } catch (IllegalArgumentException err) {}
//                    }
                }
            }
        }
    }

    world wo;

    public boolean needsReread=false;
    public ArrayList<smallUnit> SmallUnits = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    public ArrayList readBlocks (TileEntityCustom tile, BlockPos pos) {
//        Object[] thisObjList=objects.get(posArrayList.indexOf(pos));
//        ArrayList<smallUnit> SmallUnits = (ArrayList<smallUnit>)thisObjList[2];
////        ArrayList<smallUnit> units = new ArrayList<>();
//        if (Test.toDraw.size()>=8) {
////            SmallUnits.clear();
////            packet.isReading=true;
//            try {
//                for (int i=Test.positions.indexOf(pos);i<Test.positions.lastIndexOf(pos);i++) {
//                    smallUnit su=Test.toDraw.get(i);
//                    if (su.pos.equals(pos)) {
//                        SmallUnits.add(su);
////                Test.log.log(Level.INFO,"s");
//                    }
//                }
//                readCooldown=(int)Minecraft.getMinecraft().player.getDistanceSq(pos);
//                needsReread=false;
//            } catch (ConcurrentModificationException err) {
//                needsReread=true;
//            } catch (IndexOutOfBoundsException err) {
//                needsReread=true;
//            }
////            if (units.size()>=SmallUnits.size()) {
////                SmallUnits=units;
////            tile.setUnits(SmallUnits);
////            }
//
////            packet.isReading=false;
////        tile.setSUs(SmallUnits);
//        }
//        SmallUnits.clear();
//        ArrayList<smallUnit> SmallUnits = new ArrayList<>();
        SmallUnits.clear();
        for (int x=0;x<tile.getSize();x++) {
            for (int y=0;y<tile.getSize();y++) {
                for (int z=0;z<tile.getSize();z++) {
                    int slot = x+(y*tile.getSize())+(z*tile.getSize()*tile.getSize());
//                    MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
//                    PlayerList list = srv.getPlayerList();
                    ItemStack stack = tile.getStackInSlot(slot);
                    Item item = stack.getItem();
                    ItemStack stk = stack;
                    IBlockState ste=Blocks.AIR.getDefaultState();
                    if (stk.getItem() instanceof ItemSeeds) {
                        ItemSeeds sds = (ItemSeeds)stk.getItem();
                        ste=sds.getPlant(wo,subPos);
                    } else if (stk.getItem() instanceof ItemSeedFood) {
                        ItemSeedFood sds = (ItemSeedFood)stk.getItem();
                        ste=sds.getPlant(wo,subPos);
                    } else if (stk.getItem() instanceof ItemBucket) {
                        ItemBucket bck = (ItemBucket) stk.getItem();
                        FluidBucketWrapper fbw = new FluidBucketWrapper(stk);
                        try {
                            ste = fbw.getFluid().getFluid().getBlock().getDefaultState();
                        } catch (NullPointerException err) {}
                    } else if (stk.getItem() instanceof ItemBlock) {
                        ItemBlock blk = (ItemBlock)stk.getItem();
                        ste=blk.getBlock().getStateFromMeta(stk.getItemDamage());
                    } else if (stk.getItem() instanceof ItemBlockSpecial) {
                        ItemBlockSpecial blk = (ItemBlockSpecial)stk.getItem();
                        ste=blk.getBlock().getStateFromMeta(stk.getItemDamage());
                    } else if (stk.getItem().equals(Items.REDSTONE)) {
                        Block blk = Blocks.REDSTONE_WIRE;
                        ste=blk.getStateFromMeta(stk.getItemDamage());
                    } else if (stk.getItem().equals(Items.FLINT_AND_STEEL)) {
                        Block blk = Blocks.FIRE;
                        ste=blk.getStateFromMeta(stk.getItemDamage());
                    } else {
                        Block blk = Block.getBlockFromName(stk.getDisplayName());
                        try {
                            ste=blk.getStateFromMeta(stk.getItemDamage());
                        } catch (NullPointerException err) {}
                    }
//                    Block bk = Block.getBlockFromItem(item);
                    Block bk=ste.getBlock();
//                    try {
//                        SmallUnits.set(slot,new smallUnit(bk,tile.getSize(),stack.getItemDamage(),pos,new BlockPos(x,y,z)));
//                    } catch (IndexOutOfBoundsException err) {
                        SmallUnits.add(new smallUnit(bk,tile.getSize(),stack.getItemDamage(),pos,new BlockPos(x,y,z)));
//                    }
//                    list.sendMessage(new TextComponentString("≈block"+bk.getRegistryName()+"≈posX"+pos.getX()+"≈posY"+pos.getY()+"≈posZ"+pos.getZ()+"≈sPosX"+x+"≈sPosY"+y+"≈sPosZ"+z+"≈scale"+tile.getSize()+"≈meta"+stack.getItemDamage()+"≈done"));
                }
            }
        }
        return SmallUnits;
    }
    @SideOnly(Side.SERVER)
    public void sendBlocks(TileEntityCustom tile, BlockPos pos) {
//        for (int x=0;x<tile.getSize();x++) {
//            for (int y=0;y<tile.getSize();y++) {
//                for (int z=0;z<tile.getSize();z++) {
//                    int slot = x+(y*tile.getSize())+(z*tile.getSize()*tile.getSize());
//                    MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
//                    PlayerList list = srv.getPlayerList();
//                    ItemStack stack = tile.getStackInSlot(slot);
//                    Item item = stack.getItem();
//                    Block bk = Block.getBlockFromItem(item);
//                    list.sendMessage(new TextComponentString("≈block"+bk.getRegistryName()+"≈posX"+pos.getX()+"≈posY"+pos.getY()+"≈posZ"+pos.getZ()+"≈sPosX"+x+"≈sPosY"+y+"≈sPosZ"+z+"≈scale"+tile.getSize()+"≈meta"+stack.getItemDamage()+"≈done"));
//                }
//            }
//        }
        MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
        srv.getPlayerList().sendToAllNearExcept(null,pos.getX(),pos.getY(),pos.getZ(),128,tile.getWorld().getUniqueDataId(tile.getWorld().getWorldInfo().getWorldName()),tile.getUpdatePacket());
    }

    @Override
    public Material getMaterial(IBlockState state) {
//        try {
//            if (mtrl!=null&&!mtrl.equals(null)) {
//                return mtrl;
//            }
//        } catch (NullPointerException err) {}
//        try {
//            if (!this.state.equals(state)) {
//                return block.getMaterial(this.state);
//            }
//            return super.getMaterial(state);
//        } catch (NullPointerException err) {}
//        return super.getMaterial(state);
        return Blocks.WHEAT.getMaterial(Blocks.WHEAT.getDefaultState());
    }

    @Override
    public SoundType getSoundType() {
        try {
            if (!this.state.equals(this.getDefaultState())) {
                return block.getSoundType();
            }
            return block.getSoundType();
        } catch (NullPointerException err) {}
        try {
            return block.getSoundType();
        } catch (NullPointerException err) {
            return Blocks.DIRT.getSoundType();
        }
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return false;
    }

    @Override
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        try {
            try {
//                manager.add(pos,this.state);
                return false;
//                return block.addDestroyEffects(world,pos,manager);
            } catch (Exception err) {}
            return false;
        } catch (StackOverflowError err) {}
        return false;
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        try {
            try {
//                state = state.getActualState(this.world, pos);
//                int i = 4;
//
//                for (int j = 0; j < 4; ++j)
//                {
//                    for (int k = 0; k < 4; ++k)
//                    {
//                        for (int l = 0; l < 4; ++l)
//                        {
//                            double d0 = ((double)j + 0.5D) / 4.0D;
//                            double d1 = ((double)k + 0.5D) / 4.0D;
//                            double d2 = ((double)l + 0.5D) / 4.0D;
//                            BakedQuad qd = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(this.state).getQuads(this.state,this.side,0L).get(0);
//                            Particle pt = (new Particle(this.world, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, d0 - 0.5D, d1 - 0.5D, d2 - 0.5D));
//                            pt.setParticleTexture(qd.getSprite());
//                            manager.addEffect(pt);
//                        }
//                    }
//                }
                manager.addBlockDestroyEffects(pos,this.state);
                return false;
//                return block.addDestroyEffects(world,pos,manager);
            } catch (Exception err) {}
            return false;
        } catch (StackOverflowError err) {}
        return false;
    }
    //    @Override

//    @Override
//    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
//        AxisAlignedBB collision = NULL_AABB;
//        try {
//            collision=collidedBB(pos,true);
//        } catch (NoSuchMethodError err) {} catch (NullPointerException err) {}
//        return collision;
//    }
//
//    @Nullable
//    @Override
//    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
//        AxisAlignedBB collision = NULL_AABB;
//        try {
//            collision=collidedBB(pos,false);
//        } catch (NoSuchMethodError err) {} catch (NullPointerException err) {}
//        return collision;
//    }


    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        boolean isLadder=false;
        try {
            ArrayList<smallUnit> SUs = SmallerUnitsMod.getUnits(pos);
            World wo = SmallerUnitsMod.getWorld(pos,true);
            World wo2 = SmallerUnitsMod.getWorld(pos,false);
            for (smallUnit su:SUs) {
                ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
                try {
                    BlockPos offset = new BlockPos(su.sc/2,128-su.sc/2,su.sc/2);
                    BlockPos posInWo=su.sPos.add(offset);
                    try {
                        su.bk.addCollisionBoxToList(su.bk.getStateFromMeta(su.meta).getActualState(wo,posInWo), wo, new BlockPos(0, 0, 0), new AxisAlignedBB(0, 0, 0, 1, 1, 1), boxes, entity, true);
                    } catch (Exception err) {}
                    try {
                        su.bk.addCollisionBoxToList(su.bk.getStateFromMeta(su.meta).getActualState(wo2,posInWo), wo2, new BlockPos(0, 0, 0), new AxisAlignedBB(0, 0, 0, 1, 1, 1), boxes, entity, true);
                    } catch (Exception err) {}
//                    if (!(su.bk.getRegistryName().toString().equals("minecraft:air"))&&
//                            !su.bk.equals(null)&&
//                            !su.bk.equals(net.minecraft.init.Blocks.AIR)&&
//                            su.pos.equals(pos)) {
                        try {
                            boxes.add(su.bk.getStateFromMeta(su.meta).getActualState(wo,posInWo).getBoundingBox(wo,su.sPos));
                        } catch (Exception err) {}
                        try {
                            boxes.add(su.bk.getStateFromMeta(su.meta).getActualState(wo2,posInWo).getBoundingBox(wo2,su.sPos));
                        } catch (Exception err) {}
//                    }
                } catch (IllegalArgumentException err) {} catch (NullPointerException err) {}
                try {
                    for (AxisAlignedBB bb:boxes) {
                        bb=bb.offset(su.sPos);
                        AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
                        bb1=bb1.offset(pos);
//                        SmallerUnitsMod.log.log(Level.INFO,"h"+entity.getEntityBoundingBox());
//                        SmallerUnitsMod.log.log(Level.INFO,"d"+bb1);
                        if (bb1.intersects(entity.getEntityBoundingBox())) {
                            if (su.bk.isLadder(null,null,null,null)) {
                                isLadder=true;
                            }
                        }
                    }
                } catch (NullPointerException err) {}
            }
        } catch (NullPointerException err) {}
        return isLadder;
    }

    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntityCustom tile = (TileEntityCustom)worldIn.getTileEntity(pos);
        ArrayList<smallUnit> SUs = SmallerUnitsMod.getUnits(pos);
        if (worldIn.isRemote||true) {
            World wo = SmallerUnitsMod.getWorld(pos,worldIn.isRemote);
            World wo2 = SmallerUnitsMod.getWorld(pos,false);
            try {
                for (smallUnit su:SUs) {
                    ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
                    try {
                        BlockPos offset = new BlockPos(su.sc/2,128-su.sc/2,su.sc/2);
                        BlockPos posInWo=su.sPos.add(offset);
                        try {
                            su.bk.addCollisionBoxToList(su.bk.getStateFromMeta(su.meta).getActualState(wo,posInWo), wo, new BlockPos(0, 0, 0), new AxisAlignedBB(0, 0, 0, 1, 1, 1), boxes, entityIn, true);
                        } catch (Exception err) {}
                        try {
                            su.bk.addCollisionBoxToList(su.bk.getStateFromMeta(su.meta).getActualState(wo2,posInWo), wo2, new BlockPos(0, 0, 0), new AxisAlignedBB(0, 0, 0, 1, 1, 1), boxes, entityIn, true);
                        } catch (Exception err) {}
//                        if (!(su.bk.getRegistryName().toString().equals("minecraft:air"))&&
//                            !su.bk.equals(null)&&
//                            !su.bk.equals(net.minecraft.init.Blocks.AIR)&&
//                            su.pos.equals(pos)) {
//                            try {
//                                boxes.add(su.bk.getStateFromMeta(su.meta).getActualState(wo,posInWo).getBoundingBox(wo,su.sPos));
//                            } catch (Exception err) {}
//                            try {
//                                boxes.add(su.bk.getStateFromMeta(su.meta).getActualState(wo2,posInWo).getBoundingBox(wo2,su.sPos));
//                            } catch (Exception err) {}
//                        }
                    } catch (IllegalArgumentException err) {} catch (NullPointerException err) {}
//                    boxes.add(su.bk.getStateFromMeta(su.meta).getCollisionBoundingBox(wo,su.sPos));
                    if (boxes.size()!=0) {
                        for (int i=0;i<boxes.size();i++) {
                            AxisAlignedBB bb=boxes.get(i);
                            try {
                                bb=bb.offset(su.sPos);
                                AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
                                bb1=bb1.offset(pos);
                                if (!worldIn.isRemote) {
                                    if (entityBox.intersects(bb1)) {
                                        int slot = su.sPos.getX()+(su.sPos.getY()*tile.getSize())+(su.sPos.getZ()*tile.getSize()*tile.getSize());
                                        BlockPos offset = new BlockPos(su.sc/2,128-su.sc/2,su.sc/2);
                                        BlockPos pos2=su.sPos.add(offset);
                                        su.bk.onEntityCollision(SmallerUnitsMod.getWorld(pos,false).wo,pos2,su.bk.getStateFromMeta(su.meta),entityIn);
                                        ItemStack stk = new ItemStack(Items.BOOK,1);
                                        if (wo.getTileEntity(pos2)!=null) {
                                            stk = teHelper.storeTEInStack(stk,wo.getTileEntity(pos2));
                                        }
                                        stk.setStackDisplayName((wo.getBlockState(pos2).getBlock().getRegistryName().toString()));
                                        stk.setItemDamage(wo.getBlockState(pos2).getBlock().getMetaFromState(wo.getBlockState(pos2)));
                                        tile.setStackInSlot(slot,stk);
//                                        if (su.bk instanceof BlockPressurePlate ||
//                                            su.bk instanceof BlockButton) {
//                                               super.addCollisionBoxToList(new BlockPos(0,0,0), entityBox, collidingBoxes, bb1);
//                                        }
                                    }
                                }
//                                if (i!=boxes.size()-1) {
//                                    SmallerUnitsMod.log.log(Level.INFO,i);
                                    if (su.bk.getStateFromMeta(su.meta).getMaterial().blocksMovement()) {
                                        super.addCollisionBoxToList(new BlockPos(0,0,0), entityBox, collidingBoxes, bb1);
                                    }
//                                }
                            } catch (NullPointerException err) {}
                        }
                    }
                }
            } catch (NullPointerException err) {}
        } else {
//            for (int x=0;x<tile.getSize();x++) {
//                for (int y = 0; y < tile.getSize(); y++) {
//                    for (int z = 0; z < tile.getSize(); z++) {
//                        int slot = x+(y*tile.getSize())+(z*tile.getSize()*tile.getSize());
//                        ItemStack stack = tile.getStackInSlot(slot);
////                        Item item = stack.getItem();
//                        IBlockState ste = teHelper.getStateFromStack(stack,tile,new BlockPos(x,y,z));
//                        Block bk = ste.getBlock();
//                        smallUnit su = (new smallUnit(bk,tile.getSize(),stack.getItemDamage(),pos,new BlockPos(x,y,z)));
////                        SmallerUnitsMod.log.log(Level.INFO,su.sPos);
////                        SmallerUnitsMod.log.log(Level.INFO,su.sc);
//                        ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
//                        ArrayList<AxisAlignedBB> eventColliders = new ArrayList<>();
//                        try {
//                            su.bk.addCollisionBoxToList(ste, wo, new BlockPos(0, 0, 0), new AxisAlignedBB(0, 0, 0, 1, 1, 1), boxes, entityIn, true);
//                            eventColliders.add(su.bk.getStateFromMeta(su.meta).getBoundingBox(wo,su.sPos));
//                        } catch (IllegalArgumentException err) {} catch (NullPointerException err) {}
////                        boxes.add(su.bk.getStateFromMeta(su.meta).getCollisionBoundingBox(wo,su.sPos));
////                        if () {
//                            for (AxisAlignedBB bb : boxes) {
//                                try {
//                                    bb=bb.offset(su.sPos);
//                                    AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
//                                    bb1=bb1.offset(pos);
//                                    if (entityBox.intersects(bb1)) {
//                                        BlockPos offset = new BlockPos(su.sc/2,128-su.sc/2,su.sc/2);
//                                        BlockPos pos2=su.sPos.add(offset);
//                                        ItemStack stk = new ItemStack(Items.BOOK,1);
//                                        if (wo.getTileEntity(pos2)!=null) {
//                                            stk = teHelper.storeTEInStack(stk,wo.getTileEntity(pos2));
//                                        }
//                                        stk.setStackDisplayName((wo.getBlockState(pos2).getBlock().getRegistryName().toString()));
//                                        stk.setItemDamage(wo.getBlockState(pos2).getBlock().getMetaFromState(wo.getBlockState(pos2)));
//                                        tile.setStackInSlot(slot,stk);
//                                    }
//                                    super.addCollisionBoxToList(new BlockPos(0,0,0), entityBox, collidingBoxes, bb1);
//                                } catch (NullPointerException err) {}
//                            }
////                        } else {
//                            for (AxisAlignedBB bb : eventColliders) {
//                                try {
//                                    bb=bb.offset(su.sPos);
//                                    AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
//                                    bb1=bb1.offset(pos);
//                                    if (entityBox.intersects(bb1)) {
//                                        BlockPos offset = new BlockPos(su.sc/2,128-su.sc/2,su.sc/2);
//                                        BlockPos pos2=su.sPos.add(offset);
//                                        ItemStack stk = new ItemStack(Items.BOOK,1);
//                                        if (wo.getTileEntity(pos2)!=null) {
//                                            stk = teHelper.storeTEInStack(stk,wo.getTileEntity(pos2));
//                                        }
//                                        stk.setStackDisplayName((wo.getBlockState(pos2).getBlock().getRegistryName().toString()));
//                                        stk.setItemDamage(wo.getBlockState(pos2).getBlock().getMetaFromState(wo.getBlockState(pos2)));
//                                        tile.setStackInSlot(slot,stk);
////                                        SmallerUnitsMod.log.log(Level.INFO,bb1);
//                                    }
//                                    if (su.bk instanceof BlockPressurePlate ||
//                                        su.bk instanceof BlockButton) {
//                                        super.addCollisionBoxToList(new BlockPos(0,0,0), entityBox, collidingBoxes, bb1);
//                                    }
//                                } catch (NullPointerException err) {}
//                            }
////                        }
//                    }
//                }
//            }
        }
//        super.addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
//        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }
    Material mtrl = null;
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB collidedBB(BlockPos pos,boolean realBB) {
        AxisAlignedBB returnBB = NULL_AABB;
        if (realBB) {
            returnBB=FULL_BLOCK_AABB;
        }
        try {
            for (smallUnit su:SmallUnits) {
                ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
                if (su.bk.getMaterial(su.bk.getStateFromMeta(su.meta)).blocksMovement()||realBB) {
                    boxes.add(su.bk.getStateFromMeta(su.meta).getCollisionBoundingBox(wo,su.sPos));
                }
                for (AxisAlignedBB bb:boxes) {
                    try {
                        bb.offset(su.sPos);
                        AxisAlignedBB bb1 = new AxisAlignedBB(bb.minX / su.sc, bb.minY / su.sc, bb.minZ / su.sc, bb.maxX / su.sc, bb.maxY / su.sc, bb.maxZ / su.sc);
                        bb1.offset(pos);
                        if (Minecraft.getMinecraft().player.getCollisionBoundingBox().intersects(bb1)) {
                            mtrl=su.bk.getMaterial(su.bk.getStateFromMeta(su.meta));
                            AxisAlignedBB intersection = Minecraft.getMinecraft().player.getCollisionBoundingBox().intersect(bb1).offset(new BlockPos(0,0,0).subtract(pos));
                            EntityPlayerSP py = Minecraft.getMinecraft().player;
                            if (intersection.maxX<=intersection.maxY&&intersection.maxX<=intersection.maxZ) {
                                py.setVelocity(0,py.motionY,py.motionZ);
                                py.setPosition(py.posX-intersection.maxX,py.posY,py.posZ);
                            }
                            if (realBB) {
                                returnBB=returnBB.union(bb1);
                            }
//                            return intersection;
                        }
//                    super.addCollisionBoxToList(pos, entityBox, collidingBoxes, bb1);
                    } catch (NullPointerException err) {}
                }
            }
        } catch (ConcurrentModificationException err) {}
        return returnBB;
    }

//    @Nullable
//    @Override
//    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
//        return NULL_AABB;
//    }

    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);
//    public static final PropertyBool MANUAL = PropertyBool.create("manual");
    BlockStateContainer container = new BlockStateContainer.Builder(this).add(LEVEL).build();
//    BlockStateContainer container2 = new BlockStateContainer.Builder(this).add(MANUAL).build();
    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(LEVEL).build();
    }

    @Override
    public BlockStateContainer getBlockState() {
        return new BlockStateContainer.Builder(this).add(LEVEL).build();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Nullable
    @Override
    public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn) {
        return super.isAABBInsideMaterial(world,pos,boundingBox,materialIn);
    }

    @Nullable
    @Override
    public Boolean isAABBInsideLiquid(World world, BlockPos pos, AxisAlignedBB boundingBox) {
        return super.isAABBInsideLiquid(world,pos,boundingBox);
    }

//    @Override
//    public float getBlockLiquidHeight(World world, BlockPos pos, IBlockState state, Material material) {
//        return 0;
//    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
//        if (FMLCommonHandler.instance().getSide().isClient()) {
//            Minecraft.getMinecraft().player.sendChatMessage("/say hi");
//        }
        super.onBlockAdded(worldIn, pos, state);
//        wo=new world(worldIn.getSaveHandler(),worldIn.getWorldInfo(),worldIn.provider,worldIn.profiler,worldIn.isRemote);
        worldIn.scheduleBlockUpdate(pos,this,1,1);
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
//        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        if (worldIn.isRemote) {
            return true;
        }
        TileEntityCustom tile = (TileEntityCustom)worldIn.getTileEntity(pos);
//        try {
//            readBlocks(tile,pos);
//        } catch (NoSuchMethodError err) {}
        this.player=player;
        getSelectedBoundingBox(state,world,pos);
        this.player=null;
//        test.test.world wo = (test.test.world)objects.get(posArrayList.indexOf(pos))[0];
        BlockPos posInSubWorld = new BlockPos(tile.getSize()/2,128-tile.getSize()/2,tile.getSize()/2);
        boolean useItem=false;
//        try {
//            if (server()) {
//                if (block.onBlockActivated(wo,subPos.add(posInSubWorld),this.state,playerIn,hand,facing,(float)hit.x,(float)hit.y,(float)hit.z)) {
//                }
//                tile.setInventorySlotContents(,playerIn.getHeldItem(hand));
//            }
//        } catch (NoSuchMethodError err) {
//            MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
            try {
                test.test.world wo = SmallerUnitsMod.getWorld(pos,worldIn.isRemote);
                Block bk = Block.getBlockFromItem(playerIn.getHeldItem(hand).getItem());
                BlockPos posInWo=subPos.add(posInSubWorld);
//                fakePlayer player = new fakePlayer(playerIn.getEntityWorld(),playerIn.getGameProfile(),playerIn,scale);
//                worldIn.spawnEntity(player);
                if (block.onBlockActivated(wo,posInWo,this.state,playerIn,hand,this.side,(float)hit.x,(float)hit.y,(float)hit.z)) {
                    block.onBlockClicked(wo,posInWo,playerIn);
                    TileEntity te2=null;
                    try {
                        te2=wo.getTileEntity(posInWo);
                        TileEntityLockableLoot te = (TileEntityLockableLoot)te2;
                        te2=te;
                        for (Constructor constructs : playerIn.openContainer.getClass().getConstructors()) {
                            SmallerUnitsMod.log.log(Level.INFO,constructs.toString());
                            IInventory inv = new IInventory() {
                                @Override
                                public int getSizeInventory() {
                                    return te.getSizeInventory();
                                }

                                @Override
                                public boolean isEmpty() {
                                    return te.isEmpty();
                                }

                                @Override
                                public ItemStack getStackInSlot(int index) {
                                    return te.getStackInSlot(index);
                                }

                                @Override
                                public ItemStack decrStackSize(int index, int count) {
                                    return te.decrStackSize(index,count);
                                }

                                @Override
                                public ItemStack removeStackFromSlot(int index) {
                                    return te.removeStackFromSlot(index);
                                }

                                @Override
                                public void setInventorySlotContents(int index, ItemStack stack) {
                                    te.setInventorySlotContents(index,stack);
                                }

                                @Override
                                public int getInventoryStackLimit() {
                                    return te.getInventoryStackLimit();
                                }

                                @Override
                                public void markDirty() {
                                    te.markDirty();
                                }

                                @Override
                                public boolean isUsableByPlayer(EntityPlayer player) {
                                    return true;
                                }

                                @Override
                                public void openInventory(EntityPlayer player) {
                                    te.openInventory(player);
                                }

                                @Override
                                public void closeInventory(EntityPlayer player) {
                                    te.closeInventory(player);
                                }

                                @Override
                                public boolean isItemValidForSlot(int index, ItemStack stack) {
                                    return te.isItemValidForSlot(index,stack);
                                }

                                @Override
                                public int getField(int id) {
                                    return te.getField(id);
                                }

                                @Override
                                public void setField(int id, int value) {
                                    te.setField(id,value);
                                }

                                @Override
                                public int getFieldCount() {
                                    return te.getFieldCount();
                                }

                                @Override
                                public void clear() {
                                    te.clear();
                                }

                                @Override
                                public String getName() {
                                    return te.getName();
                                }

                                @Override
                                public boolean hasCustomName() {
                                    return te.hasCustomName();
                                }

                                @Override
                                public ITextComponent getDisplayName() {
                                    return te.getDisplayName();
                                }
                            };
                            try {
//                                SmallerUnitsMod.log.log(Level.INFO,"h");
                                playerIn.openContainer=(Container)constructs.newInstance(playerIn,inv);
//                                SmallerUnitsMod.log.log(Level.INFO,"d");
                            } catch (Exception err) {
                                try {
//                                    SmallerUnitsMod.log.log(Level.INFO,"h");
                                    playerIn.openContainer=(Container)constructs.newInstance(playerIn.inventory,inv,player);
//                                    SmallerUnitsMod.log.log(Level.INFO,"d");
                                } catch (Exception err2) {}
                            }
                        }
                    } catch (Exception err) {}
//                    playerIn.openContainer=new forcedContainer(player.openContainer);
                    int x = Math.abs((subPos.getX()));
                    int y = Math.abs((subPos.getY()*tile.getSize()));
                    int z = Math.abs((subPos.getZ()*tile.getSize()*tile.getSize()));
                    IBlockState ste = wo.getBlockState(posInWo);
                    ItemStack stk = new ItemStack(Items.BOOK,1);
                    if (ste.getBlock().hasTileEntity(ste)) {
                        teHelper.storeTEInStack(stk,te2);
                    }
                    stk.setStackDisplayName(ste.getBlock().getRegistryName().toString());
                    stk.setItemDamage(ste.getBlock().getMetaFromState(ste));
                    tile.setStackInSlot((x+y+z),stk);
                } else {
                    BlockPos subPos=this.subPos.offset(this.side);
                    int x = Math.abs((int)(subPos.getX()));
                    int y = Math.abs((int)(subPos.getY()*tile.getSize()));
                    int z = Math.abs((int)(subPos.getZ()*tile.getSize()*tile.getSize()));
                    int meta = bk.getMetaFromState(bk.getStateForPlacement(worldIn,this.subPos,this.side,(float)hit.x,(float)hit.y,(float)hit.z,playerIn.getHeldItem(hand).getItem().getMetadata(playerIn.getHeldItem(hand).getItemDamage()),playerIn));
                    IBlockState ste = bk.getStateForPlacement(worldIn,this.subPos,this.side,(float)hit.x,(float)hit.y,(float)hit.z,playerIn.getHeldItem(hand).getItem().getMetadata(playerIn.getHeldItem(hand).getItemDamage()),playerIn);
                    ItemStack stk = playerIn.getHeldItem(hand).copy();
//                    stk.getItem().onItemUse(playerIn,wo,this.subPos,hand,this.side,(float)this.hit.x,(float)this.hit.y,(float)this.hit.z);
                    stk.setItemDamage(meta);
                    int x2 = Math.abs((int)(this.subPos.getX()));
                    int y2 = Math.abs((int)(this.subPos.getY()*tile.getSize()));
                    int z2 = Math.abs((int)(this.subPos.getZ()*tile.getSize()*tile.getSize()));

                    ItemStack stk2 = tile.getStackInSlot(x2+y2+z2);
                    int meta2 = stk2.getItemDamage();
//                    IBlockState ste2=teHelper.getStateFromStack();
                    stk2=new ItemStack(Block.getBlockFromName(stk2.getDisplayName()),1);
                    stk2.setItemDamage(meta2);
                    IBlockState ste2=Block.getBlockFromItem(stk2.getItem()).getStateFromMeta(meta2);
                    teHelper.getStateFromStack(stk2,tile,this.subPos);
//                    SmallerUnitsMod.log.log(Level.INFO,stk2);
//                    SmallerUnitsMod.log.log(Level.INFO,stk2.getDisplayName());
//                    SmallerUnitsMod.log.log(Level.INFO,ste2);

                    if (stk.getItem() instanceof ItemHoe) {
                        if (Block.getBlockFromItem(stk2.getItem()) instanceof BlockDirt||
                            Block.getBlockFromItem(stk2.getItem()) instanceof BlockGrass) {
                            tile.setStackInSlot(x2 + y2 + z2, new ItemStack(Item.getByNameOrId("minecraft:farmland")));
                        }
                    }
                    if (stk.getItem() instanceof ItemSpade) {
                        if (Block.getBlockFromItem(stk2.getItem()) instanceof BlockGrass) {
                            tile.setStackInSlot(x2 + y2 + z2, new ItemStack(Item.getByNameOrId("minecraft:grass_path")));
                        }
                    }
//                    SmallerUnitsMod.log.log(Level.INFO,new ItemStack(Blocks.REDSTONE_WIRE).getItem().getClass());
                    useItem=stk.getItem() instanceof ItemBlock ||
                            stk.getItem() instanceof ItemSeeds ||
                            stk.getItem() instanceof ItemBlockSpecial ||
                            stk.getItem() instanceof ItemBucket ||
                            stk.getItem() instanceof ItemSeedFood ||
                            stk.getItem().equals(Items.REDSTONE) ||
                            stk.getItem().equals(Items.FLINT_AND_STEEL);
                    ItemStack blockItem = new ItemStack(stk.getItem(),1);
                    blockItem.setStackDisplayName(stk.getItem().getRegistryName().toString());
                    blockItem.setItemDamage(stk.getItemDamage());
//                    blockItem.addEnchantment(Enchantment.getEnchantmentByID(1),1);

                    if (useItem) {
                        if (ste2.getMaterial().isReplaceable()||
                            ste2.getMaterial().isLiquid()) {
//                            if (ste.equals(bk.getStateFromMeta(meta))) {
                                tile.setStackInSlot((x2+y2+z2),blockItem);
//                            } else {
//                                NBTTagList stee = new NBTTagList();
//                                for (IProperty prop:ste.getPropertyKeys()) {
//                                    stee.appendTag(new NBTTagString(prop.getName()));
//                                    stee.appendTag(new NBTTagString(""+prop.parseValue(prop.getName())));
//                                }
//                                blockItem.setTagInfo("state",stee);
//                                tile.setStackInSlot((x2+y2+z2),blockItem);
//                            }
                        } else {
//                            if (ste.equals(bk.getStateFromMeta(meta))) {
                                tile.setStackInSlot((x+y+z),blockItem);
//                            } else {
//                                NBTTagList stee = new NBTTagList();
//                                for (IProperty prop:ste.getPropertyKeys()) {
//                                    stee.appendTag(new NBTTagString(prop.getName()));
//                                    stee.appendTag(new NBTTagString(""+prop.parseValue(prop.getName())));
//                                }
//                                blockItem.setTagInfo("state",stee);
//                                tile.setStackInSlot((x2+y2+z2),blockItem);
//                            }
                        }
                    }
//                    saveWorld(tile,new ArrayList<BlockPos>(),worldIn,new Random(0L),false);
//                    Minecraft.getMinecraft().player.sendChatMessage(("≈item"+playerIn.getHeldItem(hand).getItem().getRegistryName()+"≈meta"+meta+"≈slot"+(x+y+z)+"≈bp"+pos));
                    readCooldown=0;
                }
            } catch (NullPointerException err2) {} catch (RuntimeException err2) {}
//            srv.sendMessage();
//        }
        if (!playerIn.isCreative()) {
            try {
                Item itm = playerIn.getHeldItem(hand).getItem();
                if (useItem) {
                    useItem(playerIn,hand);
                }
            } catch (NoSuchMethodError err) {}
        }
        return true;
    }
//    @SideOnly(Side.SERVER)
    public void useItem(EntityPlayer player,EnumHand hand) {
        player.getHeldItem(hand).shrink(1);
    }

//    TileEntityCustom tilePick;
//
//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
//        if (itemIn.equals(Test.tabSubs)) {
//            ItemStack stack=this.getPickBlock(this.getDefaultState(),null,world,null,null);
//            stack.writeToNBT(this.tilePick.writeToNBT(new NBTTagCompound()));
//            items.add(stack);
//            super.getSubBlocks(itemIn, items);
//        }
//    }
    @SideOnly(Side.CLIENT)
    public boolean client() {
        return true;
    }
    @SideOnly(Side.SERVER)
    public boolean server() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(new block(Material.AIR,MapColor.DIRT)), 0, new ModelResourceLocation("smaller_units:smallerunit", "inventory"));
    }

    public World world=null;

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        return ((TileEntityCustom)world.getTileEntity(pos)).getItems().contains(Item.getByNameOrId("minecraft:flint_and_steel"));
    }

    @Nullable
    @Override
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox) {
//        double bestDist=Double.POSITIVE_INFINITY;
        boundingBox=new AxisAlignedBB(0,0,0,1,1,1);
        RayTraceResult result=super.rayTrace(pos,start,end,boundingBox);
//        try {
//            for (smallUnit su:SmallUnits) {
//            AxisAlignedBB unscaledBB = su.bk.getBoundingBox(su.bk.getStateFromMeta(0),null,new BlockPos(0,0,0));
//            AxisAlignedBB bb = new AxisAlignedBB(unscaledBB.minX/su.sc,unscaledBB.minY/su.sc,unscaledBB.minZ/su.sc,unscaledBB.maxX/su.sc,unscaledBB.maxY/su.sc,unscaledBB.maxZ/su.sc);
//            Vec3d vec3d = start.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
//            Vec3d vec3d1 = end.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
//            Test.log.log(Level.INFO,bb.minX);
//            Test.log.log(Level.INFO,bb.maxX);
//            Test.log.log(Level.INFO,bb.minY);
//            Test.log.log(Level.INFO,bb.maxY);
//            Test.log.log(Level.INFO,bb.minZ);
//            Test.log.log(Level.INFO,bb.maxZ);
//            Vec3d avg = new Vec3d(Math.abs(vec3d.add(vec3d1).x/4),Math.abs(vec3d.add(vec3d1).y/4),Math.abs(vec3d.add(vec3d1).z/4));
//            Test.log.log(Level.INFO,vec3d.x+","+vec3d.y+","+vec3d.z);
//            Test.log.log(Level.INFO,vec3d1.x+","+vec3d1.y+","+vec3d1.z);
//            Test.log.log(Level.INFO,avg.x+","+avg.y+","+avg.z);
//            double dist = su.pos.add(su.sPos).getDistance((int)start.x,(int)start.y,(int)start.z);
//            Test.log.log(Level.INFO,dist);
//            Test.log.log(Level.INFO,bestDist);
////            if ()
//            RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
//            if (bb.contains(avg)&&dist<=bestDist) {
//                result = boundingBox.calculateIntercept(vec3d, vec3d1);
//                bestDist=dist;
//            }
//            if (super.rayTrace(pos,start,end,bb)!=null&&dist<=bestDist) {
//                result=super.rayTrace(pos,start,end,bb);
//                bestDist=dist;
//            }
//            }
//        } catch (ConcurrentModificationException err) {}
        try {
            if (!getSelectedBoundingBox(this.getDefaultState(),this.world,pos).equals(new AxisAlignedBB(0,0,0,0,0,0))) {
                return result;
            } else {
                return null;
            }
        } catch (NullPointerException err) {}
        return result;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TileEntityCustom tile = new TileEntityCustom();
//        tile.setSize(4);
        return tile;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        TileEntityCustom tile = new TileEntityCustom();
//        tile.setSize(16);
        return tile;
    }

    ArrayList<TileEntityCustom> tes = new ArrayList<>();

    public static class TileEntityCustom extends TileEntityLockableLoot implements Comparable<TileEntityCustom> {
        private NonNullList<ItemStack> stacks = NonNullList.<ItemStack> withSize(9, ItemStack.EMPTY);

        @Override
        public int getSizeInventory() {
            return size*size*size;
        }

        @Override
        public int compareTo(TileEntityCustom o) {
            if (this.equals(o)) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            try {
                return this.getPos().equals(((TileEntityCustom)obj).getPos());
            } catch (ClassCastException err) {}
            return false;
        }

        @Nullable
        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            return new SPacketUpdateTileEntity(pos,1,getUpdateTag());
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            return writeToNBT(new NBTTagCompound());
        }

        @Override
        public void handleUpdateTag(NBTTagCompound tag) {
            super.handleUpdateTag(tag);
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            if (pkt.getPos().equals(pkt.getPos())) {
                handleUpdateTag(pkt.getNbtCompound());
            }
        }

        public int size=2;
        public int getSize() {
            return size;
        }
        public void setSize(int size) {
            this.size=size;
            stacks = NonNullList.<ItemStack> withSize(size*size*size, ItemStack.EMPTY);
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack itemstack : this.stacks)
                if (!itemstack.isEmpty())
                    return false;
            return true;
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks.get(slot);
        }

        public ItemStack setStackInSlot(int slot, Item item) {
            ItemStack stack = new ItemStack(item,1);
            try {
                stacks.set(slot,stack);
            } catch (NullPointerException err) {}
            return stack;
        }

        public ItemStack setStackInSlot(int slot, ItemStack item) {

            try {
                stacks.set(slot,item);
            } catch (ArrayIndexOutOfBoundsException err) {}
            return item;
        }
//        public IBlockState getStateInSlot(int slot) {
//            ItemStack stk = this.getStackInSlot(slot);
//            Block bk = Block.getBlockFromItem(stk.getItem());
//            NBTTagList state = stk.getTagCompound().getTagList("State",0);
//            IBlockState ste=bk.getDefaultState();
//            for (IProperty prop:ste.getProperties().keySet()) {
//                for (int i=0;i<state.tagCount();i+=2) {
//                    if (prop.getName().equals(state.getStringTagAt(i))) {
//                        ste=ste.withProperty(prop,state.getStringTagAt(i+1));
//                    }
//                }
//            }
//            return ste;
//        }
//        ArrayList<smallUnit> units = new ArrayList<>();
//        public ArrayList<smallUnit> getUnits() {
//            return units;
//        }
//        public void setUnits(ArrayList<smallUnit> units) {
//            this.units = units;
//        }
        @Override
        public String getName() {
            return this.hasCustomName() ? this.customName : "Smaller Unit";
        }

        //Minecraft mod development discord-KittyKitCatCat
        public ItemStackHandler inventory = new ItemStackHandler(3) {
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };

        boolean isManPlaced = true;

        @Nonnull
        @Override
        public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
            if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return (T)inventory;
            }
            return super.getCapability(cap, side);
        }

        world wo=null;

        @Override
        public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
            if (wo==null) {
//                wo=new world(world.getSaveHandler(),world.getWorldInfo(),world.provider,world.profiler,world.isRemote,world,1,pos);
            }
            return super.shouldRefresh(world, pos, oldState, newSate);
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            super.readFromNBT(compound);
            size=compound.getInteger("scale");
            this.stacks = NonNullList.<ItemStack> withSize(size*size*size, ItemStack.EMPTY);
            if (compound.hasKey("forced"))
                isManPlaced=compound.getBoolean("forced");
            if (!this.checkLootAndRead(compound))
                ItemStackHelper.loadAllItems(compound, this.stacks);
            if (compound.hasKey("CustomName", 8))
                this.customName = compound.getString("CustomName");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            super.writeToNBT(compound);
            compound.setInteger("scale",size);
            compound.setBoolean("forced",isManPlaced);
            if (!this.checkLootAndWrite(compound))
                ItemStackHelper.saveAllItems(compound, this.stacks);
            if (this.hasCustomName())
                compound.setString("CustomName", this.customName);
            return compound;
        }

        @Override
        public boolean isLocked() {
            return true;
        }

        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        @Override
        public String getGuiID() {
            return "smaller_units:smallerunit";
        }

        @Override
        public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
            this.fillWithLoot(playerIn);
            return new ContainerHopper(playerInventory, this, playerIn);
        }

        @Override
        protected NonNullList<ItemStack> getItems() {
            return this.stacks;
        }
    }
}
