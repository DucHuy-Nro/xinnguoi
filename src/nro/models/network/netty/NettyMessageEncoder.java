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
        System.out.println("📤 ENCODER: Encoding message cmd=" + msg.command);
        
        try {
            byte[] data = msg.getData();
            
            out.writeByte(msg.command);
            out.writeShort(data.length);
            
            if (data.length > 0) {
                out.writeBytes(data);
            }
            
            System.out.println("✅ ENCODER: Message encoded, size=" + (data.length + 3));
            
        } catch (Exception e) {
            System.out.println("❌ ENCODER: Error - " + e.getMessage());
            e.printStackTrace();
            ctx.close();
        }
    }
}
