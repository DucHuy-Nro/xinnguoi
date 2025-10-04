package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import nro.models.interfaces.ISessionAcceptHandler;
import nro.models.network.Message;
import nro.models.utils.Logger;

/**
 * Netty Channel Handler
 * 
 * Xử lý:
 * - Connection events (connect, disconnect)
 * - Message routing
 * - Timeout
 * - Errors
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    
    private static final AttributeKey<NettySession> SESSION_KEY = AttributeKey.valueOf("session");
    private final ISessionAcceptHandler acceptHandler;
    
    public NettyServerHandler(ISessionAcceptHandler acceptHandler) {
        this.acceptHandler = acceptHandler;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // New connection established
        String ip = getClientIP(ctx);
        
        try {
            // Create session
            NettySession session = new NettySession(ctx);
            ctx.channel().attr(SESSION_KEY).set(session);
            
            // Notify accept handler (giống code cũ)
            if (acceptHandler != null) {
                acceptHandler.sessionInit(session);
            }
            
            Logger.info("🟢 Client connected: " + ip + " (ID: " + session.getID() + ")");
            
        } catch (Exception e) {
            Logger.error("❌ Error initializing session for " + ip + ": " + e.getMessage());
            ctx.close();
        }
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // Receive message from client
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session != null && session.getMessageHandler() != null) {
            try {
                // Process message through QueueHandler (giống code cũ)
                if (session.getQueueHandler() != null) {
                    session.getQueueHandler().addMessage(msg);
                } else {
                    // Fallback: process directly
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
        // Connection closed
        NettySession session = ctx.channel().attr(SESSION_KEY).get();
        
        if (session != null) {
            String ip = session.getIP();
            Logger.info("🔴 Client disconnected: " + ip + " (ID: " + session.getID() + ")");
            
            // Notify accept handler
            if (acceptHandler != null) {
                acceptHandler.sessionDisconnect(session);
            }
            
            // Cleanup session
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
        // Handle exceptions
        String message = cause.getMessage();
        
        // Ignore common disconnection errors
        if (message != null && (
            message.contains("Connection reset") ||
            message.contains("Broken pipe") ||
            message.contains("forcibly closed")
        )) {
            // Normal disconnect, just close
            ctx.close();
            return;
        }
        
        // Log other errors
        Logger.error("❌ Channel error: " + cause.getMessage());
        cause.printStackTrace();
        
        ctx.close();
    }
    
    private String getClientIP(ChannelHandlerContext ctx) {
        String address = ctx.channel().remoteAddress().toString();
        // Format: /127.0.0.1:12345 → 127.0.0.1
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
