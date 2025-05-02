package server;

import engine.math.Vec2d;
import modules.entity.PlayerEntity;
import modules.network.ClientNetworkHandler;
import modules.network.ServerNetworkHandler;
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
    private final CanvasManager canvas;
    private PrintWriter writer;

    public ServerNetworkHandler serverNetworkHandler;
    public PlayerEntity player;
    private long lastReceive=0;
    public ClientHandler(Socket socket, CanvasManager manager) {
        this.clientSocket  = socket;
        this.canvas  = manager;
        this.player=new PlayerEntity(new Vec2d(0,0));
        this.player.isAlive=true;
        cs.addEntity(player);
        this.serverNetworkHandler=new ServerNetworkHandler(this);
        this.serverNetworkHandler.sendPlayerSpawn(player);
        cs.multiClientHandler.addClient(this);
        lastReceive=System.currentTimeMillis();
    }
 
    @Override 
    public void run() {
        try (BufferedReader reader = new BufferedReader(
             new InputStreamReader(clientSocket.getInputStream())))  {
             
            writer = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(),  "UTF-8"), true);
 
            // Send initial snapshot 
            /*JSONObject initMsg = new JSONObject();
            initMsg.put("type",  "full_snapshot");
            initMsg.put("data",  canvas.getFullSnapshot()); 
            writer.println(initMsg.toString()); */
 
            // Start broadcast receiver 
            new Thread(this::processBroadcasts).start();
 
            // Handle incoming messages 
            String inputLine;
            lastReceive=System.currentTimeMillis();
            while ((inputLine = reader.readLine())  != null&&System.currentTimeMillis()-lastReceive<6000) {
                JSONObject msg = new JSONObject(inputLine);
                lastReceive=System.currentTimeMillis();
                serverNetworkHandler.apply(msg);
            }
            disconnect();
        } catch (Exception e) {
            disconnect();
            System.err.println("Client  error: " + e.getMessage()); 
        }
    }
    private void disconnect() {
        try {
            cs.removeEntity(player.id);
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
 
    private void handlePixelUpdate(JSONObject update) {
        int x = update.getInt("x"); 
        int y = update.getInt("y"); 
        Color color = new Color(update.getInt("color"),  true);
        
        if (canvas.updatePixel(x,  y, color)) {
            JSONObject broadcast = new JSONObject(update.toString()); 
            broadcast.put("type",  "pixel_update");
            //anti tiu
            broadcastQueue.offer(broadcast.toString());

        }
    }
    public void send(JSONObject o){
        broadcastQueue.offer(o.toString());
    }
 
    private void processBroadcasts() {
        try {
            while (!Thread.interrupted())  {
                String msg = broadcastQueue.take(); 
                writer.println(msg); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
    }
}