package tfc.smallerunits.networking.hackery;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.player.Player;

// TODO: I don't think this is needed
public class PacketWriter extends ChannelDuplexHandler {
	private final Connection connection;
	private final Player player;
	
	public PacketWriter(Player player, Connection connection) {
		this.connection = connection;
		this.player = player;
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//		System.out.println(msg.getClass());
//		System.out.println(Thread.currentThread().getName());
//		WrapperPacket pkt = new WrapperPacket(msg);
//		if (pkt.additionalInfo != null) {
//			super.write(ctx, pkt, promise);
//			return;
//		}
		super.write(ctx, msg, promise);
	}
}
