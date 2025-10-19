package big.game.network.packet.s2c;

import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.c2s.MessageC2SPacket;
import big.game.screen.ChatMessageScreen;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.modules.EngineMain.cs;

public class AssetsS2CPacket implements Packet<ClientNetworkHandler> {
    public static int total=-1;
    public static int assetsHash=0;
    public static ConcurrentHashMap<String, BufferedImage> assets = new ConcurrentHashMap<>();
    public static List<AssetsS2CPacket> createdData=new ArrayList<>();
    public static ConcurrentHashMap<Integer, String> receivedData = new ConcurrentHashMap<>();
    public static String RECEIVED="Asset received7n9-43mfhgx3x4hnw";
    public static String NEED_UPDATE="Assets out of date, need update wpxoeuhnboiumb";
    public static String LENGTH="length";
    public static String HASH="hash";
    public static String state="receiving";
    public String data;
    public int sequence;
    public AssetsS2CPacket(String data, int sequence) {
        this.data = data;
        this.sequence = sequence;
    }
    public AssetsS2CPacket(int total) {
        this.sequence = total;
        this.data=LENGTH;
    }
    public AssetsS2CPacket(JSONObject obj){
        this.sequence=PacketUtil.getInt(obj,"sequence");
        this.data=PacketUtil.getString(obj,"data");
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj=new JSONObject();
        PacketUtil.putPacketType(obj,getType());
        PacketUtil.put(obj,"sequence",sequence);
        PacketUtil.put(obj,"data",data);
        return obj;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(data.equals(LENGTH)){
            total=sequence;
            return;
        }
        if(data.equals(HASH)){
            assetsHash=sequence;
            if(assetsHash!=cs.setting.getAssetsHash()){
                cs.networkHandler.send(new MessageC2SPacket(NEED_UPDATE));
            }else{
                cs.networkHandler.send(new MessageC2SPacket(RECEIVED));
            }
            return;
        }
        receivedData.put(sequence, data);
        state="Receiving: "+receivedData.size()+"/"+total;
        if(receivedData.size()==total){
            mergeAndProcess();
        }
    }

    @Override
    public String getType() {
        return "assets";
    }
    public static void init(){
        create(readAssets());
    }
    public static void create(Map<String, BufferedImage> assets){
        createdData=new java.util.ArrayList<>();
        JSONObject obj=new JSONObject();
        int hash=31;
        for(String key:assets.keySet()){
            String img=Util.imageToString(assets.get(key),"png");
            obj.put(key,img);
            hash=hash*31+img.hashCode();
        }
        assetsHash=hash;
        String json= Base64.getEncoder().encodeToString(obj.toString().getBytes());
        for(String part:Util.splitString(json,16384)){
            createdData.add(new AssetsS2CPacket(part,createdData.size()));
        }
        createdData.add(0,new AssetsS2CPacket(createdData.size()));
    }
    public static Map<String,BufferedImage> readAssets(){
        ConcurrentHashMap<String,BufferedImage> map=new ConcurrentHashMap<>();
        Util.loadImagesRecursively(new File(System.getProperty("user.dir")),map);
        return map;
    }
    public static void mergeAndProcess() {
        if (total == -1) return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < total; i++) {
            String part = receivedData.get(i);
            if (part == null) {
                System.err.println("Missing part " + i);
                return;
            }
            sb.append(part);
            if((i&1)==0){
                state="Merging string: "+i+"/"+total;
            }
        }
        state="Building String";
        String json = new String(Base64.getDecoder().decode(sb.toString()));
        JSONObject obj = new JSONObject(json);
        int i=0;
        for (String key : obj.keySet()) {
            String data = obj.getString(key);
            assets.put(key, Util.stringToImage(data));
            state="Merging image: "+i+"/"+obj.keySet().size();
            i++;
        }
        i=0;
        File assetsDir = new File(System.getProperty("user.dir"), "assets");
        if (!assetsDir.exists()) {
            assetsDir.mkdirs();
        }
        for (Map.Entry<String, BufferedImage> entry :assets.entrySet()) {
            String fileName = entry.getKey();
            BufferedImage image = entry.getValue();
            if (!fileName.toLowerCase().endsWith(".png")) {
                fileName += ".png";
            }
            File outputFile = new File(assetsDir, fileName);
            try {
                ImageIO.write(image, "png", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            state = "saving: " + i + "/" + assets.size();
            i++;
        }
        cs.networkHandler.send(new MessageC2SPacket(RECEIVED));
        cs.setting.setAssetsHash(assetsHash);
        cs.setting.save();
        state="Done";
    }
}
