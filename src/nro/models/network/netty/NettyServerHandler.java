package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import nro.models.network.Message;
import nro.models.network.MySession;
import nro.models.server.Controller;
import nro.models.utils.Logger;

@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Khi có connection mới
        String ip = ctx.channel().remoteAddress().toString();
        Logger.info("🟢 New connection from: " + ip);
        
        // Tạo session mới
        NettySession session = new NettySession(ctx);
        ctx.channel().attr(AttributeKey.valueOf("session")).set(session);
        
        // TODO: Anti-DDoS check
        // TODO: SessionManager.add(session)
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // Nhận message từ client
        NettySession session = (NettySession) ctx.channel().attr(AttributeKey.valueOf("session")).get();
        
        if (session != null) {
            // Xử lý message qua Controller (giữ nguyên logic cũ!)
            Controller.gI().onMessage(session, msg);
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // Khi disconnect
        NettySession session = (NettySession) ctx.channel().attr(AttributeKey.valueOf("session")).get();
        if (session != null) {
            Logger.info("🔴 Client disconnected: " + session.getIP());
            // TODO: SessionManager.remove(session)
            // TODO: Client.gI().kickSession(session)
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                Logger.warning("⏱️ Client timeout, closing connection");
                ctx.close();
            }
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error("❌ Error: " + cause.getMessage());
        ctx.close();
    }
}