package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import java.io.*;
import java.util.List;

/**
 * Decoder với MessageSendCollect (có encryption)
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        // Chờ session được khởi tạo
        if (session == null || session.getSendCollect() == null) {
            return;
        }
        
        try {
            // Đọc số bytes available
            int readable = in.readableBytes();
            if (readable < 3) {
                return; // Cần ít nhất 3 bytes
            }
            
            // Mark để có thể rollback
            in.markReaderIndex();
            
            // Đọc vào byte array
            byte[] buffer = new byte[readable];
            in.readBytes(buffer);
            
            // Tạo InputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bais);
            
            // Dùng MessageSendCollect để decode (có encryption!)
            Message msg = session.getSendCollect().readMessage(session, dis);
            
            if (msg != null) {
                // Tính số bytes đã consume
                int consumed = readable - dis.available();
                
                // Reset và skip đúng số bytes
                in.resetReaderIndex();
                in.skipBytes(consumed);
                
                out.add(msg);
            } else {
                // Chưa đủ data, rollback
                in.resetReaderIndex();
            }
            
        } catch (Exception e) {
            // Rollback nếu lỗi
            in.resetReaderIndex();
        }
    }
}