package nro.models.network.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import nro.models.utils.Logger;

public class NettyServer {
    
    private final int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    public NettyServer(int port) {
        this.port = port;
    }
    
    public void start() throws Exception {
        // Boss group: 1-2 threads để accept connections
        bossGroup = new NioEventLoopGroup(1);
        
        // Worker group: 4-8 threads xử lý I/O cho TẤT CẢ clients
        // Tự động scale theo số CPU cores
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new NettyServerInitializer());
            
            // Bind và start server
            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            
            Logger.success("✅ Netty Server started on port " + port);
            Logger.success("📊 Boss threads: 1, Worker threads: " + 
                          ((NioEventLoopGroup)workerGroup).executorCount());
            
            // Chờ server đóng
            serverChannel.closeFuture().sync();
            
        } finally {
            shutdown();
        }
    }
    
    public void shutdown() {
        Logger.warning("🔴 Shutting down Netty server...");
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        Logger.success("✅ Netty server stopped");
    }
}