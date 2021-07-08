package com.tfc.smallerunits.utils.world.client;

import com.tfc.smallerunits.block.UnitTileEntity;
import com.tfc.smallerunits.registry.Deferred;
import com.tfc.smallerunits.utils.ExternalUnitInteractionContext;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.lighting.IWorldLightListener;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;

public class FakeClientLightingManager extends WorldLightManager {
	public boolean hasChanged = false;
	FakeClientWorld world = null;
	int recursionDepth = 0;
	private WorldLightManager lightManager;
	private int lastSize = 0;
	private byte[] lighting = new byte[0];
	private int lastX = 0;
	private int lastY = 0;
	private int lastZ = 0;
	
	public FakeClientLightingManager(IChunkLightProvider provider, boolean hasBlockLight, boolean hasSkyLight, FakeClientWorld world) {
		super(provider, hasBlockLight, hasSkyLight);
		this.world = world;
		this.lightManager = new WorldLightManager(provider, true, hasSkyLight);
	}
	
	@Override
	public void checkBlock(BlockPos blockPosIn) {
		lightManager.checkBlock(blockPosIn);
	}
	
	@Override
	public int tick(int toUpdateCount, boolean updateSkyLight, boolean updateBlockLight) {
		if (lastSize != world.owner.unitsPerBlock) {
			lighting = new byte[world.owner.unitsPerBlock * world.owner.unitsPerBlock * world.owner.unitsPerBlock];
			lastSize = world.owner.unitsPerBlock;
		}
		int i;
		for (i = 0; i < toUpdateCount; i++) {
			lastX++;
			
			if (lastX > world.owner.unitsPerBlock - 1) {
				lastZ++;
				lastX = 0;
				
				if (lastZ > world.owner.unitsPerBlock - 1) {
					lastY++;
					lastZ = 0;
					
					if (lastY > world.owner.unitsPerBlock - 1) {
						lastY = 0;
						break;
					}
				}
			}
			
			BlockPos pos = new BlockPos(lastX, lastY, lastZ);
			if (testLight(pos, world))
				break;
			pos = new BlockPos((world.owner.unitsPerBlock - 1) - lastX, (world.owner.unitsPerBlock - 1) - lastY, (world.owner.unitsPerBlock - 1) - lastZ);
			if (testLight(pos, world))
				break;
		}
		return i;
//		return lightManager.tick(toUpdateCount, updateSkyLight, updateBlockLight);
	}
	
