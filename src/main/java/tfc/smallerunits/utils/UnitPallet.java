package tfc.smallerunits.utils;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.api.placement.UnitPos;

import java.util.Collection;
import java.util.HashMap;

public class UnitPallet {
	public final CompoundNBT nbt;
	public final HashMap<BlockState, Integer> stateIdMap = new HashMap<>();
	public final HashMap<BlockPos, Integer> posIdMap = new HashMap<>();
	public final Long2ObjectLinkedOpenHashMap<SmallUnit> posUnitMap = new Long2ObjectLinkedOpenHashMap<>();
	public int lastId = 0;
	
	public UnitPallet(Collection<SmallUnit> units) {
		nbt = new CompoundNBT();
		ListNBT listNbt = new ListNBT();
		CompoundNBT states = new CompoundNBT();
		CompoundNBT blocks = new CompoundNBT();
		
		for (SmallUnit unit : units) {
			posUnitMap.put(unit.pos.toLong(), unit);
			
			if (!stateIdMap.containsKey(unit.state)) stateIdMap.put(unit.state, lastId++);
			
			posIdMap.put(unit.pos, stateIdMap.get(unit.state));
			CompoundNBT unitNBT = new CompoundNBT();
			if (unit == null || unit.pos == null) continue;
			unitNBT.putInt("x", unit.pos.getX());
			unitNBT.putInt("y", unit.pos.getY() - 64);
			unitNBT.putInt("z", unit.pos.getZ());
			unitNBT.putInt("state", stateIdMap.get(unit.state));
			
			if (unit.tileEntity != null)
				unitNBT.put("tileNBT", unit.tileEntity.serializeNBT());
			
			listNbt.add(unitNBT);
			String state = unit.state.getBlockState().toString();
			if (!state.startsWith("Block{")) {
				blocks.putString(stateIdMap.get(unit.state).toString(), unit.state.getBlock().getRegistryName().toString());
			} else {
				state = state.substring("Block{".length());
				state = state.replace("}", "");
			}
			states.putString(stateIdMap.get(unit.state).toString(), state);
		}
		
		nbt.put("units", listNbt);
		nbt.put("states", states);
		nbt.put("blocks", blocks);
	}
	
	public UnitPallet(CompoundNBT nbt, World world, BlockPos realPos, int scale) {
		this.nbt = nbt;
		ListNBT listNBT = nbt.getList("units", Constants.NBT.TAG_COMPOUND);
		CompoundNBT stateIndexMap = nbt.getCompound("states");
		CompoundNBT blockIndexMap = nbt.getCompound("blocks");
		
		for (INBT inbt : listNBT) {
			CompoundNBT nbt1 = (CompoundNBT) inbt;
			BlockPos pos = new UnitPos(nbt1.getInt("x"), nbt1.getInt("y") + 64, nbt1.getInt("z"), realPos, scale);
			
			int stateIndex = nbt1.getInt("state");
			String state = stateIndexMap.getString(stateIndex + "");
			int end = state.indexOf("[");
			
			if (end == -1) end = state.length() + 1;
			String blockName = state;
			if (state.contains("[")) blockName = (state.substring(0, end));
			if (blockIndexMap.contains(stateIndex + "")) {
				blockName = blockIndexMap.getString(stateIndex + "");
			}
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
			SmallUnit unit = null;
			for (BlockState validState : block.getStateContainer().getValidStates()) {
				if (validState.toString().startsWith("Block{" + state.replace("[", "}["))) {
					unit = new SmallUnit(new UnitPos(pos, realPos, scale), validState);
					posUnitMap.put(pos.toLong(), unit);
					break;
				} else if (validState.toString().equals(state)) {
					unit = new SmallUnit(new UnitPos(pos, realPos, scale), validState);
					posUnitMap.put(pos.toLong(), unit);
					break;
				}
			}
			if (unit == null && block != Blocks.AIR) {
				unit = new SmallUnit(new UnitPos(pos, realPos, scale), block.getDefaultState());
				posUnitMap.put(pos.toLong(), unit);
			}
			
			if (unit == null) continue;
			
			if (nbt1.contains("tileNBT")) {
				CompoundNBT teNBT = nbt1.getCompound("tileNBT");
				TileEntityType<?> type = ForgeRegistries.TILE_ENTITIES.getValue(new ResourceLocation(teNBT.getString("id")));
				if (type == null) continue;
				TileEntity te = type.create();
				if (te == null) continue;
				if (world != null && world.isRemote) {
					// TODO: do this properly
					te.setWorldAndPos(world, unit.pos);
					te.handleUpdateTag(unit.state, teNBT);
				}
				te.read(unit.state, teNBT);
				unit.tileEntity = te;
				if (world != null) unit.tileEntity.setWorldAndPos(world, pos);
			}
		}
	}
}
