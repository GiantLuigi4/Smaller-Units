package tfc.smallerunits.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;

public class UnitPallet {
	public final CompoundNBT nbt;
	public final HashMap<BlockState, Integer> stateIdMap = new HashMap<>();
	public final HashMap<BlockPos, Integer> posIdMap = new HashMap<>();
	public final HashMap<BlockPos, SmallUnit> posUnitMap = new HashMap<>();
	public int lastId = 0;
	
	public UnitPallet(Collection<SmallUnit> units) {
		nbt = new CompoundNBT();
		ListNBT listNbt = new ListNBT();
		CompoundNBT states = new CompoundNBT();
		
		for (SmallUnit unit : units) {
			posUnitMap.put(unit.pos, unit);
			
			if (!stateIdMap.containsKey(unit.state)) stateIdMap.put(unit.state, lastId++);
			
			posIdMap.put(unit.pos, stateIdMap.get(unit.state));
			CompoundNBT unitNBT = new CompoundNBT();
			unitNBT.putInt("x", unit.pos.getX());
			unitNBT.putInt("y", unit.pos.getY());
			unitNBT.putInt("z", unit.pos.getZ());
			unitNBT.putInt("state", stateIdMap.get(unit.state));
			
			if (unit.tileEntity != null) unitNBT.put("listNbt", unit.tileEntity.serializeNBT());
			
			listNbt.add(unitNBT);
			String state = unit.state.toString();
			state = state.substring("Block{".length());
			state = state.replace("}", "");
			states.putString(stateIdMap.get(unit.state).toString(), state);
		}
		
		nbt.put("units", listNbt);
		nbt.put("states", states);
	}
	
	public UnitPallet(CompoundNBT nbt) {
		this.nbt = nbt;
		ListNBT listNBT = nbt.getList("units", Constants.NBT.TAG_COMPOUND);
		CompoundNBT stateIndexMap = nbt.getCompound("states");
		
		for (INBT inbt : listNBT) {
			CompoundNBT nbt1 = (CompoundNBT) inbt;
			BlockPos pos = new BlockPos(nbt1.getInt("x"), nbt1.getInt("y"), nbt1.getInt("z"));
			int stateIndex = nbt1.getInt("state");
			String state = stateIndexMap.getString(stateIndex+"");
			int end = state.indexOf("[");
			
			if (end == -1) end = state.length()+1;
			String blockName = state;
			if (state.contains("[")) blockName = (state.substring(0,end));
			
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
			for (BlockState validState : block.getStateContainer().getValidStates()) {
				if (validState.toString().startsWith("Block{"+state.replace("[","}["))) {
					posUnitMap.put(pos, new SmallUnit(pos, validState));
					break;
				}
			}
		}
	}
}
