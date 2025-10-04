package nro.models.network.netty;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // Timeout handler: disconnect nếu idle quá 5 phút
        pipeline.addLast("idleState", new IdleStateHandler(300, 300, 0, TimeUnit.SECONDS));
        
        // Decoder: byte[] → Message
        pipeline.addLast("decoder", new MessageDecoder());
        
        // Encoder: Message → byte[]
        pipeline.addLast("encoder", new MessageEncoder());
        
        // Handler: Xử lý logic game
        pipeline.addLast("handler", new NettyServerHandler());
    }
}