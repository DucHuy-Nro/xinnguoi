package nro.models.network.netty;

import nro.models.interfaces.IMessageSendCollect;
import nro.models.interfaces.ISession;
import nro.models.network.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * MessageSendCollect cho Netty với curR/curW state
 */
public class NettyMessageSendCollect implements IMessageSendCollect {
    
    private int curR = 0;
    private int curW = 0;
    
    @Override
    public Message readMessage(ISession session, DataInputStream dis) throws Exception {
        byte cmd = dis.readByte();
        
        if (session.sentKey()) {
            cmd = readKey(session, cmd);
        }
        
        int size;
        if (session.sentKey()) {
            byte b1 = dis.readByte();
            byte b2 = dis.readByte();
            size = ((readKey(session, b1) & 0xFF) << 8) | (readKey(session, b2) & 0xFF);
        } else {
            size = dis.readUnsignedShort();
        }
        
        byte[] data = new byte[size];
        if (size > 0) {
            dis.readFully(data);
            if (session.sentKey()) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = readKey(session, data[i]);
                }
            }
        }
        
        return new Message(cmd, data);
    }
    
    @Override
    public void doSendMessage(ISession session, DataOutputStream dos, Message msg) throws IOException {
        byte[] data = msg.getData();
        
        if (session.sentKey()) {
            dos.writeByte(writeKey(session, msg.command));
            
            int size = data.length;
            byte b1 = (byte) (size >> 8);
            byte b2 = (byte) (size & 0xFF);
            dos.writeByte(writeKey(session, b1));
            dos.writeByte(writeKey(session, b2));
            
            for (byte b : data) {
                dos.writeByte(writeKey(session, b));
            }
        } else {
            dos.writeByte(msg.command);
            dos.writeShort(data.length);
            dos.write(data);
        }
        
        dos.flush();
    }
    
    @Override
    public byte readKey(ISession session, byte b) {
        byte[] keys = session.getKey();
        byte result = (byte) (keys[curR++] ^ b);
        if (curR >= keys.length) {
            curR = 0;
        }
        return result;
    }
    
    @Override
    public byte writeKey(ISession session, byte b) {
        byte[] keys = session.getKey();
        byte result = (byte) (keys[curW++] ^ b);
        if (curW >= keys.length) {
            curW = 0;
        }
        return result;
    }
}