package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.AttributeKey;
import nro.models.network.Message;
import java.io.*;

/**
 * Encoder với MessageSendCollect (xử lý encryption)
 */
public class NettyMessageEncoder extends MessageToByteEncoder<Message> {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        System.out.println("📤 V3 ENCODER: Encoding cmd=" + msg.command);
        
        if (session == null || session.getSendCollect() == null) {
            // Plain write (chưa có sendCollect)
            try {
                byte[] data = msg.getData();
                out.writeByte(msg.command);
                out.writeShort(data.length);
                out.writeBytes(data);
                System.out.println("📤 V3 ENCODER: Plain mode");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        try {
            // Dùng MessageSendCollect.doSendMessage() (có encryption!)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            session.getSendCollect().doSendMessage(session, dos, msg);
            
            byte[] encoded = baos.toByteArray();
            out.writeBytes(encoded);
            
            System.out.println("📤 V3 ENCODER: Encrypted, size=" + encoded.length);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}