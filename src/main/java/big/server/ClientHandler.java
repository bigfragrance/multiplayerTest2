package big.server;

import big.engine.math.util.EntityUtils;
import big.engine.math.util.PercentEncoder;
import big.modules.entity.player.PlayerData;
import big.modules.entity.player.PlayerEntity;
import big.modules.entity.player.ServerPlayerEntity;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.network.packet.s2c.MessageS2CPacket;
import big.modules.network.packet.s2c.TanksDataS2CPacket;
import big.modules.weapon.GunList;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static big.engine.modules.EngineMain.cs;

public class ClientHandler implements Runnable {
    private final BlockingQueue<String> broadcastQueue = new LinkedBlockingQueue<>();
    private final Socket clientSocket;
    private PrintWriter writer;

    public ServerNetworkHandler serverNetworkHandler;
    public ServerPlayerEntity player;
    private volatile long lastReceive=0;
    private long connectionStartTime;
    private boolean interrupted=false;
    private Thread processThread=null;
    private boolean handshaked=false;
    public ClientHandler(Socket socket) {
        this.clientSocket  = socket;
        lastReceive=System.currentTimeMillis();
        connectionStartTime=System.currentTimeMillis();

    }
    public void spawnPlayer(){
        this.player=new ServerPlayerEntity(EntityUtils.getRandomSpawnPosition(cs.getTeam()));
        this.player.isAlive=true;
        this.player.team=cs.getTeam();
        cs.addEntity(player);
        this.serverNetworkHandler=new ServerNetworkHandler(this);
        this.serverNetworkHandler.sendPlayerSpawn(player);
        this.serverNetworkHandler.send(new TanksDataS2CPacket(GunList.data,GunList.presetData).toJSON());
        this.player.networkHandler=this.serverNetworkHandler;
        cs.multiClientHandler.addClient(this);
    }
    @Override 
    public void run() {
        try (BufferedReader reader = new BufferedReader(
             new InputStreamReader(clientSocket.getInputStream())))  {
             
            writer = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), false);
 

            processThread= new Thread(this::processBroadcasts);
            processThread.start();
 

            String inputLine;
            lastReceive=System.currentTimeMillis();
            while ((inputLine = reader.readLine())  != null&&System.currentTimeMillis()-lastReceive<6000&&!interrupted&&!Thread.currentThread().isInterrupted()) {
                if(inputLine.equals("handshake")){
                    handshaked=true;
                    //spawnPlayer();
                    lastReceive=System.currentTimeMillis();
                    spawnPlayer();
                    continue;
                }else if(!handshaked){
                    disconnect();
                    return;
                }
                JSONObject msg = new JSONObject(PercentEncoder.decodeChinese(inputLine));
                serverNetworkHandler.apply(msg);
                lastReceive=System.currentTimeMillis();
            }
            disconnect();
            Thread.currentThread().interrupt();
            processThread.interrupt();
        } catch (Exception e) {
            disconnect();
            System.err.println("Client  error: " );
            e.printStackTrace();
            Thread.currentThread().interrupt();
            processThread.interrupt();
        }
    }
    public void checkConnecting(){
        if(System.currentTimeMillis()-lastReceive>1000||(System.currentTimeMillis()-connectionStartTime>1000&&player.name.equals(PlayerEntity.defName))){
            disconnect();
        }
    }
    private void disconnect() {
        try {
            if(handshaked){
                ServerMain.connectedPlayersEntity.put(player.name.hashCode(),new PlayerData(player));
            }
            cs.removeEntity(player.id);
            clientSocket.close();
            ServerMain.connectedPlayers.remove(clientSocket.getInetAddress().hashCode());
            cs.multiClientHandler.removeClient(this);
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        interrupted=true;
    }

    public void send(JSONObject o){
        broadcastQueue.offer(o.toString());
    }
    public void send(Packet<?> packet){
        send(packet.toJSON());
    }
    private int sent=0;
    private void processBroadcasts() {
        try {
            while (!Thread.interrupted())  {
                String msg = broadcastQueue.take(); 
                writer.println(PercentEncoder.encodeChinese(msg));
                sent++;
                if(broadcastQueue.isEmpty()||sent>50){
                    writer.flush();
                    sent=0;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
    }
}