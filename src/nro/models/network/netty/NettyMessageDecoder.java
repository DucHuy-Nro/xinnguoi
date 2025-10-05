package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ReplayingDecoder;
import nro.models.network.Message;
import java.util.List;

/**
 * Decoder tương thích 100% với protocol cũ
 * Đọc: [cmd:1byte][size:2bytes][data:nbytes]
 */
public class NettyMessageDecoder extends ReplayingDecoder<Void> {
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        System.out.println("📥 DECODER: Received " + in.readableBytes() + " bytes");
        
        try {
            byte cmd = in.readByte();
            int size = in.readUnsignedShort();
            
            System.out.println("📥 DECODER: cmd=" + cmd + ", size=" + size);
            
            if (size < 0 || size > 2 * 1024 * 1024) {
                System.out.println("❌ DECODER: Invalid size!");
                ctx.close();
                return;
            }
            
            byte[] data = new byte[size];
            in.readBytes(data);
            
            Message message = new Message(cmd, data);
            out.add(message);
            
            System.out.println("✅ DECODER: Message decoded successfully");
            
        } catch (Exception e) {
            System.out.println("❌ DECODER: Error - " + e.getMessage());
        }
    }
}
