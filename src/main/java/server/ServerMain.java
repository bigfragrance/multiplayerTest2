package server;

import modules.entity.player.PlayerData;
import modules.entity.player.ServerPlayerEntity;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
 
public class ServerMain {
    private static final int PORT = 8088;
    private static final int MAX_THREADS = 50;
    private static final CanvasManager canvas = new CanvasManager();
    private static Queue<Double> connectionTimes = new java.util.LinkedList<>();
    public static ConcurrentHashMap<Integer,Boolean> connectedPlayers = new ConcurrentHashMap<>();
    private static long lastConnectionTime = 0;
    private static double minAvgDelay=100;
    public static ConcurrentHashMap<Integer, PlayerData> connectedPlayersEntity = new ConcurrentHashMap<>();
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS); 
        lastConnectionTime = System.currentTimeMillis();
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server  listening on port " + PORT);
            
            while (true) {
                Socket client = server.accept();
                /*if(connectedPlayers.getOrDefault(client.getInetAddress().hashCode(),false)){
                    client.close();
                    continue;
                }*/
                /*int count=0;
                double delay=0;
                for(double d:connectionTimes){
                    count++;
                    delay+=d;
                }
                if(count>0){
                    double avg=delay/count;
                    if(avg<minAvgDelay){
                        client.close();
                        continue;
                    }
                }*/

                pool.execute(new  ClientHandler(client));
                connectedPlayers.put(client.getInetAddress().hashCode(),true);
                connectionTimes.offer((double) (System.currentTimeMillis()-lastConnectionTime));
                if(connectionTimes.size()>10){
                    connectionTimes.poll();
                }
                lastConnectionTime = System.currentTimeMillis();
            }
        }
    }
}