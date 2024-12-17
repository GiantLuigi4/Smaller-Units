package tfc.smallerunits.networking.hackery;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import sun.misc.Unsafe;
import tfc.smallerunits.data.access.PacketListenerAccessor;
import tfc.smallerunits.data.access.SUScreenAttachments;
import tfc.smallerunits.logging.Loggers;
import tfc.smallerunits.plat.net.NetCtx;
import tfc.smallerunits.plat.net.NetworkDirection;
import tfc.smallerunits.plat.util.PlatformUtils;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.IHateTheDistCleaner;
import tfc.smallerunits.utils.PositionalInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.BiFunction;

public class WrapperPacket extends tfc.smallerunits.plat.net.Packet {
    private static final Unsafe theUnsafe;
    public CompoundTag additionalInfo = null;
    protected boolean isBundle = false;

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

    PacketFlow flow;

    public WrapperPacket(Object wrapped, PacketFlow flow) {
        this.flow = flow;

        for (String name : InfoRegistry.names()) {
            Tag tg = InfoRegistry.supplier(name).get();
            if (tg != null) {
                if (additionalInfo == null) additionalInfo = new CompoundTag();
                additionalInfo.put(name, tg);
            }
        }
        if (wrapped instanceof FriendlyByteBuf) this.wrapped = read((FriendlyByteBuf) wrapped);
        else this.wrapped = wrapped;
    }

    @Override
    public void write(FriendlyByteBuf pBuffer) {
        if (wrapped instanceof Packet) {
            pBuffer.writeBoolean(additionalInfo != null);
            if (additionalInfo != null) pBuffer.writeNbt(additionalInfo);

            pBuffer.writeByte(flow.ordinal());
            int id = ConnectionProtocol.PLAY.getPacketId(flow, (Packet<?>) wrapped);
            if (id == -1) {
                pBuffer.writeInt(id);
                if (wrapped.getClass().equals(ClientboundBundlePacket.class)) {
                    pBuffer.writeBoolean(false);
                    BundledPacketHandler.writeBundle((ClientboundBundlePacket) wrapped, pBuffer, ConnectionProtocol.PLAY, flow);
                } else {
                    pBuffer.writeBoolean(true);
                    System.err.println("Writing invalid packet... what?");
                }
            } else {
                pBuffer.writeInt(id);
                ((Packet<?>) wrapped).write(pBuffer);
            }
        }
    }

