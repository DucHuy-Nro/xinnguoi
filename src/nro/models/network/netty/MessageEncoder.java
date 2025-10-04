package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;
import nro.models.network.Message;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        try {
            byte[] data = msg.getData();
            
            // Write format: [cmd:1byte][size:2bytes][data:nbytes]
            out.writeByte(msg.command);
            out.writeShort(data.length);
            out.writeBytes(data);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}