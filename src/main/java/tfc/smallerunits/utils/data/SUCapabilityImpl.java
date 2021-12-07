package tfc.smallerunits.utils.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.IRenderUnitsInBlocks;

import java.util.HashMap;

public class SUCapabilityImpl implements SUCapability {
	// TODO: make this not use UnitTileEntities
	HashMap<BlockPos, UnitTileEntity> unitMap = new HashMap<>();
	IWorld world;
	INBT inbt;
	
	public SUCapabilityImpl() {
	}
	
	@Override
	public UnitTileEntity getTile(World world, BlockPos pos) {
		if (unitMap.containsKey(pos)) return unitMap.get(pos);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof UnitTileEntity) return (UnitTileEntity) te;
		return null;
	}
	
	@Override
	public HashMap<BlockPos, UnitTileEntity> getMap() {
		return unitMap;
	}
	
	@Override
	public INBT serialize() {
		
		CompoundNBT nbt = new CompoundNBT();
		for (BlockPos pos : unitMap.keySet()) {
			UnitTileEntity tileEntity = unitMap.get(pos);
			CompoundNBT teNBT = tileEntity.serializeNBT();
			nbt.put(pos.getCoordinatesAsString(), teNBT);
		}
		return nbt;
	}
	
	public void tick(IWorld world) {
		if (!(world instanceof World)) return;
		
		if (FMLEnvironment.dist.isClient()) {
			if (world instanceof ClientWorld) {
				if (world == Minecraft.getInstance().world) {
					for (UnitTileEntity value : unitMap.values()) {
						((IRenderUnitsInBlocks) Minecraft.getInstance().worldRenderer).SmallerUnits_addUnitInBlock(value);
					}
				}
			}
		}
		
		if (inbt != null) {
			if (inbt instanceof CompoundNBT) {
				// TODO:
				CompoundNBT nbt = ((CompoundNBT) inbt);
				for (String s : nbt.keySet()) {
					UnitTileEntity te = new UnitTileEntity();
					String[] coords = s.split(", ");
					int x = Integer.parseInt(coords[0]);
					int y = Integer.parseInt(coords[1]);
					int z = Integer.parseInt(coords[2]);
					BlockPos pos = new BlockPos(x, y, z);
					te.setWorldAndPos((World) world, pos);
					CompoundNBT compound = nbt.getCompound(s);
					te.deserializeNBT(compound);
					unitMap.put(pos, te);
				}
				inbt = null;
			}
		}
		for (UnitTileEntity value : unitMap.values()) {
			value.tick();
			if (world instanceof ServerWorld) {
				value.getBlockState().getBlock().tick(
						value.getBlockState(), (ServerWorld) world, value.getPos(), ((ServerWorld) world).rand
				);
			}
		}
	}
	
	@Override
	public void deserialze(INBT inbt) {
		this.inbt = inbt;
	}
}
