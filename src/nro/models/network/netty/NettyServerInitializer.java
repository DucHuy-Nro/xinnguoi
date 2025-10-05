package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import nro.models.interfaces.ISessionAcceptHandler;
import java.util.concurrent.TimeUnit;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    
    private final ISessionAcceptHandler acceptHandler;
    
    public NettyServerInitializer(ISessionAcceptHandler acceptHandler) {
        this.acceptHandler = acceptHandler;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 1. Timeout handler
        pipeline.addLast("idleState", new IdleStateHandler(600, 600, 0));
        
        // 2. Decoder: ByteBuf → Message (TÊN MỚI!)
        pipeline.addLast("decoder", new NettyMessageDecoder());
        
        // 3. Encoder: Message → ByteBuf (TÊN MỚI!)
        pipeline.addLast("encoder", new NettyMessageEncoder());
        
        // 4. Handler
        pipeline.addLast("handler", new NettyServerHandler(acceptHandler));
    }
}