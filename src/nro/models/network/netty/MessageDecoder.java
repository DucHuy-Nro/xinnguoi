package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import nro.models.network.Message;
import nro.models.interfaces.ISession;
import java.util.List;

/**
 * Decode ByteBuf → Message
 * Tương thích với protocol cũ (có key encryption)
 */
public class MessageDecoder extends ByteToMessageDecoder {
    
    private static final int MAX_MESSAGE_SIZE = 2 * 1024 * 1024; // 2MB
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Netty session
        NettySession session = (NettySession) ctx.channel().attr(
            io.netty.util.AttributeKey.valueOf("session")).get();
        
        if (session == null) {
            return;
        }
        
        // Chuyển ByteBuf sang byte array để MessageSendCollect xử lý
        try {
            // Đọc tất cả bytes available
            int available = in.readableBytes();
            if (available == 0) {
                return;
            }
            
            byte[] buffer = new byte[available];
            in.readBytes(buffer);
            
            // Tạo DataInputStream từ buffer
            java.io.DataInputStream dis = new java.io.DataInputStream(
                new java.io.ByteArrayInputStream(buffer)
            );
            
            // Dùng MessageSendCollect để decode (có handle encryption!)
            if (session.getSendCollect() != null) {
                Message msg = session.getSendCollect().readMessage(session, dis);
                if (msg != null) {
                    out.add(msg);
                }
            }
            
        } catch (Exception e) {
            // Ignore và đợi data tiếp theo
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Không close ngay, có thể là lỗi tạm thời
    }
}