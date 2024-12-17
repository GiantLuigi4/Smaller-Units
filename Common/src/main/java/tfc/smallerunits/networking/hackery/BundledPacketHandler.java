package tfc.smallerunits.networking.hackery;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import tfc.smallerunits.plat.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;

public class BundledPacketHandler implements Packet {
    public static void writeBundle(ClientboundBundlePacket wrapped, FriendlyByteBuf pBuffer, ConnectionProtocol protocol, PacketFlow flow) {
        for (Packet<ClientGamePacketListener> subPacket : wrapped.subPackets()) {
            int id = protocol.getPacketId(flow, subPacket);
            pBuffer.writeInt(id);
            subPacket.write(pBuffer);
        }
    }

    List<Packet> children = new ArrayList<>();

    public static BundledPacketHandler readBundle(FriendlyByteBuf obj, ConnectionProtocol protocol, PacketFlow flow) {
        BundledPacketHandler result = new BundledPacketHandler();
        while (obj.isReadable()) {
            int id = obj.readInt();
            protocol.createPacket(flow, id, obj);
        }
        return result;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        throw new RuntimeException("Unsupported: this packet should not get sent");
    }

    @Override
    public void handle(PacketListener packetListener) {
        throw new RuntimeException("Unsupported: use the version with a network context");
    }

    public void handle(PacketListener packetListener, NetworkContext context) {
        for (Packet child : children) {
            if (child instanceof ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
                PlatformUtils.customPayload(clientboundCustomPayloadPacket, context, packetListener);
            } else {
                child.handle(packetListener);
            }
        }
    }
}
