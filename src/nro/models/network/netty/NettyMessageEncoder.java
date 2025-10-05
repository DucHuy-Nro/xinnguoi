package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;
import nro.models.network.Message;

/**
 * Encoder đơn giản: Message → ByteBuf
 * Format: [cmd:1byte][size:2bytes][data:nbytes]
 */
public class NettyMessageEncoder extends MessageToByteEncoder<Message> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        try {
            byte[] data = msg.getData();
            
            // Write: [cmd][size][data]
            out.writeByte(msg.command);
            out.writeShort(data.length);
            
            if (data.length > 0) {
                out.writeBytes(data);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        }
    }
}