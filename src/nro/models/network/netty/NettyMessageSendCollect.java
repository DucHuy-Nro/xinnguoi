package nro.models.network.netty;

import nro.models.interfaces.IMessageSendCollect;
import nro.models.interfaces.ISession;
import nro.models.network.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * MessageSendCollect cho Netty - COPY CHÍNH XÁC từ code gốc
 */
public class NettyMessageSendCollect implements IMessageSendCollect {
    
    private int curR = 0;
    private int curW = 0;
    
    public int getCurR() {
        return curR;
    }
    
    public void setCurR(int curR) {
        this.curR = curR;
    }
    
    @Override
    public Message readMessage(ISession session, DataInputStream dis) throws Exception {
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
            
            if (available < size) {
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
            curR = savedCurR;
            throw e;
        }
    }
    
    @Override
    public void doSendMessage(ISession session, DataOutputStream dos, Message msg) throws IOException {
        try {
            byte[] data = msg.getData();
            byte cmd = msg.command;
            
            // Write cmd
            if (session.sentKey()) {
                dos.writeByte(writeKey(session, cmd));
            } else {
                dos.writeByte(cmd);
            }
            
            // Write size
            if (data != null) {
                int size = data.length;
                
                // ⭐ QUAN TRỌNG: Special commands với 3-byte size!
                if (cmd == -32 || cmd == -66 || cmd == -74 || cmd == 11 || cmd == -67 || cmd == -87 || cmd == 66 || cmd == 12) {
                    // 3-byte size format
                    byte b2 = writeKey(session, (byte) size);
                    dos.writeByte(b2 - 128);
                    byte b3 = writeKey(session, (byte) (size >> 8));
                    dos.writeByte(b3 - 128);
                    byte b4 = writeKey(session, (byte) (size >> 16));
                    dos.writeByte(b4 - 128);
                } else if (session.sentKey()) {
                    // Normal 2-byte encrypted size
                    int byte1 = writeKey(session, (byte) (size >> 8));
                    dos.writeByte(byte1);
                    int byte2 = writeKey(session, (byte) (size & 0xFF));
                    dos.writeByte(byte2);
                } else {
                    // Plain 2-byte size
                    dos.writeShort(size);
                }
                
                // Write data bytes
                if (session.sentKey()) {
                    // ⭐ ENCRYPT từng byte!
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(session, data[i]);
                    }
                }
                dos.write(data);
            } else {
                dos.writeShort(0);
            }
            
            dos.flush();
            msg.cleanup();
            
        } catch (IOException ex) {
            // Ignore
        }
    }
    
    @Override
    public byte readKey(ISession session, byte b) {
        byte[] keys = session.getKey();
        byte result = (byte) ((keys[curR++] & 0xFF) ^ (b & 0xFF));
        if (curR >= keys.length) {
            curR %= keys.length;
        }
        return result;
    }
    
    @Override
    public byte writeKey(ISession session, byte b) {
        byte[] keys = session.getKey();
        byte result = (byte) ((keys[curW++] & 0xFF) ^ (b & 0xFF));
        if (curW >= keys.length) {
            curW %= keys.length;
        }
        return result;
    }
}