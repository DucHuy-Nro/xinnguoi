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
            
            System.out.println("📥 V3 DECODER: Processing " + readable + " bytes, sentKey=" + session.sentKey());
            
            // Chuyển ByteBuf → byte array
             int toRead = Math.min(readable, 1024);
            byte[] buffer = new byte[toRead];
            in.markReaderIndex();
            in.readBytes(buffer);
            
            // Tạo DataInputStream
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            DataInputStream dis = new DataInputStream(bais);
            
            // Dùng MessageSendCollect.readMessage() (có handle encryption!)
              Message msg = null;
            try {
                msg = session.getSendCollect().readMessage(session, dis);
            } catch (Exception ex) {
                System.out.println("❌ V3 DECODER readMessage exception: " + ex.getMessage());
                ex.printStackTrace();
                in.resetReaderIndex();
                return;
            }
            
            if (msg != null) {
                // Tính bytes consumed
                int consumed = toRead - dis.available();
                
                // Reset và skip
                in.resetReaderIndex();
                in.skipBytes(consumed);
                
                out.add(msg);
                System.out.println("✅ V3 DECODER: Success! cmd=" + msg.command + ", consumed=" + consumed + " bytes");
                return;
            } else {
                // Rollback
                in.resetReaderIndex();
               System.out.println("⏳ V3 DECODER: Message is null, waiting...");
            }
            
        } catch (Exception e) {
            in.resetReaderIndex();
                        System.out.println("❌ V3 DECODER outer exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}