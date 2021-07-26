package tfc.smallerunits.utils.world.common;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.util.concurrent.ITaskExecutor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkTaskPriorityQueueSorter;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.TicketManager;
import net.minecraft.world.server.TicketType;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import tfc.smallerunits.utils.world.server.FakeServerChunkProvider;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;

public class FakeTicketManager extends TicketManager {
	public FakeServerChunkProvider provider;
	private boolean hasInit;
	
	public FakeTicketManager(Executor p_i50707_1_, Executor p_i50707_2_) {
		super(p_i50707_1_, p_i50707_2_);
	}
	
	public void init() {
		if (!hasInit) {
			tickets = new Long2ObjectOpenHashMap<>();
			
			playerChunkTracker = new FakePlayerChunkTracker(8);
			playerTicketTracker = new FakePlayerTicketTracker(33);
			
			ticketTracker = new ChunkTicketTracker();
			
			chunkHolders = new Array2DHashSet<>();
			chunkPositions = new LongOpenHashSet();
			
			setup(this::run, this::run);
			
			hasInit = true;
		}
	}
	
	public void run(Runnable runnable) {
		runnable.run();
	}
	
	protected void setup(Executor p_i50707_1_, Executor p_i50707_2_) {
		ITaskExecutor<Runnable> itaskexecutor = ITaskExecutor.inline("player ticket throttler", p_i50707_2_::execute);
		ChunkTaskPriorityQueueSorter chunktaskpriorityqueuesorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(itaskexecutor), p_i50707_1_, 4);
		this.field_219384_l = chunktaskpriorityqueuesorter;
		this.field_219385_m = chunktaskpriorityqueuesorter.func_219087_a(itaskexecutor, true);
		this.field_219386_n = chunktaskpriorityqueuesorter.func_219091_a(itaskexecutor);
		this.field_219388_p = p_i50707_2_;
	}
	
	@Override
	public <T> void releaseWithLevel(TicketType<T> type, ChunkPos pos, int level, T value) {
	}
	
	@Override
	protected boolean contains(long p_219371_1_) {
		return true;
	}
	
	@Nullable
	@Override
	public ChunkHolder getChunkHolder(long chunkPosIn) {
		return provider.func_217213_a(chunkPosIn);
	}
	
	@Nullable
	@Override
	public ChunkHolder setChunkLevel(long chunkPosIn, int newLevel, @Nullable ChunkHolder holder, int oldLevel) {
		return provider.func_217213_a(chunkPosIn);
	}
	
	@Override
	public int getSpawningChunksCount() {
		return 0;
	}
	
	public class FakePlayerChunkTracker extends PlayerChunkTracker {
		public FakePlayerChunkTracker(int levelCount) {
			super(levelCount);
		}
	}
	
	public class FakePlayerTicketTracker extends PlayerTicketTracker {
		public FakePlayerTicketTracker(int levelCount) {
			super(levelCount);
		}
	}
}
