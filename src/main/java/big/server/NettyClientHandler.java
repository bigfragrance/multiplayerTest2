package big.server;

import big.engine.util.EntityUtils;
import big.engine.util.PacketUtil;
import big.game.entity.player.PlayerData;
import big.game.entity.player.ServerPlayerEntity;
import big.game.network.JSONNBTConverter;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.AssetsS2CPacket;
import big.game.network.packet.s2c.MessageS2CPacket;
import big.game.network.packet.s2c.ServerDataS2CPacket;
import big.game.weapon.GunList;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.json.JSONException;
import org.json.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static big.engine.modules.EngineMain.cs;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private Channel channel;
    private volatile boolean interrupted = false;

    private final BlockingQueue<JSONObject> broadcastQueue = new LinkedBlockingQueue<>();
    public ServerNetworkHandler serverNetworkHandler;
    public ServerPlayerEntity player;
    private long lastReceive = 0;
    private boolean handshaked = false;
    private Thread sendThread = null;
    public boolean dataSent = false;
    public boolean isFirst=true;

    public NettyClientHandler() {
        lastReceive = System.currentTimeMillis();
    }

     
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
        System.out.println("Client " + remote.getAddress() + " connected");
         
        NettyServerMain.onNewConnection(remote);
         
        startSendThread();
    }

     
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (interrupted) return;
        ByteBuf buf = (ByteBuf) msg;
        try {
            int len = buf.readableBytes();
            if (len <= 0) return;
            byte[] data = new byte[len];
            buf.readBytes(data);

            CompoundTag receivedTag;
            try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data))) {
                try (NBTInputStream nbtIn = new NBTInputStream(gzipIn)) {
                    NamedTag namedTag = nbtIn.readTag(len);
                    receivedTag = (CompoundTag) namedTag.getTag();
                }
            }

            JSONObject json = convertCompoundTagToJSONObject(receivedTag);
            if(isFirst){
                isFirst=false;
                return;
            }
            if (!handshaked && PacketUtil.get(json,"type") instanceof String && PacketUtil.getString(json,"type").equals("handshake")) {
                handshaked = true;
                spawnPlayer();
                lastReceive = System.currentTimeMillis();
                System.out.println("Handshake received");
                return;
            }
            if (!handshaked) {
                disconnect();
                return;
            }

            try {
                serverNetworkHandler.apply(json);
            } catch (JSONException e) {
                 
                disconnect();
            } catch (Exception e) {
                disconnect();
            }

            lastReceive = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        } finally {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
         
        cause.printStackTrace();
        disconnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
         
        disconnect();
    }

    private void startSendThread() {
        sendThread = new Thread(() -> processBroadcasts());
        sendThread.start();
    }

    private void processBroadcasts() {
        try {
            long lastSend = System.currentTimeMillis();
            int sent = 0;
            while (!Thread.interrupted() && !interrupted && channel != null && channel.isActive()) {
                JSONObject obj = broadcastQueue.take();
                CompoundTag tag = convertJSONObjectToCompoundTag(obj);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos, true)) {
                    try (NBTOutputStream nbtOut = new NBTOutputStream(gzipOut)) {
                        NamedTag named = new NamedTag("root", tag);
                        nbtOut.writeTag(named, Tag.DEFAULT_MAX_DEPTH);
                        nbtOut.flush();
                    }
                }

                byte[] bytes = baos.toByteArray();
                 
                ByteBuf out = Unpooled.buffer(4 + bytes.length);
                out.writeInt(bytes.length);
                out.writeBytes(bytes);
                ChannelFuture f = channel.writeAndFlush(out);
                 
                sent++;
                if (sent > 50 || System.currentTimeMillis() - lastSend > 5) {
                     
                    sent = 0;
                    lastSend = System.currentTimeMillis();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
             
            e.printStackTrace();
        }
    }

    public void send(JSONObject obj) {
        broadcastQueue.offer(obj);
    }

    public void sendServerData() {
        send(new ServerDataS2CPacket(GunList.data, GunList.presetData, cs.borderBox).toJSON());
        send(new AssetsS2CPacket(AssetsS2CPacket.HASH, AssetsS2CPacket.assetsHash).toJSON());
    }

    public void sendAssetsData() {
        for (AssetsS2CPacket packet : AssetsS2CPacket.createdData) {
            send(packet.toJSON());
        }
    }

    public void send(Packet<?> packet) {
        send(packet.toJSON());
    }

    public void spawnPlayer() {
        this.player = new ServerPlayerEntity(EntityUtils.getRandomSpawnPosition(cs.getTeam()));
        this.player.isAlive = true;
        this.player.team = cs.getTeam();
        cs.addEntity(player);
        this.serverNetworkHandler = new ServerNetworkHandler(this);
        cs.multiClientHandler.addClient(this);
        this.serverNetworkHandler.sendPlayerSpawn(player);
        this.sendServerData();
        player.networkHandler = serverNetworkHandler;
        MessageS2CPacket.sendHistory(this);
    }

    public void disconnect() {
        if (interrupted) return;
        interrupted = true;
        try {
            if (handshaked && player != null) {
                NettyServerMain.connectedPlayersEntity.put(player.name.hashCode(), new PlayerData(player));
                cs.removeEntity(player.id);
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            cs.multiClientHandler.removeClient(this);
        } catch (Exception e) {
            System.err.println("Error closing client channel: " + e.getMessage());
        }
        if (sendThread != null) sendThread.interrupt();
    }

    private CompoundTag convertJSONObjectToCompoundTag(JSONObject obj) {
        return JSONNBTConverter.toCompound(obj);
    }

    private JSONObject convertCompoundTagToJSONObject(CompoundTag tag) {
        return JSONNBTConverter.toJSON(tag);
    }

}
