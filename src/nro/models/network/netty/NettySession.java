package nro.models.network.netty;

import io.netty.channel.ChannelHandlerContext;
import nro.models.network.Message;
import nro.models.network.MySession;

/**
 * NettySession extends MySession
 * Tương thích 100% với code cũ!
 * 
 * Chỉ override các methods liên quan đến network I/O
 */
public class NettySession extends MySession {
    
    private final ChannelHandlerContext ctx;
    private final NettyMessageSendCollect nettySendCollect = new NettyMessageSendCollect();
    public NettySession(ChannelHandlerContext ctx) {
        super(null); // Không cần Socket
        this.ctx = ctx;
        this.ipAddress = extractIP(ctx);
        
   
    }
    
    // Override để trả về NettyMessageSendCollect
    @Override
    public nro.models.interfaces.IMessageSendCollect getSendCollect() {
       return this.nettySendCollect;
    }
    @Override
    public void sendKey() throws Exception {
        sendSessionKey();
    }
    @Override
    public void sendMessage(Message msg) {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(msg);
        }
    }
    
    @Override
    public void doSendMessage(Message msg) throws Exception {
        sendMessage(msg);
    }
    
    @Override
    public void disconnect() {
         System.out.println("⚠️ NettySession.disconnect() called!");
        new Exception("Disconnect called from:").printStackTrace();

        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
        }
    }
    
    @Override
    public boolean isConnected() {
              boolean active = ctx != null && ctx.channel().isActive();
        if (!active) {
            System.out.println("⚠️ NettySession.isConnected() = FALSE! ctx=" + (ctx != null) + ", active=" + (ctx != null ? ctx.channel().isActive() : "null"));
        }
        return active;
    }
    
    @Override
    public String getIP() {
        return ipAddress;
    }
    
    private String extractIP(ChannelHandlerContext ctx) {
        String address = ctx.channel().remoteAddress().toString();
        if (address.startsWith("/")) {
            address = address.substring(1);
        }
        int colonIndex = address.lastIndexOf(':');
        if (colonIndex > 0) {
            address = address.substring(0, colonIndex);
        }
        return address;
    }
}