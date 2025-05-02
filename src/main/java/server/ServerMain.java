package server;

import java.net.ServerSocket;
import java.net.Socket; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
 
public class ServerMain {
    private static final int PORT = 8088;
    private static final int MAX_THREADS = 50;
    private static final CanvasManager canvas = new CanvasManager();
 
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS); 
        
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server  listening on port " + PORT);
            
            while (true) {
                Socket client = server.accept(); 
                pool.execute(new  ClientHandler(client, canvas));
            }
        }
    }
}