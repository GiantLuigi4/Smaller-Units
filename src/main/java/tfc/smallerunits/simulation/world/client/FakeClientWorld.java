package tfc.smallerunits.simulation.world.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import org.jetbrains.annotations.Nullable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.forge.SUModelDataManager;
import tfc.smallerunits.client.render.compat.UnitParticleEngine;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.block.ParentLookup;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.utils.BreakData;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.scale.ResizingUtils;
import tfc.smallerunits.utils.storage.VecMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class FakeClientWorld extends ClientLevel implements ITickerWorld {
	public final Region region;
	public final int upb;
	public final VecMap<Pair<BlockState, VecMap<VoxelShape>>> cache = new VecMap<>(2);
	public ParentLookup lookup;
	public ParentLookup lookupTemp;
	ClientLevel parent;
	
	private final ArrayList<Runnable> completeOnTick = new ArrayList<>();
	// if I do Minecraft.getInstance().getTextureManager(), it messes up particle textures
	UnitParticleEngine particleEngine = new UnitParticleEngine(this, new TextureManager(Minecraft.getInstance().getResourceManager()));
	
	@Override
	public void playSound(@Nullable Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch) {
		super.playSound(pPlayer, pX, pY, pZ, pSound, pCategory, pVolume, pPitch);
	}
	
	@Override
	public void playSound(@Nullable Player pPlayer, Entity pEntity, SoundEvent pEvent, SoundSource pCategory, float pVolume, float pPitch) {
//		super.playSound(pPlayer, pEntity, pEvent, pCategory, pVolume, pPitch);
		double scl = 1f / upb;
		if (ResizingUtils.isResizingModPresent())
			scl *= 1 / ResizingUtils.getSize(Minecraft.getInstance().cameraEntity);
		if (scl > 1) scl = 1 / scl;
		double finalScl = scl;
		completeOnTick.add(() -> {
			parent.playSound(pPlayer, pEntity, pEvent, pCategory, (float) (pVolume * finalScl), pPitch);
		});
	}
	
	@Override
	public void playLocalSound(BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
		super.playLocalSound(pPos, pSound, pCategory, pVolume, pPitch, pDistanceDelay);
	}
	
	@Override
	public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
//		super.playLocalSound(pX, pY, pZ, pSound, pCategory, pVolume, pPitch, pDistanceDelay);
		double scl = 1f / upb;
		BlockPos pos = getRegion().pos.toBlockPos();
		pX *= scl;
		pY *= scl;
		pZ *= scl;
		pX += pos.getX();
		pY += pos.getX();
		pZ += pos.getX();
		double finalPX = pX;
		double finalPY = pY;
		double finalPZ = pZ;
		if (ResizingUtils.isResizingModPresent())
			scl *= 1 / ResizingUtils.getSize(Minecraft.getInstance().cameraEntity);
		if (scl > 1) scl = 1 / scl;
		double finalScl = scl;
		completeOnTick.add(() -> {
			parent.playLocalSound(finalPX, finalPY, finalPZ, pSound, pCategory, (float) (pVolume * finalScl), pPitch, pDistanceDelay);
		});
	}
	
	public FakeClientWorld(ClientLevel parent, ClientPacketListener p_205505_, ClientLevelData p_205506_, ResourceKey<Level> p_205507_, Holder<DimensionType> p_205508_, int p_205509_, int p_205510_, Supplier<ProfilerFiller> p_205511_, LevelRenderer p_205512_, boolean p_205513_, long p_205514_, int upb, Region region) {
		super(p_205505_, p_205506_, p_205507_, p_205508_, p_205509_, p_205510_, p_205511_, p_205512_, p_205513_, p_205514_);
		this.parent = parent;
		this.region = region;
		this.chunkSource = new TickerClientChunkCache(this, 0, upb);
		this.upb = upb;
		this.isClientSide = true;
		
		particleEngine.setLevel(this);
		
		lookup = (pos) -> lookupTemp.getState(pos);
		lookupTemp = pos -> {
			BlockPos bp = region.pos.toBlockPos().offset(
					// TODO: double check this
					Math.floor(pos.getX() / (double) upb),
					Math.floor(pos.getY() / (double) upb),
					Math.floor(pos.getZ() / (double) upb)
			);
			if (cache.containsKey(bp)) {
//				BlockState state = cache.get(bp).getFirst();
//				VoxelShape shape = state.getCollisionShape(parent, bp);
				// TODO: empty shape check
				return cache.get(bp).getFirst();
			}
//			if (!parent.isLoaded(bp)) // TODO: check if there's a way to do this which doesn't cripple the server
//				return Blocks.VOID_AIR.defaultBlockState();
//			ChunkPos cp = new ChunkPos(bp);
//			if (parent.getChunk(cp.x, cp.z, ChunkStatus.FULL, false) == null)
//				return Blocks.VOID_AIR.defaultBlockState();
			if (Minecraft.getInstance().level == null) return Blocks.VOID_AIR.defaultBlockState();
			BlockState state = parent.getBlockState(bp);
//			if (state.equals(Blocks.VOID_AIR.defaultBlockState()))
//				return state;
			cache.put(bp, Pair.of(state, new VecMap<>(2)));
			return state;
		};
		
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(this));
	}
	
	// forge is stupid and does not account for there being more than 1 world at once
	public final SUModelDataManager modelDataManager = new SUModelDataManager();
	
	public UnitParticleEngine getParticleEngine() {
		return particleEngine;
	}
	
	public HashMap<Integer, BreakData> breakStatus = new HashMap<>();
	
	@Override
	public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {
//		Entity ent = getEntity(pBreakerId);
//		if (!(ent instanceof Player)) {
//			ent = parent.getEntity(pBreakerId);
//			if (!(ent instanceof Player)) {
//				return;
//			}
//		}
		if (pProgress < 0) breakStatus.remove(pBreakerId);
		else {
			if (breakStatus.containsKey(pBreakerId))
				breakStatus.replace(pBreakerId, new BreakData(pPos, pProgress));
			else
				breakStatus.put(pBreakerId, new BreakData(pPos, pProgress));
		}
	}
	
	@Override
	public HashMap<Integer, BreakData> getBreakData() {
		return breakStatus;
	}
	
	@Override
	protected void finalize() throws Throwable {
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(this));
		super.finalize();
	}
	
	@Override
	public void sendPacketToServer(Packet<?> pPacket) {
		NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(region.pos, upb));
		parent.sendPacketToServer(pPacket);
		NetworkingHacks.unitPos.remove();
	}
	
	@Override
	public void disconnect() {
		NetworkingHacks.unitPos.set(new NetworkingHacks.LevelDescriptor(region.pos, upb));
		parent.disconnect();
		NetworkingHacks.unitPos.remove();
	}
	
	// TODO: do this a bit more properly
	@Override
	public void addParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		addAlwaysVisibleParticle(pParticleData, false, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}
	
	@Override
	public void addParticle(ParticleOptions pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		addAlwaysVisibleParticle(pParticleData, pForceAlwaysRender, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}
	
	@Override
	public void addAlwaysVisibleParticle(ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		addAlwaysVisibleParticle(pParticleData, false, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}
	
	@Override
	public void addAlwaysVisibleParticle(ParticleOptions pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		Particle particle = particleEngine.createParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
		if (particle != null)
			particleEngine.add(particle);
	}
	
	// TODO: stuff that requires a level renderer
	@Override
	public void globalLevelEvent(int pId, BlockPos pPos, int pData) {
		// TODO
	}
	
	@Override
	public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData) {
//		System.out.println(pType);
		ClientLevel level = Minecraft.getInstance().level;
		ParticleEngine engine = Minecraft.getInstance().particleEngine;
		Minecraft.getInstance().level = this;
		Minecraft.getInstance().particleEngine = particleEngine;
		// TODO level renderer so I don't need this switch
		switch (pType) {
			case 1000 -> playLocalSound(pPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			case 1001 -> playLocalSound(pPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
			case 1002 -> playLocalSound(pPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
			case 1003 -> playLocalSound(pPos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
			case 1004 -> playLocalSound(pPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
			case 1005 -> playLocalSound(pPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1006 -> playLocalSound(pPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1007 -> playLocalSound(pPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1008 -> playLocalSound(pPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1029 -> playLocalSound(pPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1030 -> playLocalSound(pPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1031 -> playLocalSound(pPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1011 -> playLocalSound(pPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1012 -> playLocalSound(pPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1013 -> playLocalSound(pPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1014 -> playLocalSound(pPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1033 -> playLocalSound(pPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			case 1046 -> playLocalSound(pPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.random.nextFloat() * 0.1F + 0.9F, false);
			case 1034 -> playLocalSound(pPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			case 1035 -> playLocalSound(pPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			case 1036 -> playLocalSound(pPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1037 -> playLocalSound(pPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
			case 1039 -> playLocalSound(pPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, random.nextFloat() * 0.1F + 0.9F, false);
			default -> {
				switch (pType) {
					case 1505:
						BoneMealItem.addGrowthParticles(this, pPos, pData);
						playLocalSound(pPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
						break;
					case 1009:
						if (pData == 0)
							playLocalSound(pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
						else if (pData == 1)
							playLocalSound(pPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F, false);
						break;
					case 1504:
						PointedDripstoneBlock.spawnDripParticle(this, pPos, getBlockState(pPos));
						break;
					case 1501:
						playLocalSound(pPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
						
						for (int i2 = 0; i2 < 8; ++i2) {
							addParticle(ParticleTypes.LARGE_SMOKE, (double) pPos.getX() + random.nextDouble(), (double) pPos.getY() + 1.2D, (double) pPos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
						}
						break;
					case 1502:
						playLocalSound(pPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
						
						for (int l1 = 0; l1 < 5; ++l1) {
							double d15 = (double) pPos.getX() + random.nextDouble() * 0.6D + 0.2D;
							double d20 = (double) pPos.getY() + random.nextDouble() * 0.6D + 0.2D;
							double d26 = (double) pPos.getZ() + random.nextDouble() * 0.6D + 0.2D;
							addParticle(ParticleTypes.SMOKE, d15, d20, d26, 0.0D, 0.0D, 0.0D);
						}
						break;
					case 1503:
						playLocalSound(pPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
						
						for (int k1 = 0; k1 < 16; ++k1) {
							double d14 = (double) pPos.getX() + (5.0D + random.nextDouble() * 6.0D) / 16.0D;
							double d19 = (double) pPos.getY() + 0.8125D;
							double d25 = (double) pPos.getZ() + (5.0D + random.nextDouble() * 6.0D) / 16.0D;
							addParticle(ParticleTypes.SMOKE, d14, d19, d25, 0.0D, 0.0D, 0.0D);
						}
						break;
					case 2000:
						Direction direction = Direction.from3DDataValue(pData);
						int j1 = direction.getStepX();
						int j2 = direction.getStepY();
						int k2 = direction.getStepZ();
						double d18 = (double) pPos.getX() + (double) j1 * 0.6D + 0.5D;
						double d24 = (double) pPos.getY() + (double) j2 * 0.6D + 0.5D;
						double d28 = (double) pPos.getZ() + (double) k2 * 0.6D + 0.5D;
						
						for (int i3 = 0; i3 < 10; ++i3) {
							double d4 = random.nextDouble() * 0.2D + 0.01D;
							double d6 = d18 + (double) j1 * 0.01D + (random.nextDouble() - 0.5D) * (double) k2 * 0.5D;
							double d8 = d24 + (double) j2 * 0.01D + (random.nextDouble() - 0.5D) * (double) j2 * 0.5D;
							double d30 = d28 + (double) k2 * 0.01D + (random.nextDouble() - 0.5D) * (double) j1 * 0.5D;
							double d9 = (double) j1 * d4 + random.nextGaussian() * 0.01D;
							double d10 = (double) j2 * d4 + random.nextGaussian() * 0.01D;
							double d11 = (double) k2 * d4 + random.nextGaussian() * 0.01D;
							this.addParticle(ParticleTypes.SMOKE, d6, d8, d30, d9, d10, d11);
						}
						break;
					case 2001:
						BlockState blockstate = Block.stateById(pData);
						if (!blockstate.isAir()) {
							SoundType soundtype = blockstate.getSoundType(this, pPos, null);
							this.playLocalSound(pPos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, false);
						}
						
						this.addDestroyBlockEffect(pPos, blockstate);
						break;
					default:
						System.out.println("Unkown Level Event: " + pType);
				}
			}
		}
		Minecraft.getInstance().level = level;
		Minecraft.getInstance().particleEngine = engine;
	}
	
	@Override
	public void createFireworks(double pX, double pY, double pZ, double pMotionX, double pMotionY, double pMotionZ, @Nullable CompoundTag pCompound) {
		ParticleEngine engine = Minecraft.getInstance().particleEngine;
		Minecraft.getInstance().particleEngine = particleEngine;
		super.createFireworks(pX, pY, pZ, pMotionX, pMotionY, pMotionZ, pCompound);
		Minecraft.getInstance().particleEngine = engine;
	}
	
	public BlockHitResult collectShape(Vec3 start, Vec3 end, Function<AABB, Boolean> simpleChecker, BiFunction<BlockPos, BlockState, BlockHitResult> boxFiller, int upbInt) {
		BlockHitResult closest = null;
		double d = Double.POSITIVE_INFINITY;
		
		int minX = (int) Math.floor(Math.min(start.x, end.x)) - 1;
		int minY = (int) Math.floor(Math.min(start.y, end.y)) - 1;
		int minZ = (int) Math.floor(Math.min(start.z, end.z)) - 1;
		int maxX = (int) Math.ceil(Math.max(start.x, end.x)) + 1;
		int maxY = (int) Math.ceil(Math.max(start.y, end.y)) + 1;
		int maxZ = (int) Math.ceil(Math.max(start.z, end.z)) + 1;
		
		// TODO: there are better ways to do this
		for (int x = minX; x < maxX; x += 16) {
			for (int y = minY; y < maxY; y += 16) {
				for (int z = minZ; z < maxZ; z += 16) {
					AABB box = new AABB(
							x, y, z,
							x + 16, y + 16, z + 16
					);
					if (simpleChecker.apply(box)) {
						for (int x0 = 0; x0 < 16; x0++) {
							for (int y0 = 0; y0 < 16; y0++) {
								for (int z0 = 0; z0 < 16; z0++) {
									int x1 = x + x0;
									int y1 = y + y0;
									int z1 = z + z0;
									box = new AABB(
											x1, y1, z1,
											x1 + 1, y1 + 1, z1 + 1
									);
									if (simpleChecker.apply(box)) {
										BlockPos pos = new BlockPos(x1, y1, z1);
										BlockState state = getBlockState(pos);
										if (state.isAir()) continue;
										BlockHitResult result = boxFiller.apply(pos, state);
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
				(box) -> {
					return box.contains(fStartVec) || box.clip(fStartVec, endVec).isPresent();
				}, (pos, state) -> {
//					int x = pos.getX();
//					int y = pos.getY();
//					int z = pos.getZ();
					VoxelShape sp = state.getShape(this, pos);
					BlockHitResult result = sp.clip(pContext.getFrom(), pContext.getTo(), pos);
					if (result == null) return result;
					if (!result.getType().equals(HitResult.Type.MISS)) {
						Vec3 off = pContext.getFrom().subtract(pContext.getTo());
						off = off.normalize().scale(0.1);
						Vec3 st = result.getLocation().add(off);
						Vec3 ed = result.getLocation().subtract(off);
//						return sp.clip(st, pContext.getTo(), pos);
						return sp.clip(st, ed, pos);
					}
					return result;
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
		BlockPos rp = region.pos.toBlockPos();
		int xo = ((pBlockPos.getX()) / upb);
		int yo = ((pBlockPos.getY()) / upb);
		int zo = ((pBlockPos.getZ()) / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac;
		ac = parent.getChunkAt(parentPos);
		
		ISUCapability cap = SUCapabilityManager.getCapability((LevelChunk) ac);
		UnitSpace space = cap.getUnit(parentPos);
		if (space == null) {
			space = cap.getOrMakeUnit(parentPos);
			space.setUpb(upb);
		}
	}
	
	@Override
	public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {
		// TODO
		BlockPos rp = region.pos.toBlockPos();
		int xo = ((pPos.getX()) / upb);
		int yo = ((pPos.getY()) / upb);
		int zo = ((pPos.getZ()) / upb);
		BlockPos parentPos = rp.offset(xo, yo, zo);
		ChunkAccess ac;
		ac = parent.getChunkAt(parentPos);
		
		ISUCapability cap = SUCapabilityManager.getCapability((LevelChunk) ac);
		UnitSpace space = cap.getUnit(parentPos);
		if (space == null) {
			space = cap.getOrMakeUnit(parentPos);
			space.setUpb(upb);
		}
//		BasicVerticalChunk vc = (BasicVerticalChunk) getChunkAt(cp.getWorldPosition());
//		vc = vc.getSubChunk(cy);
//		vc.setBlockFast(new BlockPos(x, y, z), state);
		
		((SUCapableChunk) ac).SU$markDirty(parentPos);
//		super.sendBlockUpdated(pPos, pOldState, pNewState, pFlags);
	}
	
	@Override
	public RecipeManager getRecipeManager() {
		return parent.getRecipeManager();
	}
	
	@Override
	public int getUPB() {
		return upb;
	}
	
	@Override
	public void handleRemoval() {
		// I don't remember what this is
	}
	
	@Nullable
	@Override
	public Entity getEntity(int pId) {
		return super.getEntity(pId);
	}
	
	@Override
	public Iterable<Entity> entitiesForRendering() {
		return getEntities().getAll();
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
	public void tickTime() {
		for (Runnable runnable : completeOnTick) runnable.run();
		completeOnTick.clear();
		
		// TODO: does this need the player position and whatnot to be setup?
		particleEngine.tick();
		
		AABB box = HitboxScaling.getOffsetAndScaledBox(Minecraft.getInstance().player.getBoundingBox(), Minecraft.getInstance().player.position(), upb);
		Vec3 vec = box.getCenter().subtract(0, box.getYsize() / 2, 0);
		BlockPos pos = new BlockPos(vec);
		this.animateTick(pos.getX(), pos.getY(), pos.getZ());

//		this.tickEntities();
		for (Entity entity : entitiesForRendering()) {
			// TODO: remove null entities..?
			if (entity != null) {
				entity.xOld = entity.position().x;
				entity.yOld = entity.position().y;
				entity.zOld = entity.position().z;
				entity.yRotO = entity.getViewYRot(1);
				entity.xRotO = entity.getViewXRot(1);
				entity.tick();
			}
		}
	}
	
	@Override
	public boolean isOutsideBuildHeight(int pY) {
		return false;
	}
	
	@Override
	public int getMinBuildHeight() {
		return -32;
	}
	
	@Override
	public int getMaxBuildHeight() {
		return upb * 512 + 32;
	}
	
	@Override
	public int getSectionsCount() {
		return getMaxSection() - getMinSection();
	}
	
	@Override
	public int getMinSection() {
		return 0;
	}
	
	@Override
	public int getSectionIndexFromSectionY(int pSectionIndex) {
		return pSectionIndex;
	}
	
	@Override
	public int getMaxSection() {
		return upb + 4;
	}
	
	@Override
	public Holder<Biome> getBiome(BlockPos p_204167_) {
		Registry<Biome> reg = registryAccess().registry(Registry.BIOME_REGISTRY).get();
		return reg.getOrCreateHolder(Biomes.THE_VOID);
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
	
	@Override
	public void invalidateCache(BlockPos pos) {
		cache.remove(pos);
	}
}
