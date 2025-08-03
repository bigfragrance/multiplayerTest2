package modules.client;

import engine.math.util.PacketUtil;
import modules.network.ClientNetworkHandler;
import org.json.JSONArray;
import org.json.JSONObject; 
import javax.swing.*; 
import java.awt.*; 
import java.io.*; 
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static engine.modules.EngineMain.cs;

public class ClientNetwork {
    private static final int RECONNECT_DELAY = 5000;
    final ReentrantReadWriteLock canvasLock = new ReentrantReadWriteLock();
    private Color[][] canvas = new Color[64][64];
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAddress;
    private int port = 8088;
    private volatile boolean running = true;
    public ClientNetworkHandler networkHandler;
    private boolean connected=false;
    private Queue<String> toSend=new LinkedList<>();
    public ClientNetwork(ClientNetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    public void connect(String address,int port) throws IOException {
        this.serverAddress  = address;
        this.port  = port;
        establishConnection();

        new Thread(() -> {
            while (running) {
                try {
                    String message = in.readLine();
                    if (message == null) throw new IOException("Connection closed");

                    // 统一处理消息类型
                    JSONObject msgObj = new JSONObject(message);
                    try {
                        cs.networkHandler.apply(msgObj);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    if (running) handleDisconnect(e);
                }
            }
        }).start();
    }
    private void handleSnapshot(JSONArray snapshot) {
        canvasLock.writeLock().lock();
        try {
            for (int i = 0; i < snapshot.length();  i++) {
                JSONObject pixel = snapshot.getJSONObject(i);
                int x = pixel.getInt("x");
                int y = pixel.getInt("y");
                canvas[x][y] = new Color(pixel.getInt("color"),  true);
            }
        } finally {
            canvasLock.writeLock().unlock();
        }
    }

    private void handleSingleUpdate(JSONObject update) {
        canvasLock.writeLock().lock();
        try {
            int x = update.getInt("x");
            int y = update.getInt("y");
            canvas[x][y] = new Color(update.getInt("color"),  true);
        } finally {
            canvasLock.writeLock().unlock();
        }
    }
    private void establishConnection() throws IOException {
        socket = new Socket();
        socket.connect(new  InetSocketAddress(serverAddress, port), 3000);
        socket.setTcpNoDelay(true);
        out = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream(),  StandardCharsets.UTF_8), true);
        in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        out.println("handshake");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connected=true;
    }
 
    private void handleDisconnect(Exception e) {
        System.err.println("Connection  lost: " + e.getMessage()); 
        SwingUtilities.invokeLater(()  -> 
            JOptionPane.showMessageDialog(null,  "Reconnecting to server..."));
        
        try {
            Thread.sleep(RECONNECT_DELAY); 
            establishConnection();
        } catch (Exception ex) {
            if (running) handleDisconnect(ex);
        }
    }
 
    public void sendPixelUpdate(int x, int y, Color color) {
        JSONObject json = new JSONObject();
        json.put(PacketUtil.getShortVariableName("type"),  "pixel_update");
        json.put("x",  x);
        json.put("y",  y);
        json.put("color",  color.getRGB()); 
        out.println(json.toString());
        out.println(json.toString());
        out.println(json.toString());
    }
    public void send(JSONObject json) {
        if(!connected){
            toSend.add(json.toString());
            return;
        }
        while(!toSend.isEmpty()){
            out.println(toSend.poll());
        }
        out.println(json.toString());
    }
 
    public Color getPixel(int x, int y) {
        canvasLock.readLock().lock(); 
        try {
            return canvas[x][y];
        } finally {
            canvasLock.readLock().unlock(); 
        }
    }
 
    public void disconnect() {
        running = false;
        try {
            socket.close(); 
        } catch (IOException e) {
            System.err.println("Disconnect  error: " + e.getMessage()); 
        }
    }
}