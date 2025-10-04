package nro.models.network.netty;

import io.netty.channel.ChannelHandlerContext;
import nro.models.network.MySession;
import nro.models.network.Message;

public class NettySession extends MySession {
    
    private final ChannelHandlerContext ctx;
    
    public NettySession(ChannelHandlerContext ctx) {
        super(null); // Không cần Socket nữa!
        this.ctx = ctx;
        this.ipAddress = ctx.channel().remoteAddress().toString();
    }
    
    @Override
    public void sendMessage(Message msg) {
        if (ctx.channel().isActive()) {
            ctx.writeAndFlush(msg);
        }
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
}