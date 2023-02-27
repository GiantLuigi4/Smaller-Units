package tfc.smallerunits.networking.hackery;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class PacketReader extends SimpleChannelInboundHandler<WrapperPacket> {
	private final Player player;
	private final Connection connection;
	
	public PacketReader(Player player, Connection connection) {
		this.player = player;
		this.connection = connection;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WrapperPacket msg) throws Exception {
//		if (msg instanceof WrapperPacket)
//			msg = ((WrapperPacket) msg).wrapped;
		NetworkContext context = new NetworkContext(connection.getPacketListener(), player, (Packet) msg.wrapped);
		msg.preRead(context);
		super.channelRead(ctx, msg.wrapped);
		msg.teardown(context);
	}
	
	@Override
	public boolean acceptInboundMessage(Object msg) throws Exception {
		return super.acceptInboundMessage(msg);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}
}
