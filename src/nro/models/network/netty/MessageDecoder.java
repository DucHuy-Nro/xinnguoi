package nro.models.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.ByteToMessageDecoder;
import nro.models.network.Message;
import java.util.List;

/**
 * Decode ByteBuf → Message
 * Protocol format: [cmd:1byte][size:2bytes][data:size bytes]
 */
public class MessageDecoder extends ByteToMessageDecoder {
    
    private static final int MAX_MESSAGE_SIZE = 2 * 1024 * 1024; // 2MB
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Need at least 3 bytes: cmd(1) + size(2)
        if (in.readableBytes() < 3) {
            return;
        }
        
        in.markReaderIndex();
        
        byte cmd = in.readByte();
        int size = in.readUnsignedShort();
        
        // Validate message size
        if (size < 0 || size > MAX_MESSAGE_SIZE) {
            ctx.close();
            return;
        }
        
        // Check if full message available
        if (in.readableBytes() < size) {
            in.resetReaderIndex();
            return;
        }
        
        // Read message data
        byte[] data = new byte[size];
        in.readBytes(data);
        
        // Create Message object (dùng constructor với data)
        Message message = new Message(cmd, data);
        out.add(message);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}