	private boolean testLight(BlockPos pos, FakeClientWorld world) {
		if (isInbounds(pos, world.owner.unitsPerBlock)) {
			BlockState state = world.getBlockState(pos.add(0, 64, 0));
			int stateLight = state.getLightValue(world, pos.add(0, 64, 0));
			int max = stateLight;
			for (Direction dir : Direction.values()) {
				if (isInbounds(pos.offset(dir), world.owner.unitsPerBlock)) {
					int amt = lighting[toIndex(pos.offset(dir))];
					max = Math.max(max, amt - 1);
				} else {
					ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(world, pos.offset(dir).add(0, 64, 0));
					if (context.stateInRealWorld != null) {
						if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
							if (!context.posInRealWorld.equals(world.owner.getPos())) {
								if (context.teInRealWorld != null) {
									if (!context.teInRealWorld.equals(world.owner)) {
										if ((((FakeClientWorld) ((UnitTileEntity) context.teInRealWorld).getFakeWorld())) != null) {
											if (((FakeClientLightingManager) ((FakeClientWorld) ((UnitTileEntity) context.teInRealWorld).getFakeWorld()).lightManager) != null) {
												if (((FakeClientLightingManager) ((FakeClientWorld) ((UnitTileEntity) context.teInRealWorld).getFakeWorld()).lightManager).isInbounds(context.posInFakeWorld.down(64), ((UnitTileEntity) context.teInRealWorld).unitsPerBlock)) {
													int amt = ((FakeClientLightingManager) ((FakeClientWorld) ((UnitTileEntity) context.teInRealWorld).getFakeWorld()).lightManager).lighting[toIndex(context.posInFakeWorld.down(64))];
													max = Math.max(max, amt - 1);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if (stateLight == 0) {
				max -= state.getOpacity(world, pos.add(0, 64, 0));
			}
			max = Math.max(0, max);
			if (lighting[toIndex(pos)] != (max)) {
				lighting[toIndex(pos)] = (byte) max;
				hasChanged = true;
				world.markSurroundingsForRerender(0, 0, 0);
			}
			return false;
		} else {
			lastX = 0;
			lastY = 0;
			lastZ = 0;
			return true;
		}
	}
	
	public boolean isInbounds(BlockPos pos, int upb) {
		return
				!(
						pos.getX() < 0 ||
								pos.getX() > upb - 1 ||
								pos.getZ() < 0 ||
								pos.getZ() > upb - 1 ||
								pos.getY() < 0 ||
								pos.getY() > upb - 1
				) && toIndex(pos) >= 0 && toIndex(pos) < lighting.length;
	}
	
	public int getBlockLight(BlockPos pos) {
		if (isInbounds(pos, ((FakeClientWorld) world).owner.unitsPerBlock)) {
			if (((FakeClientWorld) world).owner.getWorld().isRemote)
				testLight(pos, (FakeClientWorld) world);
			return Math.max(lighting[toIndex(pos)], 0);
		}
		for (Direction value : Direction.values()) {
			if (isInbounds(pos.offset(value), ((FakeClientWorld) world).owner.unitsPerBlock)) {
				return Math.max(lighting[toIndex(pos.offset(value))] - 1, 0);
			}
		}
		return 0;
	}
	
	public int toIndex(BlockPos pos) {
		return
				pos.getX() +
						(pos.getY() * ((FakeClientWorld) world).owner.unitsPerBlock) +
						(pos.getZ() * ((FakeClientWorld) world).owner.unitsPerBlock * ((FakeClientWorld) world).owner.unitsPerBlock);
	}
	
	@Override
	public boolean hasLightWork() {
		return lightManager.hasLightWork();
	}
	
	@Override
	public IWorldLightListener getLightEngine(LightType type) {
		return lightManager.getLightEngine(type);
	}
	
	@Override
	public String getDebugInfo(LightType p_215572_1_, SectionPos p_215572_2_) {
		return lightManager.getDebugInfo(p_215572_1_, p_215572_2_);
	}
	
	@Override
	public int getLightSubtracted(BlockPos blockPosIn, int amount) {
		return lightManager.getLightSubtracted(blockPosIn, amount);
	}
	
	@Override
	public void onBlockEmissionIncrease(BlockPos blockPosIn, int p_215573_2_) {
		lightManager.onBlockEmissionIncrease(blockPosIn, p_215573_2_);
	}
	
	@Override
	public void updateSectionStatus(SectionPos pos, boolean isEmpty) {
		lightManager.updateSectionStatus(pos, isEmpty);
	}
	
	@Override
	public void enableLightSources(ChunkPos p_215571_1_, boolean p_215571_2_) {
		lightManager.enableLightSources(p_215571_1_, p_215571_2_);
	}
	
	@Override
	public void setData(LightType type, SectionPos pos, @Nullable NibbleArray array, boolean p_215574_4_) {
		lightManager.setData(type, pos, array, p_215574_4_);
	}
	
	@Override
	public void retainData(ChunkPos pos, boolean retain) {
		lightManager.retainData(pos, retain);
	}
	
	@Override
	public void func_215567_a(BlockPos p_215567_1_, boolean p_215567_2_) {
		lightManager.func_215567_a(p_215567_1_, p_215567_2_);
	}
	
	@Override
	protected void finalize() {
		if (lightManager.blockLight != null) {
			lightManager.blockLight.storage = null;
			lightManager.blockLight.type = null;
			lightManager.blockLight.chunkProvider = null;
			lightManager.blockLight.field_215629_e = true;
			lightManager.blockLight.recentPositions = null;
			lightManager.blockLight.recentChunks = null;
			lightManager.blockLight.scratchPos = null;
		}
		if (lightManager.skyLight != null) {
			lightManager.skyLight.storage = null;
			lightManager.skyLight.type = null;
			lightManager.skyLight.chunkProvider = null;
			lightManager.skyLight.field_215629_e = true;
			lightManager.skyLight.recentPositions = null;
			lightManager.skyLight.recentChunks = null;
			lightManager.skyLight.scratchPos = null;
		}
		if (this.blockLight != null) {
			this.blockLight.storage = null;
			this.blockLight.type = null;
			this.blockLight.chunkProvider = null;
			this.blockLight.field_215629_e = true;
			this.blockLight.recentPositions = null;
			this.blockLight.recentChunks = null;
			this.blockLight.scratchPos = null;
		}
		if (this.skyLight != null) {
			this.skyLight.storage = null;
			this.skyLight.type = null;
			this.skyLight.chunkProvider = null;
			this.skyLight.field_215629_e = true;
			this.skyLight.recentPositions = null;
			this.skyLight.recentChunks = null;
			this.skyLight.scratchPos = null;
		}
	}
}
