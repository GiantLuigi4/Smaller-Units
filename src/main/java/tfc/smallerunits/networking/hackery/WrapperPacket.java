package tfc.smallerunits.networking.hackery;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import sun.misc.Unsafe;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.networking.platform.NetCtx;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.PositionalInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.BiFunction;

public class WrapperPacket extends tfc.smallerunits.networking.Packet {
	private static final Unsafe theUnsafe;
	public CompoundTag additionalInfo = null;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	Object wrapped;
	boolean hasRead = false;
	private HashMap<String, Object> objs = new HashMap<>();
	
	public WrapperPacket(FriendlyByteBuf pBuffer) {
		wrapped = read(pBuffer);
	}
	
	public WrapperPacket(Object wrapped) {
		for (String name : InfoRegistry.names()) {
			Tag tg = InfoRegistry.supplier(name).get();
			if (tg != null) {
				if (additionalInfo == null) additionalInfo = new CompoundTag();
				additionalInfo.put(name, tg);
			}
		}
		if (wrapped instanceof FriendlyByteBuf) wrapped = read((FriendlyByteBuf) wrapped);
		else this.wrapped = wrapped;
	}
	
	@Override
	public void write(FriendlyByteBuf pBuffer) {
		if (wrapped instanceof Packet) {
			pBuffer.writeBoolean(additionalInfo != null);
			if (additionalInfo != null) pBuffer.writeNbt(additionalInfo);
			pBuffer.writeByteArray(wrapped.getClass().getName().getBytes(StandardCharsets.UTF_8));
			((Packet<?>) wrapped).write(pBuffer);
		}
	}
	
	public Object read(FriendlyByteBuf obj) {
		NetworkingHacks.increaseBlockPosPrecision.set(true);
		try {
			preRead(obj);
			String name = new String(obj.readByteArray());
//			System.out.println(name);
			Class<?> clazz = Class.forName(name);
			Object obj1 = theUnsafe.allocateInstance(clazz);
			if (obj1 instanceof Packet) {
				for (Constructor<?> constructor : obj1.getClass().getConstructors()) {
					if (constructor.getParameterCount() == 1) {
						if (constructor.getParameters()[0].getType().equals(FriendlyByteBuf.class)) {
							return constructor.newInstance(new FriendlyByteBuf(obj));
						}
					}
				}
			}
		} catch (Throwable err) {
			theUnsafe.throwException(err);
		}
		NetworkingHacks.increaseBlockPosPrecision.remove();
		return null;
	}
	
	public void teardown(NetworkContext connection) {
		for (String s : objs.keySet()) InfoRegistry.reseter(s).accept(objs.get(s), connection);
	}
	
	public void preRead(NetworkContext connection) {
		if (hasRead) return;
		hasRead = true;
		if (additionalInfo != null) {
			for (String allKey : additionalInfo.getAllKeys()) {
				BiFunction<Tag, NetworkContext, Object> consumer = InfoRegistry.consumer(allKey);
				if (consumer != null) objs.put(allKey, consumer.apply(additionalInfo.get(allKey), connection));
			}
		}
	}
	
	private void preRead(FriendlyByteBuf obj) {
		if (obj.readBoolean()) {
			additionalInfo = obj.readNbt();
		}
	}
	
	@Override
	public void handle(NetCtx ctx) {
		ctx.setPacketHandled(true);
		// TODO: I don't know why this happens
		if (wrapped == null) return;
		
		Player player = ctx.getSender();
		
		BlockableEventLoop<?> pExecutor = null;
		if (player != null) {
			if (player.getServer() != null) {
				pExecutor = player.getServer();
			} else if (player.level.isClientSide) {
				pExecutor = (BlockableEventLoop<?>) IHateTheDistCleaner.getMinecraft();
			}
		} else {
			pExecutor = (BlockableEventLoop<?>) IHateTheDistCleaner.getMinecraft();
		}
		if (!pExecutor.isSameThread()) {
			pExecutor.executeIfPossible(() -> {
				doHandle(ctx);
			});
		} else {
			doHandle(ctx);
		}
	}
	
	protected void doHandle(NetCtx ctx) {
		NetworkingHacks.increaseBlockPosPrecision.set(true);
		NetworkContext context = new NetworkContext(ctx.getHandler(), ((PacketListenerAccessor) ctx.getHandler()).getPlayer(), ((Packet) this.wrapped));
		
		PositionalInfo info = new PositionalInfo(context.player);
		
		preRead(context);
		PacketUtilMess.preHandlePacket(ctx.getHandler(), context.pkt);
		
		Object old = null;
		boolean toServer = checkServer(ctx);
		if (toServer) old = context.player.containerMenu;
		else old = IHateTheDistCleaner.getScreen();
		// get level
		Level preHandleLevel = context.player.level;
		int upb = 0;
		if (preHandleLevel instanceof ITickerLevel tl) upb = tl.getUPB();
		// TODO: debug this garbage
		((PacketListenerAccessor) ctx.getHandler()).setWorld(preHandleLevel);
		
		NetworkingHacks.currentContext.set(new NetworkHandlingContext(
				context, info, ctx.getDirection(), preHandleLevel
		));
		
		try {
			context.pkt.handle(ctx.getHandler());
		} catch (Throwable ignored) {
			Loggers.PACKET_HACKS_LOGGER.error("-- A wrapped packet has encountered an error: desyncs are imminent --");
			ignored.printStackTrace();
		}
		
		if (toServer) {
			Object newV = context.player.containerMenu;
			if (old != newV) {
				if (newV != context.player.inventoryMenu) {
					NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
					((SUScreenAttachments) newV).setup(info, preHandleLevel, upb, descriptor.pos());
				}
			}
		} else {
			Object newV = IHateTheDistCleaner.getScreen();
			if (old != newV) {
				if (newV != null) {
					NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
					((SUScreenAttachments) newV).setup(info, preHandleLevel, upb, descriptor.pos());
				}
			}
		}
		
		PacketUtilMess.postHandlePacket(ctx.getHandler(), context.pkt);
		teardown(context);
		NetworkingHacks.increaseBlockPosPrecision.remove();
		NetworkingHacks.unitPos.remove();
		NetworkingHacks.currentContext.remove();
	}
	
	@Override
	public boolean isSkippable() {
		return false;
	}
}
