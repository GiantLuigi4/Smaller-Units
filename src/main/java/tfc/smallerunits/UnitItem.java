package tfc.smallerunits;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.config.SmallerUnitsConfig;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.accessor.IRenderUnitsInBlocks;
import tfc.smallerunits.utils.data.SUCapability;
import tfc.smallerunits.utils.data.SUCapabilityManager;

public class UnitItem extends BlockItem {
	public UnitItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}
	
	//TODO:Fill item group with all pickblocked smaller units.
	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		for (int i = SmallerUnitsConfig.SERVER.minUPB.get(); i <= SmallerUnitsConfig.SERVER.maxUPB.get(); i++) {
			ItemStack stack = new ItemStack(Deferred.UNITITEM.get());
			CompoundNBT defaultNBT = new CompoundNBT();
			defaultNBT.putInt("upb", i);
			stack.getOrCreateTag().put("BlockEntityTag", defaultNBT);
			if (group.equals(Deferred.group)) {
				items.add(stack);
			}
		}
//		for (String s : Group.strings) {
//			ItemStack stack2 = new ItemStack(Deferred.UNITITEM.get());
//			CompoundNBT nbt = new CompoundNBT();
//			nbt.putString("world", s);
//			nbt.putInt("upb", 8);
//			stack2.getOrCreateTag().put("BlockEntityTag", nbt);
//			if (group.equals(Deferred.group)) {
//				items.add(stack2);
//			}
//		}
		super.fillItemGroup(group, items);
	}
	
	//Just incase fill item group doesn't work due to some other mod being dumb.
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		CompoundNBT defaultNBT = new CompoundNBT();
		defaultNBT.putInt("upb", 4);
		if (!stack.getOrCreateTag().contains("BlockEntityTag"))
			stack.getOrCreateTag().put("BlockEntityTag", defaultNBT);
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
//		File file = new File("smaller_units\\" + stack.getDisplayName().getString() + ".nbt");
//		try {
//			if (!file.exists()) {
//				file.getParentFile().mkdirs();
//				file.createNewFile();
//				CompressedStreamTools.write(stack.getOrCreateTag().getCompound("BlockEntityTag"), file);
//			}
//		} catch (Throwable ignored) {
//		}
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
//		return super.onItemUse(context);
		BlockItemUseContext blockItemUseContext = new BlockItemUseContext(context);
		BlockPos pos = blockItemUseContext.offsetPos;
//		System.out.println(pos);
		BlockState state = context.getWorld().getBlockState(pos);
		if (true || state.isAir(context.getWorld(), pos) || blockItemUseContext.replacingClickedOnBlock()) {
			return super.onItemUse(context);
		} else {
			VoxelShape shape = state.getCollisionShape(context.getWorld(), pos, ISelectionContext.dummy());
			// HAHA, YES
			// I REMEMBERED HOW VANILLA WOULD DO THIS
			VoxelShape ONLY_SECOND = VoxelShapes.combine(shape, VoxelShapes.create(0, 0, 0, 1, 1, 1), IBooleanFunction.ONLY_SECOND);
			if (!ONLY_SECOND.isEmpty()) {
				Chunk chunk = context.getWorld().getChunkAt(pos);
				
				ItemStack stack = blockItemUseContext.getItem();
				CompoundNBT nbt = stack.getOrCreateTag();
				if (nbt.contains("BlockEntityTag")) nbt = nbt.getCompound("BlockEntityTag");
				else nbt.putInt("upb", 4);
				
				LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
				if (capability.isPresent()) {
					SUCapability cap = capability.resolve().get();
					if (cap.getMap().containsKey(pos)) {
						return ActionResultType.FAIL;
					}
					UnitTileEntity tileEntity = new UnitTileEntity();
					tileEntity.setWorldAndPos(context.getWorld(), pos);
					tileEntity.deserializeNBT(nbt);
					tileEntity.setWorldAndPos(context.getWorld(), pos);
					tileEntity.isNatural = false;
					
					if (FMLEnvironment.dist.isClient()) {
						if (context.getWorld() instanceof ClientWorld) {
							if (Minecraft.getInstance().world == context.getWorld()) {
								((IRenderUnitsInBlocks) Minecraft.getInstance().worldRenderer).SmallerUnits_addUnitInBlock(tileEntity);
							}
						}
					}
					
					cap.getMap().put(pos, tileEntity);
					chunk.markDirty();
					context.getWorld().markBlockRangeForRenderUpdate(pos, Blocks.AIR.getDefaultState(), state);
					if (!context.getPlayer().isCreative()) stack.shrink(1);
				}
				
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.FAIL;
		}
	}
}
