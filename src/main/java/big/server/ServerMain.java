package big.server;

import big.engine.util.AvgCounter;
import big.game.entity.player.PlayerData;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
 
public class ServerMain {
    private static final int PORT = 8088;
    private static final int MAX_THREADS = 50;
    private static final CanvasManager canvas = new CanvasManager();
    private static AvgCounter connectSpeed = new AvgCounter();
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
                /*if(connectSpeed.getAvg()<minAvgDelay){
                    client.close();
                    continue;
                }*/

                pool.execute(new  ClientHandler(client));
                connectedPlayers.put(client.getInetAddress().hashCode(),true);
                connectSpeed.add((double) (System.currentTimeMillis()-lastConnectionTime));
                lastConnectionTime = System.currentTimeMillis();
            }
        }
    }
}