package test.test;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class packet implements IMessage {
    public packet() {
    }
    @Override
    public void fromBytes(ByteBuf buf) {
    }
    @Override
    public void toBytes(ByteBuf buf) {
    }
    public class handler implements IMessageHandler<packet,IMessage> {
        @Override
        public IMessage onMessage(packet message, MessageContext ctx) {
            return null;
        }
    }
}