    public Object read(FriendlyByteBuf obj) {
        NetworkingHacks.increaseBlockPosPrecision.set(true);
        try {
            preRead(obj);

            this.flow = obj.readByte() == 0 ? PacketFlow.SERVERBOUND : PacketFlow.CLIENTBOUND;
            int id = obj.readInt();
            if (id == -1) {
                if (obj.readBoolean()) {
                    System.err.println("Received invalid packet... what?");
                    return null;
                }
                wrapped = BundledPacketHandler.readBundle(obj, ConnectionProtocol.PLAY, flow);
                isBundle = true;
            } else {
                wrapped = ConnectionProtocol.PLAY.createPacket(flow, id, obj);
            }
            NetworkingHacks.increaseBlockPosPrecision.remove();
            return wrapped;
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

        if (wrapped instanceof ServerboundMovePlayerPacket) {
            Loggers.SU_LOGGER.warn("Move packet received in a wrapper packet on server...");
            return;
        }

        Player player = ctx.getSender();

        BlockableEventLoop<?> pExecutor = null;
        if (player != null) {
            if (player.getServer() != null) {
                pExecutor = player.getServer();
            } else if (player.level().isClientSide) {
                pExecutor = (BlockableEventLoop<?>) IHateTheDistCleaner.getMinecraft();
            }
        } else {
            pExecutor = (BlockableEventLoop<?>) IHateTheDistCleaner.getMinecraft();
            player = IHateTheDistCleaner.getPlayer();
        }

        // vanilla
        if (!pExecutor.isSameThread()) {
            pExecutor.executeIfPossible(() -> {
                doHandle(ctx);
            });
        } else doHandle(ctx);
    }

    protected void doHandle(NetCtx ctx) {
        NetworkingHacks.increaseBlockPosPrecision.set(true);
        //TODO is it SERVERBOUND?
        NetworkContext context = new NetworkContext(new Connection(PacketFlow.SERVERBOUND), ((PacketListenerAccessor) ctx.getHandler()).getPlayer(), ((Packet) this.wrapped));

        PositionalInfo info = new PositionalInfo(context.player);

        preRead(context);
        try {
            PacketUtilMess.preHandlePacket(ctx.getHandler(), context.pkt);
        } catch (Throwable err) {
            if ((err instanceof ClassCastException) || (err.getCause() instanceof ClassCastException)) {
//				if (castException.toString().startsWith("class net.minecraft.client.multiplayer.ClientLevel cannot be cast to class tfc.smallerunits.simulation.level.ITickerLevel")) {
//					if (err.getStackTrace()[0].getLineNumber() == 47) {
//						// fully recoverable in this scenario, for some reason
//						Loggers.SU_LOGGER.warn("Failed to handle packet " + wrapped + ".\nHowever, this should be recoverable.");
//						return;
//					}
//				}
                if (!(Minecraft.getInstance().level instanceof ITickerLevel)) {
                    Loggers.SU_LOGGER.warn("Failed to handle packet " + wrapped + ".\nHowever, this should be recoverable.");
                    if (err.getStackTrace() != null) {
                        Loggers.SU_LOGGER.warn("Exception: ", err);
                    }
                    try {
                        PacketUtilMess.postHandlePacket(ctx.getHandler(), context.pkt);
                        teardown(context);
                        NetworkingHacks.increaseBlockPosPrecision.remove();
                        NetworkingHacks.unitPos.remove();
                        NetworkingHacks.currentContext.remove();
                    } catch (Throwable ignored) {
                    }
                    return;
                }
            }
            throw new RuntimeException(err);
        }

        Object old = null;
        boolean toServer = ctx.getDirection() == NetworkDirection.TO_SERVER;
        if (toServer) old = context.player.containerMenu;
        else old = IHateTheDistCleaner.getScreen();
        // get level
        Level preHandleLevel = context.player.level();
        int upb = 0;
        if (preHandleLevel instanceof ITickerLevel tl) upb = tl.getUPB();
        // TODO: debug this garbage
        ((PacketListenerAccessor) ctx.getHandler()).setWorld(preHandleLevel);

        NetworkingHacks.currentContext.set(new NetworkHandlingContext(
                context, info, ctx.getDirection(), preHandleLevel
        ));

        try {
            PacketListener listener = ctx.getHandler();
            if (isBundle) {
                ((BundledPacketHandler) wrapped).handle(listener, context);
            } else if (context.pkt instanceof ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
                PlatformUtils.customPayload(clientboundCustomPayloadPacket, context, listener);
            } else {
                context.pkt.handle(listener);
            }
        } catch (Throwable ignored) {
            Loggers.PACKET_HACKS_LOGGER.error("-- A wrapped packet has encountered an error: desyncs are imminent --");
            ignored.printStackTrace();
        }

        if (toServer) {
            Object newV = context.player.containerMenu;
            if (old != newV) {
                if (newV != context.player.inventoryMenu) {
                    NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
                    ((SUScreenAttachments) newV).setup(info, preHandleLevel, descriptor);
                }
            }
        } else {
            Object newV = IHateTheDistCleaner.getScreen();
            if (old != newV) {
                if (newV != null) {
                    NetworkingHacks.LevelDescriptor descriptor = NetworkingHacks.unitPos.get();
                    ((SUScreenAttachments) newV).setup(info, preHandleLevel, descriptor);
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
