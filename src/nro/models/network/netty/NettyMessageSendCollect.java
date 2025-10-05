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
    
    // Getter/Setter cho Decoder rollback
    public int getCurR() {
        return curR;
    }
    
    public void setCurR(int curR) {
        this.curR = curR;
    }
    
    @Override
    public Message readMessage(ISession session, DataInputStream dis) throws Exception {
        // ⭐ BACKUP curR trước khi decode!
        int savedCurR = curR;
        
        try {
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
            
            int available = dis.available();
            
            // Check đủ bytes chưa
            if (available < size) {
                // ROLLBACK curR!
                curR = savedCurR;
                return null;
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
            
        } catch (Exception e) {
            // Rollback curR
            curR = savedCurR;
            throw e;
        }
    }
    
    @Override
    public void doSendMessage(ISession session, DataOutputStream dos, Message msg) throws IOException {
        byte[] data = msg.getData();
        byte cmd = msg.command;
        
        // Write cmd
        if (session.sentKey()) {
            dos.writeByte(writeKey(session, cmd));
        } else {
            dos.writeByte(cmd);
        }
        
        // Write size
        int size = data != null ? data.length : 0;
        
        // Special commands với 3-byte size
        if (cmd == -32 || cmd == -66 || cmd == -74 || cmd == 11 || cmd == -67 || cmd == -87 || cmd == 66 || cmd == 12) {
            if (session.sentKey()) {
                dos.writeByte(writeKey(session, (byte) size) - 128);
                dos.writeByte(writeKey(session, (byte) (size >> 8)) - 128);
                dos.writeByte(writeKey(session, (byte) (size >> 16)) - 128);
            } else {
                dos.writeByte((byte) size);
                dos.writeByte((byte) (size >> 8));
                dos.writeByte((byte) (size >> 16));
            }
        } else {
            // Normal 2-byte size
            if (session.sentKey()) {
                dos.writeByte(writeKey(session, (byte) (size >> 8)));
                dos.writeByte(writeKey(session, (byte) (size & 0xFF)));
            } else {
                dos.writeShort(size);
            }
        }
        
        // Write data
        if (data != null && size > 0) {
            if (session.sentKey()) {
                for (int i = 0; i < data.length; i++) {
                    dos.writeByte(writeKey(session, data[i]));
                }
            } else {
                dos.write(data);
            }
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