package big.game.client;

import big.engine.math.util.PacketUtil;
import big.engine.math.util.PercentEncoder;
import big.game.network.ClientNetworkHandler;
import big.game.network.JSONNBTConverter;
import net.querz.nbt.io.*;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.json.JSONObject;
import javax.swing.*; 
import java.awt.*; 
import java.io.*; 
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public void connect(String address, int port) throws IOException {
        this.serverAddress = address;
        this.port = port;
        establishConnection();

        new Thread(() -> {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                while (running) {

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


                    JSONObject msg = JSONNBTConverter.toJSON(receivedTag);
                    try {
                        networkHandler.apply(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                if (running) handleDisconnect(e);
            }
        }).start();
    }
    private void establishConnection() throws IOException {
        socket = new Socket();
        socket.connect(new  InetSocketAddress(serverAddress, port), 3000);
        socket.setTcpNoDelay(true);
        out = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream(),  StandardCharsets.UTF_8), true);
        in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        send(new JSONObject().put("type", "handshake"));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connected=true;
    }
 
    private void handleDisconnect(Exception e) {
        e.printStackTrace();
        SwingUtilities.invokeLater(()  -> 
            JOptionPane.showMessageDialog(null,  "Reconnecting to big.server..."));
        
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
    public void sendold(JSONObject json) {
        if(!connected){
            toSend.add(json.toString());

            return;
        }
        while(!toSend.isEmpty()){
            out.println(PercentEncoder.encodeChinese(toSend.poll()));
        }
        out.println(PercentEncoder.encodeChinese(json.toString()));
    }
    public void send(JSONObject json) {
        if (!connected) {
            toSend.add(json.toString());
            return;
        }

        try {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());


            while (!toSend.isEmpty()) {
                String cached = toSend.poll();
                sendJsonAsCompoundTag(cached, dos);
            }

            sendJsonAsCompoundTag(json.toString(), dos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendJsonAsCompoundTag(String jsonString, DataOutputStream dos) throws IOException {
        JSONObject obj = new JSONObject(jsonString);
        CompoundTag compoundTag = JSONNBTConverter.toCompound(obj);

        NamedTag namedTag = new NamedTag("root", compoundTag);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream out = new GZIPOutputStream(baos, true)) {
            NBTOutput nbtOut = new NBTOutputStream(out); // LittleEndianNBTOutputStream(out)
            nbtOut.writeTag(namedTag, Tag.DEFAULT_MAX_DEPTH);
            nbtOut.flush();
        }

        byte[] bytes = baos.toByteArray();

        dos.writeInt(bytes.length);
        dos.write(bytes);
        dos.flush();
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