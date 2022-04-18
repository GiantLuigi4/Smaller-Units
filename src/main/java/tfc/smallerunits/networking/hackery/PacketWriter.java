package tfc.smallerunits.networking.hackery;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class PacketWriter extends ChannelDuplexHandler {
	public PacketWriter() {
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		ctx.write(
				new WrapperPacket(msg),
				promise
		);
	}
}
