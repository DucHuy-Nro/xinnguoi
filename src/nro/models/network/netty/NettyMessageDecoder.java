package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import java.io.*;
import java.util.List;

/**
 * Decoder với MessageSendCollect (xử lý encryption)
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session == null) {
            return;
        }
        
        // Đợi có sendCollect (set trong sessionInit)
        if (session.getSendCollect() == null) {
            return;
        }
        
        try {
            int readable = in.readableBytes();
            if (readable == 0) {
                return;
            }
            
            System.out.println("📥 V3 DECODER: Processing " + readable + " bytes");
            
            // Chuyển ByteBuf → byte array
            byte[] buffer = new byte[readable];
            in.markReaderIndex();
            in.readBytes(buffer);
            
            // Tạo DataInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bais);
            
            // Dùng MessageSendCollect.readMessage() (có handle encryption!)
            Message msg = session.getSendCollect().readMessage(session, dis);
            
            if (msg != null) {
                // Tính bytes consumed
                int consumed = readable - dis.available();
                
                // Reset và skip
                in.resetReaderIndex();
                in.skipBytes(consumed);
                
                out.add(msg);
                System.out.println("✅ V3 DECODER: Success! cmd=" + msg.command);
            } else {
                // Rollback
                in.resetReaderIndex();
                System.out.println("⏳ V3 DECODER: Not complete yet");
            }
            
        } catch (Exception e) {
            in.resetReaderIndex();
            System.out.println("❌ V3 DECODER: " + e.getMessage());
        }
    }
}