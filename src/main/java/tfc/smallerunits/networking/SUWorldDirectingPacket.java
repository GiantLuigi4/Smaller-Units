//package com.tfc.smallerunits.networking;
//
//import com.tfc.smallerunits.helpers.PacketHacksHelper;
//import com.tfc.smallerunits.mixins.SimpleChannelAccessor;
//import net.minecraft.network.INetHandler;
//import net.minecraft.network.IPacket;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.network.NetworkInstance;
//import net.minecraftforge.fml.network.NetworkRegistry;
//import net.minecraftforge.fml.network.simple.SimpleChannel;
//
//import java.lang.reflect.Field;
//import java.util.Map;
//
//public class SUWorldDirectingPacket<MSG> implements IPacket {
//	public ResourceLocation network_instance;
//	public BlockPos pos;
//	public World world;
//	public MSG wrapped;
//	public NetworkInstance instance;
//	public PacketBuffer buffer;
//
//	private static final Map<ResourceLocation, NetworkInstance> networkInstances;
//
//	static {
//		try {
//			Field f = NetworkRegistry.class.getDeclaredField("instances");
//			f.setAccessible(true);
//			networkInstances = (Map<ResourceLocation, NetworkInstance>) f.get(null);
//		} catch (Throwable err) {
//			throw new RuntimeException(err);
//		}
//	}
//
//	public SUWorldDirectingPacket(ResourceLocation network_instance, BlockPos pos, MSG wrapped) {
//		this.network_instance = network_instance;
//		this.pos = pos;
//		this.wrapped = wrapped;
//	}
//
//	public SUWorldDirectingPacket(PacketBuffer buffer) {
//		this.readPacketData(buffer);
//	}
//
//	@Override
//	public void readPacketData(PacketBuffer buf) {
//		network_instance = new ResourceLocation(buf.readString());
//		pos = buf.readBlockPos();
//		instance = networkInstances.get(network_instance);
//		buffer = buf;
//	}
//
//	@Override
//	public void writePacketData(PacketBuffer buf) {
//		buf.writeString(network_instance.toString());
//		buf.writeBlockPos(pos);
//		SimpleChannel channel = PacketHacksHelper.channelHashMap.get(network_instance);
//		System.out.println(channel);
//		((SimpleChannelAccessor) channel)
//				.smaller_units_getCodec()
//				.build(wrapped, buf);
//	}
//
//	@Override
//	public void processPacket(INetHandler handler) {
//
//	}
//}
