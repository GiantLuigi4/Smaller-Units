package tfc.smallerunits.block;

import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleHelper {
	public static Particle create(World worldObj, double x, double y, double z, BlockPos hitPos, UnitTileEntity tileEntity, BlockPos worldPos, float scl) {
		Particle particle = new DiggingParticle(
				(ClientWorld) worldObj,
				((hitPos.getX() + x) / tileEntity.unitsPerBlock) + worldPos.getX(),
				(((hitPos.getY() - 64) + y) / tileEntity.unitsPerBlock) + worldPos.getY(),
				((hitPos.getZ() + z) / tileEntity.unitsPerBlock) + worldPos.getZ(),
				0, 0, 0,
				(tileEntity.getFakeWorld() == null ? worldObj.getBlockState(worldPos) : tileEntity.getFakeWorld().getBlockState(hitPos))
		).setBlockPos(worldPos).multiplyVelocity(0.2F * scl).multiplyParticleScaleBy(0.6F * scl);
		return particle;
	}
}
