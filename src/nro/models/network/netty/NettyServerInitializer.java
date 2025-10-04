package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import nro.models.interfaces.ISessionAcceptHandler;
import java.util.concurrent.TimeUnit;

/**
 * Netty Pipeline Initializer
 * 
 * Pipeline flow:
 * Client → [Timeout] → [Decoder] → [Handler] → [Encoder] → Client
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    
    private final ISessionAcceptHandler acceptHandler;
    
    // Constructor nhận acceptHandler
    public NettyServerInitializer(ISessionAcceptHandler acceptHandler) {
        this.acceptHandler = acceptHandler;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 1. Timeout handler: auto disconnect nếu idle 10 phút
        pipeline.addLast("idleState", new IdleStateHandler(
            600,  // Reader idle time (seconds)
            600,  // Writer idle time (seconds)
            0     // All idle time (0 = disabled)
        ));
        
        // 2. Decoder: ByteBuf → Message
        pipeline.addLast("decoder", new MessageDecoder());
        
        // 3. Encoder: Message → ByteBuf
        pipeline.addLast("encoder", new MessageEncoder());
        
        // 4. Handler: Xử lý logic game (truyền acceptHandler vào)
        pipeline.addLast("handler", new NettyServerHandler(acceptHandler));
    }
}