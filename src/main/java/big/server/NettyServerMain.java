package big.server;

import big.engine.util.AvgCounter;
import big.game.entity.player.PlayerData;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServerMain {
    private static final int PORT = 8088;
    private static final int MAX_THREADS = 50;  
    private static AvgCounter connectSpeed = new AvgCounter();
    public static ConcurrentHashMap<Integer,Boolean> connectedPlayers = new ConcurrentHashMap<>();
    private static long lastConnectionTime = 0;
    public static ConcurrentHashMap<Integer, PlayerData> connectedPlayersEntity = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        lastConnectionTime = System.currentTimeMillis();
         
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();  
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                      
                     p.addLast(new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 0, 4, 0, 4));
                      
                     p.addLast(new NettyClientHandler());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(new InetSocketAddress(PORT)).sync();
            System.out.println("Server listening on port " + PORT);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

     
    public static void onNewConnection(java.net.InetSocketAddress remoteAddress) {
        connectedPlayers.put(remoteAddress.getAddress().hashCode(), true);
        connectSpeed.add((double) (System.currentTimeMillis() - lastConnectionTime));
        lastConnectionTime = System.currentTimeMillis();
    }
}
