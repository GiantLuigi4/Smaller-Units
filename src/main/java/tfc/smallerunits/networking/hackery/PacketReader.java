package tfc.smallerunits.networking.hackery;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PacketReader extends SimpleChannelInboundHandler<WrapperPacket> {
	public PacketReader() {
	}
	
	public PacketReader(boolean autoRelease) {
		super(autoRelease);
	}
	
	public PacketReader(Class<? extends WrapperPacket> inboundMessageType) {
		super(inboundMessageType);
	}
	
	public PacketReader(Class<? extends WrapperPacket> inboundMessageType, boolean autoRelease) {
		super(inboundMessageType, autoRelease);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WrapperPacket msg) throws Exception {
//		if (msg instanceof WrapperPacket)
//			msg = ((WrapperPacket) msg).wrapped;
		super.channelRead(ctx, msg.wrapped);
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
