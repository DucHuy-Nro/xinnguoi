package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import java.io.*;
import java.util.List;

/**
 * Decoder đơn giản - KHÔNG dùng MessageSendCollect
 * Đọc plain binary, để MessageHandler xử lý encryption
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Cần ít nhất 3 bytes: [cmd:1][size:2]
        int readable = in.readableBytes();
        if (readable < 3) {
            return;
        }
        
        System.out.println("📥 DECODER: Readable=" + readable + " bytes");
        
        // Mark position
        in.markReaderIndex();
        
        try {
            // Đọc header
            byte cmd = in.readByte();
            int size = in.readUnsignedShort();
            
            System.out.println("📥 DECODER: cmd=" + cmd + ", size=" + size);
            
            // Validate size
            if (size < 0 || size > 1024 * 1024) {
                System.out.println("❌ DECODER: Invalid size!");
                ctx.close();
                return;
            }
            
            // Check nếu đủ data
            if (in.readableBytes() < size) {
                System.out.println("⏳ DECODER: Not enough data, waiting...");
                in.resetReaderIndex();
                return;
            }
            
            // Đọc data
            byte[] data = new byte[size];
            in.readBytes(data);
            
            // Tạo message
            Message message = new Message(cmd, data);
            out.add(message);
            
            System.out.println("✅ DECODER: Message decoded successfully");
            
        } catch (Exception e) {
            System.out.println("❌ DECODER: Error - " + e.getMessage());
            in.resetReaderIndex();
        }
    }
}