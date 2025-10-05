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
        try {
            // Đọc cmd (1 byte)
            byte cmd = in.readByte();
            
            // Đọc size (2 bytes, unsigned short)
            int size = in.readUnsignedShort();
            
            // Validate size
            if (size < 0 || size > 2 * 1024 * 1024) {
                ctx.close();
                return;
            }
            
            // Đọc data
            byte[] data = new byte[size];
            in.readBytes(data);
            
            // Tạo Message (dùng constructor với data)
            Message message = new Message(cmd, data);
            out.add(message);
            
        } catch (Exception e) {
            // ReplayingDecoder tự động rollback nếu chưa đủ data
        }
    }
}