package tfc.smallerunits.simulation.world.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.forge.SUModelDataManager;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.world.ITickerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class FakeClientWorld extends ClientLevel implements ITickerWorld {
	public final Region region;
	public final int upb;
	public final Map<BlockPos, BlockState> cache = new Object2ObjectLinkedOpenHashMap<>();
	public ParentLookup lookup;
	public ParentLookup lookupTemp;
	ClientLevel parent;
	
	// forge is stupid and does not account for there being more than 1 world at once
	public final SUModelDataManager modelDataManager = new SUModelDataManager();
	
	public FakeClientWorld(ClientLevel parent, ClientPacketListener p_205505_, ClientLevelData p_205506_, ResourceKey<Level> p_205507_, Holder<DimensionType> p_205508_, int p_205509_, int p_205510_, Supplier<ProfilerFiller> p_205511_, LevelRenderer p_205512_, boolean p_205513_, long p_205514_, int upb, Region region) {
		super(p_205505_, p_205506_, p_205507_, p_205508_, p_205509_, p_205510_, p_205511_, p_205512_, p_205513_, p_205514_);
		this.parent = parent;
		this.region = region;
		this.chunkSource = new TickerClientChunkCache(this, 0, upb);
		this.upb = upb;
		this.isClientSide = true;
		
		lookup = (pos) -> lookupTemp.getState(pos);
		lookupTemp = pos -> {
			BlockPos bp = region.pos.toBlockPos().offset(
					// TODO: double check this
					Math.floor(pos.getX() / (double) upb),
					Math.floor(pos.getY() / (double) upb),
					Math.floor(pos.getZ() / (double) upb)
			);
			if (cache.containsKey(bp))
				return cache.get(bp);
//			if (!parent.isLoaded(bp)) // TODO: check if there's a way to do this which doesn't cripple the server
//				return Blocks.VOID_AIR.defaultBlockState();
//			ChunkPos cp = new ChunkPos(bp);
//			if (parent.getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null)
//				return Blocks.VOID_AIR.defaultBlockState();
//			if (!getServer().isReady())
//				return Blocks.VOID_AIR.defaultBlockState();
			if (Minecraft.getInstance().level == null) return Blocks.VOID_AIR.defaultBlockState();
			BlockState state = parent.getBlockState(bp);
//			if (state.equals(Blocks.VOID_AIR.defaultBlockState()))
//				return state;
			cache.put(bp, state);
			return state;
		};
	}
	
	// TODO: stuff that requires a level renderer
	// I'll need a custom level renderer for this, I guess
	@Override
	public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
	}
	
	@Override
	public void addParticle(ParticleOptions pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
	}
	
	@Override
	public void addAlwaysVisibleParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
	}
	
	@Override
	public void addAlwaysVisibleParticle(ParticleOptions pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
	}
	
	public BlockHitResult collectShape(Vec3 start, Vec3 end, Function<BlockPos, Boolean> simpleChecker, BiFunction<BlockPos, BlockState, BlockHitResult> boxFiller, int upbInt) {
		BlockHitResult closest = null;
		double d = Double.POSITIVE_INFINITY;
		
		int minX = (int) Math.floor(Math.min(start.x, end.x)) - 1;
		int minY = (int) Math.floor(Math.min(start.y, end.y)) - 1;
		int minZ = (int) Math.floor(Math.min(start.z, end.z)) - 1;
		int maxX = (int) Math.ceil(Math.max(start.x, end.x)) + 1;
		int maxY = (int) Math.ceil(Math.max(start.y, end.y)) + 1;
		int maxZ = (int) Math.ceil(Math.max(start.z, end.z)) + 1;
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					BlockState state = getBlockState(new BlockPos(x, y, z));
					if (state.isAir()) continue;
					if (simpleChecker.apply(new BlockPos(x, y, z))) {
						BlockHitResult result = boxFiller.apply(new BlockPos(x, y, z), state);
						if (result != null) {
							double dd = result.getLocation().distanceTo(start);
							if (dd < d) {
								d = dd;
								closest = result;
							}
						}
					}
				}
			}
		}
		
		if (closest == null) return BlockHitResult.miss(end, Direction.UP, new BlockPos(end)); // TODO
		return closest;
	}
	
	@Override
	public BlockHitResult clip(ClipContext pContext) {
		// I prefer this method over vanilla's method
		Vec3 fStartVec = pContext.getFrom();
		Vec3 endVec = pContext.getTo();
		return collectShape(
				pContext.getFrom(),
				pContext.getTo(),
				(pos) -> {
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
					AABB box = new AABB(
//								x / upbDouble, y / upbDouble, z / upbDouble,
//								(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
							x, y, z,
							x + 1, y + 1, z + 1
					);
					return box.contains(fStartVec) || box.clip(fStartVec, endVec).isPresent();
				}, (pos, state) -> {
					int x = pos.getX();
					int y = pos.getY();
					int z = pos.getZ();
					VoxelShape sp = state.getShape(this, pos);
					return sp.clip(pContext.getFrom(), pContext.getTo(), pos);
//					for (AABB toAabb : sp.toAabbs()) {
//						toAabb = toAabb.move(x, y, z);
//						UnitBox b = new UnitBox(
//								toAabb.minX / upbDouble,
//								toAabb.minY / upbDouble,
//								toAabb.minZ / upbDouble,
//								toAabb.maxX / upbDouble,
//								toAabb.maxY / upbDouble,
//								toAabb.maxZ / upbDouble,
//								new BlockPos(x, y, z)
//						);
//					}
				},
				upb
		);
	}
	
	@Override
	public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState) {
		// TODO
	}
	
	@Override
	public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
		// TODO
	}
	
	@Override
	public int getUPB() {
		return upb;
	}
	
	@Override
	public void handleRemoval() {
		// I don't remember what this is
	}
	
	@Override
	public void SU$removeEntity(Entity pEntity) {
	
	}
	
	@Override
	public void SU$removeEntity(UUID uuid) {
	
	}
	
	@Override
	public Iterable<Entity> getAllEntities() {
		return new ArrayList<>(); // TODO
	}
	
	@Override
	public Level getParent() {
		return parent;
	}
	
	@Override
	public Region getRegion() {
		return region;
	}
	
	@Override
	public ParentLookup getLookup() {
		return lookup;
	}
	
	@Override
	public RegistryAccess registryAccess() {
		// TODO: find a proper solution
		if (parent == null) parent = Minecraft.getInstance().level;
		return parent.registryAccess();
	}
	
	@Override
	public Tag getTicksIn(BlockPos myPosInTheLevel, BlockPos offset) {
		return new CompoundTag();
	}
	
	@Override
	public void setLoaded() {
		// maybe TODO?
	}
	
	@Override
	public void loadTicks(CompoundTag ticks) {
		// nah, this don't exist client side
	}
	
	@Override
	public void clear(BlockPos myPosInTheLevel, BlockPos offset) {
		for (int x = myPosInTheLevel.getX(); x < offset.getX(); x++) {
			for (int y = myPosInTheLevel.getY(); y < offset.getY(); y++) {
				for (int z = myPosInTheLevel.getZ(); z < offset.getZ(); z++) {
					BlockPos pz = new BlockPos(x, y, z);
					BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(pz);
					vc.setBlockFast(new BlockPos(x, pz.getY(), z), null);
				}
			}
		}
	}
	
	@Override
	public void setFromSync(ChunkPos cp, int cy, int x, int y, int z, BlockState state, HashMap<ChunkPos, ChunkAccess> accessHashMap, ArrayList<BlockPos> positions) {
		BlockPos rp = region.pos.toBlockPos();
		int xo = ((cp.x * 16) / upb) + (x / upb);
		int yo = ((cy * 16) / upb) + (y / upb);
		int zo = ((cp.z * 16) / upb) + (z / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac;
		// vertical lookups shouldn't be too expensive
		if (!accessHashMap.containsKey(new ChunkPos(parentPos))) {
			ac = parent.getChunkAt(parentPos);
			accessHashMap.put(new ChunkPos(parentPos), ac);
			if (!positions.contains(parentPos)) {
				ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
				positions.add(parentPos);
			}
		} else ac = accessHashMap.get(new ChunkPos(parentPos));
		
		ISUCapability cap = SUCapabilityManager.getCapability((LevelChunk) ac);
		UnitSpace space = cap.getUnit(parentPos);
		if (space == null) {
			space = cap.getOrMakeUnit(parentPos);
			space.setUpb(upb);
		}
		BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(cp.getWorldPosition());
		vc = vc.getSubChunk(cy);
		vc.setBlockFast(new BlockPos(x, y, z), state);
		
		((SUCapableChunk) ac).SU$markDirty(parentPos);
	}
}
