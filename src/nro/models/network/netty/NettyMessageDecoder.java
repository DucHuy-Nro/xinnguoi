package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import java.util.List;

/**
 * Decoder với MessageSendCollect (xử lý encryption)
 */
public class NettyMessageDecoder extends ByteToMessageDecoder {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session == null || session.getSendCollect() == null) {
            return;
        }
        

        // Cần ít nhất 3 bytes
        if (in.readableBytes() < 3) {
            return;
        }

//        System.out.println("📥 DECODER: " + in.readableBytes() + " bytes, sentKey=" + session.sentKey());

        // ⭐ BACKUP curR
        int savedCurR = ((nro.models.network.netty.NettyMessageSendCollect)session.getSendCollect()).getCurR();

        in.markReaderIndex();

        try {
            // Đọc cmd từ ByteBuf
            byte cmd = in.readByte();
            if (session.sentKey()) {
                cmd = session.getSendCollect().readKey(session, cmd);
            }
//    System.out.println("🔍 CMD=" + cmd);
            
     
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
          
            

//           System.out.println("🔍 SIZE=" + size + ", avail=" + in.readableBytes());

            // Check đủ bytes
            if (in.readableBytes() < size) {
//                System.out.println("⏳ Not enough! Rollback curR: " + savedCurR);
                // ⭐ ROLLBACK curR
                ((nro.models.network.netty.NettyMessageSendCollect)session.getSendCollect()).setCurR(savedCurR);
                in.resetReaderIndex();
                return;
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

//            System.out.println("✅ DECODED: cmd=" + cmd + ", size=" + size);

        } catch (Exception e) {
//            System.out.println("❌ DECODER: " + e.getMessage());
            // ⭐ ROLLBACK curR
            ((nro.models.network.netty.NettyMessageSendCollect)session.getSendCollect()).setCurR(savedCurR);
            in.resetReaderIndex();
            
      
        }
    }
}