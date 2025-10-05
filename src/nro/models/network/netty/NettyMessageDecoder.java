package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import java.io.*;
import java.util.List;

/**
 * Decoder ĐÚNG - Decode từng message một, không đọc cả buffer!
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session == null || session.getSendCollect() == null) {
            return;
        }
        
        // Decode từng message cho đến khi hết buffer
        while (in.readableBytes() >= 3) {
            int readable = in.readableBytes();
            
            System.out.println("📥 DECODER: " + readable + " bytes, sentKey=" + session.sentKey());
            
            in.markReaderIndex();
            
            try {
                // Đọc cmd
                byte cmd = in.readByte();
                if (session.sentKey()) {
                    cmd = session.getSendCollect().readKey(session, cmd);
                }
                
                System.out.println("🔍 CMD=" + cmd);
                
                // Đọc size
                int size;
                if (session.sentKey()) {
                    byte b1 = in.readByte();
                    byte b2 = in.readByte();
                    b1 = session.getSendCollect().readKey(session, b1);
                    b2 = session.getSendCollect().readKey(session, b2);
                    size = ((b1 & 0xFF) << 8) | (b2 & 0xFF);
                } else {
                    size = in.readUnsignedShort();
                }
                
                System.out.println("🔍 SIZE=" + size + ", available=" + in.readableBytes());
                
                // Check đủ bytes
                if (in.readableBytes() < size) {
                    System.out.println("⏳ Not enough, waiting...");
                    in.resetReaderIndex();
                    break; // Chờ thêm data
                }
                
                // Đọc data
                byte[] data = new byte[size];
                in.readBytes(data);
                
                if (session.sentKey()) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = session.getSendCollect().readKey(session, data[i]);
                    }
                }
                
                Message message = new Message(cmd, data);
                out.add(message);
                
                System.out.println("✅ DECODED: cmd=" + cmd + ", size=" + size);
                
                // Tiếp tục decode message tiếp theo nếu còn data
                
            } catch (Exception e) {
                System.out.println("❌ DECODER: " + e.getMessage());
                in.resetReaderIndex();
                break;
            }
        }
    }
}