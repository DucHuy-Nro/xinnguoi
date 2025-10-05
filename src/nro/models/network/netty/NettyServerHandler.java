package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
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

            // Init session
            if (acceptHandler != null) {
                acceptHandler.sessionInit(session);
            }

            // Gửi session key ngay
            try {
                System.out.println("📤 Sending session key...");
                session.sendKey();
                System.out.println("✅ Key sent!");
            } catch (Exception ex) {
                Logger.error("❌ Error sending key: " + ex.getMessage());
                ex.printStackTrace();
                ctx.close();
                return;
            }

            Logger.warning("🟢 Client connected & key sent: " + ip + " (ID: " + session.getID() + ")");

        } catch (Exception e) {
            Logger.error("❌ Error initializing: " + e.getMessage());
            e.printStackTrace();
            ctx.close();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        System.out.println("📨 HANDLER: Received message cmd=" + msg.command);

        NettySession session = ctx.channel().attr(SESSION_KEY).get();

        if (session == null) {
            System.out.println("❌ HANDLER: Session is null!");
            return;
        }

        if (session.getQueueHandler() == null) {
            System.out.println("❌ HANDLER: QueueHandler is null!");
            return;
        }

        try {
            session.getQueueHandler().addMessage(msg);
        } catch (Exception e) {
            System.out.println("❌ HANDLER: Error - " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        NettySession session = ctx.channel().attr(SESSION_KEY).get();

        if (session != null) {
            String ip = session.getIP();
            Logger.warning("🔴 Disconnected: " + ip + " (ID: " + session.getID() + ")");

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
            Logger.warning("⏱️ Timeout, closing");
            ctx.close();
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

        Logger.error("❌ Error: " + cause.getMessage());
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