package tfc.smallerunits.utils.data;

import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import tfc.smallerunits.block.UnitTileEntity;

import java.util.HashMap;

public interface SUCapability {
	UnitTileEntity getTile(World world, BlockPos pos);
	
	HashMap<BlockPos, UnitTileEntity> getMap();
	
	INBT serialize();
	
	void deserialze(INBT nbt);
	
	void tick(IWorld worldForge);
}
