package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import nro.models.network.Message;
import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Cần ít nhất 3 bytes: cmd (1) + size (2)
        if (in.readableBytes() < 3) {
            return;
        }
        
        in.markReaderIndex();
        
        byte cmd = in.readByte();
        short size = in.readShort();
        
        // Kiểm tra message hợp lệ
        if (size < 0 || size > 1024 * 1024) { // Max 1MB
            ctx.close();
            return;
        }
        
        // Chờ đủ data
        if (in.readableBytes() < size) {
            in.resetReaderIndex();
            return;
        }
        
        // Đọc data
        byte[] data = new byte[size];
        in.readBytes(data);
        
        // Tạo Message object
        Message message = new Message(cmd);
        if (size > 0) {
            message.writer().write(data);
        }
        
        out.add(message);
    }
}