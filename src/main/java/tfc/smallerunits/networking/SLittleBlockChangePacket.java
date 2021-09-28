package tfc.smallerunits.networking;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.SmallUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class SLittleBlockChangePacket implements IPacket {
	public ArrayList<SmallUnit> units;
	public HashMap<Long, CompoundNBT> nbtLookup = new HashMap<>();
	public BlockPos pos;
	public int scale;
	
	public SLittleBlockChangePacket(ArrayList<SmallUnit> units, BlockPos pos, int scale) {
		this.units = units;
		this.pos = pos;
		this.scale = scale;
	}
	
	public SLittleBlockChangePacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		units = new ArrayList<>();
		pos = buf.readBlockPos();
		scale = buf.readInt();
		
		ArrayList<BlockState> pallet = new ArrayList<>();
//		ArrayList<Pair<BlockPos, Integer>> tiles = new ArrayList<>();
		
		int amtPallet = buf.readInt();
		for (int i = 0; i < amtPallet; i++) {
			ResourceLocation regName = buf.readResourceLocation();
			String stateString = buf.readString();
			Block block = ForgeRegistries.BLOCKS.getValue(regName);
			if (block == null) block = Blocks.AIR;
			BlockState state = block.getDefaultState();
			for (BlockState validState : block.getStateContainer().getValidStates()) {
				if (validState.toString().equals(stateString)) {
					state = validState;
					break;
				}
			}
			pallet.add(state);
		}
		
		int amtTiles = buf.readInt();
		for (int i = 0; i < amtTiles; i++) {
			BlockPos pos = buf.readBlockPos();
			int indx = buf.readInt();
			units.add(new SmallUnit(new UnitPos(new UnitPos(pos, this.pos, scale), pos, scale), pallet.get(indx)));
		}

//		for (int unitIndex = 0; unitIndex < buf.readInt(); unitIndex++) {
//			BlockPos unitPos = buf.readBlockPos();
//			Block block = ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation());
//			if (block == null) block = Blocks.AIR;
//			BlockState state = block.getDefaultState();
//			{
//				String stateName = buf.readString();
//				for (BlockState validState : block.getStateContainer().getValidStates()) {
//					if (validState.toString().equals(stateName)) {
//						state = validState;
//					}
//				}
//			}
////			int properties = buf.readInt();
////			for (int propertyIndex = 0; propertyIndex < properties; propertyIndex++) {
////				String name = buf.readString();
////				String value = buf.readString();
////				for (Property<?> property : state.getProperties()) {
////					if (property.getName().equals(name)) {
////						Optional<?> val = property.parseValue(value);
////						if (val.isPresent()) state = stateWith(state, property, value);
////					}
////				}
////			}
//			units.add(new SmallUnit(new UnitPos(unitPos, pos, scale), state));
//		}
	}
	
	public <T extends Comparable<T>> BlockState stateWith(BlockState state, Property<T> property, Object value) {
		return state.with(property, (T) value);
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBlockPos(pos);
		buf.writeInt(scale);
		
		ArrayList<Pair<ResourceLocation, String>> pallet = new ArrayList<>();
		ArrayList<Pair<BlockPos, Integer>> tiles = new ArrayList<>();
		
		for (SmallUnit unit : units) {
			ResourceLocation regName = unit.state.getBlock().getRegistryName();
			String stateString = unit.state.toString();
			boolean anyMatch = false;
			int indxMatch = 0;
			for (Pair<ResourceLocation, String> value : pallet) {
				if (value.getFirst().equals(regName) && value.getSecond().equals(stateString)) {
					anyMatch = true;
					break;
				}
				indxMatch++;
			}
			if (!anyMatch) pallet.add(Pair.of(regName, stateString));
			tiles.add(Pair.of(unit.pos, indxMatch));
		}
		
		buf.writeInt(pallet.size());
		for (int i = 0; i < pallet.size(); i++) {
			Pair<ResourceLocation, String> entry = pallet.get(i);
			buf.writeResourceLocation(entry.getFirst());
			buf.writeString(entry.getSecond());
		}
		
		buf.writeInt(tiles.size());
		for (Pair<BlockPos, Integer> tile : tiles) {
			buf.writeBlockPos(tile.getFirst());
			buf.writeInt(tile.getSecond());
		}

//		buf.writeInt(units.size());
//		for (SmallUnit unit : units) {
//			buf.writeBlockPos(unit.pos);
//			buf.writeResourceLocation(unit.state.getBlock().getRegistryName());
//
//			{
//				buf.writeString(unit.state.toString());
//			}
////			{
////				Collection<Property<?>> properties = unit.state.getProperties();
////				buf.writeInt(properties.size());
////				for (Property<?> property : properties) {
////					buf.writeString(property.getName());
////					buf.writeString(unit.state.get(property).toString());
////				}
////			}
//		}
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide().isClient()) {
			BlockState state = Minecraft.getInstance().world.getBlockState(pos);
			if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
			TileEntity te = Minecraft.getInstance().world.getTileEntity(pos);
			if (!(te instanceof UnitTileEntity)) return;
			UnitTileEntity tileEntity = (UnitTileEntity) te;
			
			Long2ObjectOpenHashMap<SmallUnit> unitsUpdated = new Long2ObjectOpenHashMap<>();
			for (SmallUnit unit : units) unitsUpdated.put(unit.pos.toLong(), unit);
			
			ArrayList<Long> toRemove = new ArrayList<>();
			for (SmallUnit value : tileEntity.getBlockMap().values()) {
				if (unitsUpdated.containsKey(value.pos.toLong())) {
					value.state = unitsUpdated.get(value.pos.toLong()).state;
					toRemove.add(value.pos.toLong());
					if (value.tileEntity != null) {
						if (!value.tileEntity.getType().isValidBlock(value.state.getBlock())) {
//							tileEntity.getFakeWorld().removeTileEntity(value.pos);
							value.oldTE = value.tileEntity;
							value.tileEntity = null;
						}
					} else {
						if (value.state.hasTileEntity()) {
							value.tileEntity = value.state.createTileEntity(tileEntity.getFakeWorld());
							if (value.tileEntity != null)
								value.tileEntity.setWorldAndPos(tileEntity.getFakeWorld(), value.pos);
						}
					}
				}
			}
			
			for (Long aLong : toRemove) unitsUpdated.remove(aLong);
			for (SmallUnit value : unitsUpdated.values()) {
				tileEntity.getBlockMap().put(value.pos.toLong(), value);
				if (value.state.hasTileEntity()) {
					value.tileEntity = value.state.createTileEntity(tileEntity.getFakeWorld());
					if (value.tileEntity != null) value.tileEntity.setWorldAndPos(tileEntity.getFakeWorld(), value.pos);
				}
				if (value.tileEntity != null) {
					if (!value.tileEntity.getType().isValidBlock(value.state.getBlock())) {
//						tileEntity.getFakeWorld().removeTileEntity(value.pos);
						value.oldTE = value.tileEntity;
						value.tileEntity = null;
					}
				}
			}
			
			// TODO: make this mark corners dirty
			tileEntity.markDirty();
			for (Direction dir : Direction.values()) {
				if (tileEntity.getWorld().isBlockLoaded(pos.offset(dir))) {
					TileEntity te1 = tileEntity.getWorld().getTileEntity(pos.offset(dir));
					if (te1 instanceof UnitTileEntity) {
						// TODO: "optimize" this (only mark dirty if a block changed at the edge of the unit)
						te1.markDirty();
					}
				}
			}
		}
	}
}
