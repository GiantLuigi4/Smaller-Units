package tfc.smallerunits.Dimension;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.*;
import net.minecraftforge.client.IRenderHandler;
//import tfc.smallerunits.Registry.ModEventRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public class SUSimulatorWorld extends Dimension {
	public SUSimulatorWorld(net.minecraft.world.World worldIn, DimensionType typeIn) {
		super(worldIn, typeIn, 1);
	}
	
	@Nonnull
	@Override
	public ChunkGenerator<?> createChunkGenerator() {
		Set<Biome> biomes= ImmutableSet.of(
				Biomes.THE_VOID
		);
		return new ChunkGenerator<GenerationSettings>(this.getWorld(), new BiomeProvider(biomes) {
			@Override
			public Biome getNoiseBiome(int x, int y, int z) {
				return Biomes.THE_VOID;
			}
		},new GenerationSettings()) {
			@Override
			public void generateSurface(WorldGenRegion p_225551_1_, IChunk p_225551_2_) {
			
			}
			
			@Override
			public int getGroundHeight() {
				return 0;
			}
			
			@Override
			public void makeBase(IWorld worldIn, IChunk chunkIn) {
			
			}
			
			@Override
			public int func_222529_a(int p_222529_1_, int p_222529_2_, Heightmap.Type heightmapType) {
				return 0;
			}
		};
	}
	
	@Nullable
	@Override
	public BlockPos findSpawn(@Nonnull ChunkPos chunkPosIn, boolean checkValid) {
		return null;
	}
	
	@Override
	public void getLightmapColors(float partialTicks, float sunBrightness, float skyLight, float blockLight, Vector3f colors) {
//		colors=new Vector3f(sunBrightness,skyLight,blockLight);
	}
	
	@Override
	public float getLightBrightness(int p_227174_1_) {
		return super.getLightBrightness(p_227174_1_);
	}
	
	@Override
	public double getVoidFogYFactor() {
		return 20;
	}
	
	@Nonnull
	@Override
	public DimensionType getType() {
		return Objects.requireNonNull(DimensionType.byName(new ResourceLocation("smallerunits", "susimulator")));
	}
	
	@Nullable
	@Override
	public IRenderHandler getCloudRenderer() {
		return super.getCloudRenderer();
	}
	
	@Nullable
	@Override
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
		return null;
	}
	
	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		double d0 = MathHelper.frac((double)worldTime / 2400.0D - 0.25D);
		double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
		return (float)(d0 * 2.0D + d1) / 3.0F;
	}
	
	@Override
	public boolean isSurfaceWorld() {
		return false;
	}
	
	@Nonnull
	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks) {
		return new Vec3d(0,0,0);
	}
	
	@Override
	public boolean canRespawnHere() {
		return false;
	}
	
	@Override
	public boolean doesXZShowFog(int x, int z) {
		return true;
	}
	
	@Nullable
	@Override
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		return super.calcSunriseSunsetColors(celestialAngle, partialTicks);
	}
	
	@Override
	public boolean canDoLightning(Chunk chunk) {
		return super.canDoLightning(chunk);
	}
	
	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}
	
	@Override
	public boolean canMineBlock(PlayerEntity player, BlockPos pos) {
		return super.canMineBlock(player,pos);
	}
	
	@Override
	public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
		return super.shouldMapSpin(entity,x,z,rotation);
	}
}

