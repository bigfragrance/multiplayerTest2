package big.game.network;

import big.engine.math.Vec2d;
import big.engine.util.EntityUtils;
import big.engine.util.PacketUtil;
import big.game.entity.BlockEntity;
import big.game.entity.Entity;
import big.game.entity.EntityType;
import big.game.entity.player.PlayerEntity;
import big.game.entity.player.ServerPlayerEntity;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.EntitySpawnS2CPacket;
import big.server.NettyClientHandler;
import big.server.NettyServerMain;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.util.PacketName.*;
import static big.engine.modules.EngineMain.cs;

public class ServerNetworkHandler {
    public static double updateRange=10;
    public static double updateRangeBlocks=10;
    public NettyClientHandler clientHandler;
    public boolean deathSent=false;
    public ConcurrentHashMap<Long,Boolean> sentRemove=new ConcurrentHashMap<>();
    public ServerNetworkHandler(NettyClientHandler client){
        this.clientHandler =client;
    }
    public void apply(JSONObject o){
        Packet<ServerNetworkHandler> packet= PacketUtil.getC2SPacket(o);
        if(packet!=null){
            packet.apply(this);
        }
    }
    public void send(JSONObject o){
        this.clientHandler.send(o);
    }
    public void sendEntitySpawn(Entity e){
        if(!clientHandler.dataSent) return;
        if(e!=null){
            if(!inRange(e)) return;
            send(new EntitySpawnS2CPacket(e).toJSON());
        }
    }
    public boolean inRange(Entity e){
        if(!clientHandler.dataSent) return false;
        double distance=e.prevPosition.distanceTo(clientHandler.player.position)+e.boundingBox.avgSize()/2;
        distance /=clientHandler.player.getFov();
        return e instanceof ServerPlayerEntity|| (e instanceof BlockEntity?distance<updateRangeBlocks:distance<updateRange);
    }
    public void sendEntityRemove(long id){
        if(sentRemove.getOrDefault(id,false)) return;
        JSONObject o2=new JSONObject();
        o2.put(PacketUtil.getShortVariableName("type"),entity_remove);
        o2.put(PacketUtil.getShortVariableName("id"),id);
        send(o2);
        //sentRemove.remove(id);
    }
    public void sendPlayerSpawn(Entity e){
        if(e!=null){
            JSONObject o2=new JSONObject();
            o2.put(PacketUtil.getShortVariableName("type"),player_respawn);

            JSONObject o3=new JSONObject();
            o3.put(PacketUtil.getShortVariableName("type"),e.getType());
            o3.put("data",e.toJSON());
            o2.put("entity",o3);

            send(o2);
        }
    }
    public void sendEntityUpdate(Entity e){
        if(e==null||!clientHandler.dataSent) return;
        if(!inRange(e)) {
            sendEntityRemove(e.id);
            sentRemove.put(e.id,true);
            return;
        }
        sentRemove.put(e.id,false);
        send(e.getUpdate());
    }
    public void clearTemp(){
        List<Long> toRemove=new ArrayList<>();
        for(long id:sentRemove.keySet()){
            if(!cs.entities.containsKey(id)){
                toRemove.add(id);
            }
        }
        toRemove.forEach(id -> {
            sentRemove.remove(id);
        });
    }
    public void checkDeath(){
        if(clientHandler.player!=null){
            if(!clientHandler.player.isAlive){
                JSONObject o=new JSONObject();
                o.put(PacketUtil.getShortVariableName("type"),player_death);
                send(o);
                deathSent=true;
            }
        }
    }
    public void sendPlayerRespawn(PlayerEntity player){
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),player_status);
        player.addJSON(o);
        send(o);
    }
    public void sendPlayerData(PlayerEntity player){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"player_data_other");
        PacketUtil.put(o,"name",player.name);
        PacketUtil.put(o,"id",player.id);
        send(o);
    }

}
