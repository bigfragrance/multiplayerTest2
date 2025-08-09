package server;

import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import modules.entity.player.PlayerData;
import modules.entity.player.PlayerEntity;
import modules.entity.player.ServerPlayerEntity;
import modules.network.ServerNetworkHandler;
import modules.network.packet.s2c.TanksDataS2CPacket;
import modules.weapon.GunList;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.Socket; 
import java.util.concurrent.BlockingQueue; 
import java.util.concurrent.LinkedBlockingQueue;

import static engine.modules.EngineMain.cs;

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
        spawnPlayer();
    }
    public void spawnPlayer(){
        this.player=new ServerPlayerEntity(EntityUtils.getRandomSpawnPosition(cs.getTeam()));
        this.player.isAlive=true;
        this.player.team=cs.getTeam();
        cs.addEntity(player);
        this.serverNetworkHandler=new ServerNetworkHandler(this);
        this.serverNetworkHandler.sendPlayerSpawn(player);
        this.serverNetworkHandler.send(new TanksDataS2CPacket(GunList.data).toJSON());
        this.player.networkHandler=this.serverNetworkHandler;
        cs.multiClientHandler.addClient(this);
    }
    @Override 
    public void run() {
        try (BufferedReader reader = new BufferedReader(
             new InputStreamReader(clientSocket.getInputStream())))  {
             
            writer = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(),  "UTF-8"), false);
 
            // Send initial snapshot 
            /*JSONObject initMsg = new JSONObject();
            initMsg.put(PacketUtil.getShortVariableName("type"),  "full_snapshot");
            initMsg.put("data",  canvas.getFullSnapshot()); 
            writer.println(initMsg.toString()); */
 
            // Start broadcast receiver 
            processThread= new Thread(this::processBroadcasts);
            processThread.start();
 
            // Handle incoming messages 
            String inputLine;
            lastReceive=System.currentTimeMillis();
            while ((inputLine = reader.readLine())  != null&&System.currentTimeMillis()-lastReceive<6000&&!interrupted&&!Thread.currentThread().isInterrupted()) {
                if(inputLine.equals("handshake")){
                    handshaked=true;
                    //spawnPlayer();
                    lastReceive=System.currentTimeMillis();
                    continue;
                }else if(!handshaked){
                    disconnect();
                    return;
                }
                JSONObject msg = new JSONObject(inputLine);
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
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        interrupted=true;
    }

    public void send(JSONObject o){
        broadcastQueue.offer(o.toString());
    }
    private int sent=0;
    private void processBroadcasts() {
        try {
            while (!Thread.interrupted())  {
                String msg = broadcastQueue.take(); 
                writer.println(msg);
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