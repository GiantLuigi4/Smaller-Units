package tfc.smallerunits.utils.world.server;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerTickList;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.mixins.referenceremoval.ServerTickListAccessor;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.ExternalUnitInteractionContext;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FakeServerTickList<T> extends ServerTickList<T> {
	private FakeServerWorld world1;
	private final boolean isBlock;
	
	public FakeServerTickList(FakeServerWorld p_i231625_1_, Predicate<T> p_i231625_2_, Function<T, ResourceLocation> p_i231625_3_, Consumer<NextTickListEntry<T>> p_i231625_4_, boolean isBlock) {
		super(p_i231625_1_, p_i231625_2_, p_i231625_3_, p_i231625_4_);
		this.world1 = p_i231625_1_;
		this.isBlock = isBlock;
	}
	
	@Override
	public void scheduleTick(BlockPos pos, T itemIn, int scheduledTime, TickPriority priority) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(world1, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(world1.owner.getPos())) {
					if (context.teInRealWorld instanceof UnitTileEntity) {
						if (isBlock) {
							((UnitTileEntity) context.teInRealWorld).worldServer.getPendingBlockTicks().scheduleTick(context.posInFakeWorld, (Block) itemIn, scheduledTime, priority);
						} else {
							((UnitTileEntity) context.teInRealWorld).worldServer.getPendingFluidTicks().scheduleTick(context.posInFakeWorld, (Fluid) itemIn, scheduledTime, priority);
						}
						return;
					}
				}
			}
		}
		super.scheduleTick(pos, itemIn, scheduledTime, priority);
	}
	
	@Override
	public boolean isTickPending(BlockPos pos, T obj) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(world1, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(world1.owner.getPos())) {
					if (context.teInRealWorld instanceof UnitTileEntity) {
						return isTickScheduled(context.posInFakeWorld, obj);
					}
				}
			}
		}
		return super.isTickPending(pos, obj);
	}
	
	@Override
	public boolean isTickScheduled(BlockPos pos, T itemIn) {
		ExternalUnitInteractionContext context = new ExternalUnitInteractionContext(world1, pos);
		if (context.stateInRealWorld != null) {
			if (context.stateInRealWorld.equals(Deferred.UNIT.get().getDefaultState())) {
				if (!context.posInRealWorld.equals(world1.owner.getPos())) {
					if (context.teInRealWorld instanceof UnitTileEntity) {
						return isTickScheduled(context.posInFakeWorld, itemIn);
					}
				}
			}
		}
		return super.isTickScheduled(pos, itemIn);
	}
	
	public void close() {
		world1 = null;
		((ServerTickListAccessor) this).setWorld(null);
	}
}
