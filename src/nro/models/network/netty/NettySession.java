package nro.models.network.netty;

import io.netty.channel.ChannelHandlerContext;
import nro.models.network.Message;
import nro.models.player.Player;

/**
 * NettySession extends MySession
 * Tương thích 100% với code cũ!
 */
public class NettySession extends nro.models.network.MySession {
    
    private final ChannelHandlerContext ctx;
    
    public NettySession(ChannelHandlerContext ctx) {
        super(null); // Không cần Socket
        this.ctx = ctx;
        this.ipAddress = extractIP(ctx);
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
        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
        }
    }
    
    @Override
    public boolean isConnected() {
        return ctx != null && ctx.channel().isActive();
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