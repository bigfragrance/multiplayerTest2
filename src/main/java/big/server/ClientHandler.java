package big.server;

import big.engine.util.EntityUtils;
import big.game.entity.player.PlayerData;
import big.game.entity.player.ServerPlayerEntity;
import big.game.network.JSONNBTConverter;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.AssetsS2CPacket;
import big.game.network.packet.s2c.MessageS2CPacket;
import big.game.network.packet.s2c.ServerDataS2CPacket;
import big.game.weapon.GunList;
import net.querz.nbt.io.*;
import net.querz.nbt.tag.CompoundTag;

import net.querz.nbt.tag.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static big.engine.modules.EngineMain.cs;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private volatile boolean interrupted = false;

    private final BlockingQueue<JSONObject> broadcastQueue = new LinkedBlockingQueue<>();
    public ServerNetworkHandler serverNetworkHandler;
    public ServerPlayerEntity player;
    private volatile long lastReceive = 0;
    private long connectionStartTime;
    private boolean handshaked = false;
    private Thread sendThread = null;
    public boolean dataSent=false;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        lastReceive = System.currentTimeMillis();
        connectionStartTime = System.currentTimeMillis();
        System.out.println("Client " + clientSocket.getInetAddress() + " connected");
        //spawnPlayer();
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

    @Override
    public void run() {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {


            sendThread = new Thread(() -> processBroadcasts(dos));
            sendThread.start();

            while (!interrupted) {

                int len;
                try {
                    len = dis.readInt();
                } catch (EOFException e) {
                    break;
                }

                if (len <= 0) continue;

                byte[] data = new byte[len];
                dis.readFully(data);


                CompoundTag receivedTag;
                try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data))) {
                    NBTInput nbtIn = new NBTInputStream(gzipIn);
                    NamedTag namedTag = nbtIn.readTag(len);
                    receivedTag = (CompoundTag) namedTag.getTag();
                }


                JSONObject msg = convertCompoundTagToJSONObject(receivedTag);
                if (!handshaked && msg.optString("type").equals("handshake")) {
                    handshaked = true;
                    spawnPlayer();
                    lastReceive = System.currentTimeMillis();
                    continue;
                }
                if (!handshaked) {
                    disconnect();
                    break;
                }
                try {
                    serverNetworkHandler.apply(msg);
                }catch (JSONException e){
                    break;
                }catch (Exception e){
                    disconnect();
                    break;
                }

                lastReceive = System.currentTimeMillis();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void processBroadcasts(DataOutputStream dos) {
        try {
            long lastSend=System.currentTimeMillis();
            int sent=0;
            JSONArray packetArray=new JSONArray();
            while (!Thread.interrupted() && !interrupted) {

                JSONObject obj = broadcastQueue.take();
                //System.out.println(broadcastQueue.size());

                JSONObject packet=obj;//new ArrayPacket(packetArray).toJSON();
                CompoundTag tag=convertJSONObjectToCompoundTag(packet);
                packetArray=new JSONArray();
                //lastSend=System.currentTimeMillis();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos, true)) {
                    NBTOutput nbtOut = new NBTOutputStream(gzipOut);
                    NamedTag named = new NamedTag("root", tag);
                    nbtOut.writeTag(named, Tag.DEFAULT_MAX_DEPTH);
                    nbtOut.flush();
                }

                byte[] bytes = baos.toByteArray();


                dos.writeInt(bytes.length);
                dos.write(bytes);
                sent++;
                if(sent>50||System.currentTimeMillis()-lastSend>5) {
                    dos.flush();
                    sent=0;
                    lastSend=System.currentTimeMillis();
                }

                /*if(packetArray.length()<50&&System.currentTimeMillis()-lastSend<1) {
                    packetArray.put(obj);
                }else {

                }*/
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
    }


    public void send(JSONObject obj) {
        broadcastQueue.offer(obj);
    }
    public void sendServerData(){
        send(new ServerDataS2CPacket(GunList.data,GunList.presetData,cs.borderBox).toJSON());
        send(new AssetsS2CPacket(AssetsS2CPacket.HASH,AssetsS2CPacket.assetsHash));
    }
    public void sendAssetsData(){
        for(AssetsS2CPacket packet:AssetsS2CPacket.createdData){
            send(packet);
        }
    }

    public void send(Packet<?> packet) {
        send(packet.toJSON());
    }

    public void disconnect() {
        interrupted = true;
        try {
            if (handshaked && player != null) {
                ServerMain.connectedPlayersEntity.put(player.name.hashCode(), new PlayerData(player));
                cs.removeEntity(player.id);
            }
            clientSocket.close();
            cs.multiClientHandler.removeClient(this);
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
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
