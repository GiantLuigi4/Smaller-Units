package tfc.smallerunits.utils.world.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Queues;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.*;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.ModList;
import sun.misc.Unsafe;
import tfc.smallerunits.utils.world.server.BlankPOIManager;
import tfc.smallerunits.utils.world.server.FakeServerChunkProvider;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FakeChunkManager extends ChunkManager {
	private static final Unsafe theUnsafe;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public FakeServerChunkProvider provider;
	private boolean hasInit;
	
	public FakeChunkManager(ServerWorld p_i232602_1_, SaveFormat.LevelSave p_i232602_2_, DataFixer p_i232602_3_, TemplateManager p_i232602_4_, Executor p_i232602_5_, ThreadTaskExecutor<Runnable> p_i232602_6_, IChunkLightProvider p_i232602_7_, ChunkGenerator p_i232602_8_, IChunkStatusListener p_i232602_9_, Supplier<DimensionSavedDataManager> p_i232602_10_, int p_i232602_11_, boolean p_i232602_12_) {
		super(p_i232602_1_, p_i232602_2_, p_i232602_3_, p_i232602_4_, p_i232602_5_, p_i232602_6_, p_i232602_7_, p_i232602_8_, p_i232602_9_, p_i232602_10_, p_i232602_11_, p_i232602_12_);
	}
	
	@Override
	public void setPlayerTracking(ServerPlayerEntity player, boolean track) {
	}
	
	@Override
	public Stream<ServerPlayerEntity> getTrackingPlayers(ChunkPos pos, boolean boundaryOnly) {
		if (provider.world instanceof FakeServerWorld) {
			TileEntity te = ((FakeServerWorld) provider.world).owner;
			return ((ServerChunkProvider) te.getWorld().getChunkProvider()).chunkManager.getTrackingPlayers(
					new ChunkPos(te.getPos()), boundaryOnly
			);
		}
		return Stream.empty();
	}
	
	private static final Thread td = new Thread();
	private static final ThreadTaskExecutor<Runnable> executor = new ThreadTaskExecutor<Runnable>("a") {
		@Override
		protected Runnable wrapTask(Runnable runnable) {
			return runnable;
		}
		
		@Override
		protected boolean canRun(Runnable runnable) {
			return true;
		}
		
		@Override
		protected Thread getExecutionThread() {
			return td;
		}
	};
	
	public void init() {
		if (!hasInit) {
			this.entities = new Int2ObjectOpenHashMap<>();
			try {
				pointOfInterestManager = (PointOfInterestManager) theUnsafe.allocateInstance(BlankPOIManager.class);
//				pointOfInterestManager = (PointOfInterestManager) theUnsafe.allocateInstance(PointOfInterestManager.class);
				this.ticketManager = new FakeProxyTicketManager(Runnable::run, Runnable::run);
				this.lightManager = (ServerWorldLightManager) provider.theChunk.getWorld().getLightManager();
				this.mainThread = executor;
			} catch (Throwable err) {
				throw new RuntimeException(err);
			}
			//TODO: Cubic Chunks Support
			if (ModList.get().isLoaded("cubicchunks")) {
				try {
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("visibleCubeMap")), new Long2ObjectLinkedOpenHashMap<>());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("updatingCubeMap")), new Long2ObjectLinkedOpenHashMap<>());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("cubesToDrop")), new LongOpenHashSet());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("cubeEntitiesInLevel")), new LongOpenHashSet());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("pendingCubeUnloads")), new Long2ObjectLinkedOpenHashMap<>());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("tickingGeneratedCubes")), new AtomicInteger());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("cubeTypeCache")), new Long2ByteOpenHashMap());
					theUnsafe.getAndSetObject(this, theUnsafe.objectFieldOffset(this.getClass().getDeclaredField("cubeUnloadQueue")), Queues.newConcurrentLinkedQueue());
				} catch (Throwable ignored) {
				}
			}
			hasInit = true;
		}
	}
	
	@Override
	public Iterable<ChunkHolder> getLoadedChunksIterable() {
		return ImmutableSet.of(((FakeTicketManager) provider.ticketManager).getChunkHolder(0));
	}
	
	public class FakeProxyTicketManager extends ProxyTicketManager {
		public FakeProxyTicketManager(Executor p_i50469_2_, Executor p_i50469_3_) {
			super(p_i50469_2_, p_i50469_3_);
		}
	}
}
