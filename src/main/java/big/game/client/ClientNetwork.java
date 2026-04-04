package big.game.client;

import big.game.network.ClientNetworkHandler;
import big.game.network.JSONNBTConverter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import net.querz.nbt.tag.Tag;
import org.json.JSONObject;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ClientNetwork {

    private static final int RECONNECT_DELAY = 5000;
    private final ClientNetworkHandler networkHandler;
    private Channel channel;
    private EventLoopGroup group;
    private volatile boolean running = true;
    private volatile boolean connected = false;
    private String serverAddress;
    private int port = 8088;
    private final Queue<String> toSend = new LinkedList<>();

    public ClientNetwork(ClientNetworkHandler handler) {
        this.networkHandler = handler;
    }

    public void connect(String address, int port) {
        this.serverAddress = address;
        this.port = port;
        startConnection();
    }

    private void startConnection() {
        new Thread(() -> {
            group = new NioEventLoopGroup(1);
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                 
                                p.addLast(new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 0, 4, 0, 4));
                                p.addLast(new NettyClientHandler());
                            }
                        });

                ChannelFuture f = b.connect(serverAddress, port).sync();
                this.channel = f.channel();
                this.connected = true;

                 
                send(new JSONObject().put("type", "handshake"));

                f.channel().closeFuture().sync();
            } catch (Exception e) {
                if (running) handleDisconnect(e);
            } finally {
                group.shutdownGracefully();
            }
        }).start();
    }

    private void handleDisconnect(Exception e) {
        connected = false;
        e.printStackTrace();
        if (!running) return;
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, "Reconnecting to big.server..."));
        try {
            TimeUnit.MILLISECONDS.sleep(RECONNECT_DELAY);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        startConnection();
    }

    public void send(JSONObject json) {
        if (!connected || channel == null || !channel.isActive()) {
            synchronized (toSend) {
                toSend.add(json.toString());
            }
            return;
        }

        try {
             
            synchronized (toSend) {
                while (!toSend.isEmpty()) {
                    String cached = toSend.poll();
                    sendJsonAsCompoundTag(new JSONObject(cached));
                }
            }
             
            sendJsonAsCompoundTag(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendJsonAsCompoundTag(JSONObject obj) throws Exception {
        CompoundTag compoundTag = JSONNBTConverter.toCompound(obj);
        NamedTag namedTag = new NamedTag("root", compoundTag);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos, true);
             NBTOutputStream nbtOut = new NBTOutputStream(gzipOut)) {
            nbtOut.writeTag(namedTag, Tag.DEFAULT_MAX_DEPTH);
            nbtOut.flush();
        }

        byte[] bytes = baos.toByteArray();
        ByteBuf out = Unpooled.buffer(4 + bytes.length);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        channel.writeAndFlush(out);
    }

    public void disconnect() {
        running = false;
        connected = false;
        try {
            if (channel != null) channel.close();
            if (group != null) group.shutdownGracefully();
        } catch (Exception e) {
            System.err.println("Disconnect error: " + e.getMessage());
        }
    }


    private class NettyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
            int len = buf.readableBytes();
            if (len <= 0) return;
            byte[] data = new byte[len];
            buf.readBytes(data);

            CompoundTag receivedTag;
            try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data));
                 NBTInputStream nbtIn = new NBTInputStream(gzipIn)) {
                NamedTag namedTag = nbtIn.readTag(len);
                receivedTag = (CompoundTag) namedTag.getTag();
            }

            JSONObject msg = JSONNBTConverter.toJSON(receivedTag);
            try {
                networkHandler.apply(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (running) handleDisconnect(new IOException("Server closed connection"));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
