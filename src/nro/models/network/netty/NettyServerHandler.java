package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;  // ← IMPORT NÀY!
import nro.models.interfaces.ISessionAcceptHandler;
import nro.models.network.Message;
import nro.models.utils.Logger;

@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    private final ISessionAcceptHandler acceptHandler;
    
    public NettyServerHandler(ISessionAcceptHandler acceptHandler) {
        this.acceptHandler = acceptHandler;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        String ip = getClientIP(ctx);
        
        try {
            NettySession session = new NettySession(ctx);
            ctx.channel().attr(SESSION_KEY).set(session);
            
            if (acceptHandler != null) {
                acceptHandler.sessionInit(session);
            }
            
            // Đổi Logger.info() thành Logger.warning() hoặc success()
            Logger.warning("🟢 Client connected: " + ip + " (ID: " + session.getID() + ")");
            
        } catch (Exception e) {
            Logger.error("❌ Error initializing session for " + ip + ": " + e.getMessage());
            ctx.close();
        }
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session != null && session.getMessageHandler() != null) {
            try {
                if (session.getQueueHandler() != null) {
                    session.getQueueHandler().addMessage(msg);
                } else {
                    session.getMessageHandler().onMessage(session, msg);
                    msg.cleanup();
                }
            } catch (Exception e) {
                Logger.error("❌ Error processing message: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session != null) {
            String ip = session.getIP();
            // Đổi Logger.info() thành Logger.warning()
            Logger.warning("🔴 Client disconnected: " + ip + " (ID: " + session.getID() + ")");
            
            if (acceptHandler != null) {
                acceptHandler.sessionDisconnect(session);
            }
            
            session.dispose();
        }
        
        ctx.channel().attr(SESSION_KEY).set(null);
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            
            if (event.state() == IdleState.READER_IDLE) {
                Logger.warning("⏱️ Client idle (no read), closing connection");
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                Logger.warning("⏱️ Client idle (no write), closing connection");
                ctx.close();
            }
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String message = cause.getMessage();
        
        if (message != null && (
            message.contains("Connection reset") ||
            message.contains("Broken pipe") ||
            message.contains("forcibly closed")
        )) {
            ctx.close();
            return;
        }
        
        Logger.error("❌ Channel error: " + cause.getMessage());
        cause.printStackTrace();
        
        ctx.close();
    }
    
    private String getClientIP(ChannelHandlerContext ctx) {
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