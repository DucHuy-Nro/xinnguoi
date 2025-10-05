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
        
        byte[] originalData = msg.getData();
        System.out.println("📤 ENCODER: cmd=" + msg.command + ", dataLen=" + originalData.length + ", sentKey=" + (session != null ? session.sentKey() : "null"));
        
        if (session == null || session.getSendCollect() == null) {
            // Plain
            try {
                out.writeByte(msg.command);
                out.writeShort(originalData.length);
                out.writeBytes(originalData);
                System.out.println("📤 ENCODER: PLAIN mode");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            session.getSendCollect().doSendMessage(session, dos, msg);
            
            byte[] encoded = baos.toByteArray();
            out.writeBytes(encoded);
            
            System.out.println("📤 ENCODER: ENCRYPTED, totalSize=" + encoded.length + " (was " + originalData.length + ")");
            
            // Log first 10 bytes để debug
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < Math.min(10, encoded.length); i++) {
                hex.append(String.format("%02X ", encoded[i]));
            }
            System.out.println("   First bytes: " + hex.toString());
            
        } catch (Exception e) {
            System.out.println("❌ ENCODER exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}