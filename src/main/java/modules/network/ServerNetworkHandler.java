package modules.network;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import modules.entity.BlockEntity;
import modules.entity.bullet.BulletEntity;
import modules.entity.Entity;
import modules.entity.player.PlayerEntity;
import modules.entity.player.ServerPlayerEntity;
import modules.network.packet.Packet;
import org.json.JSONObject;
import server.ClientHandler;
import server.ServerMain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static engine.math.util.PacketName.*;
import static engine.modules.EngineMain.cs;

public class ServerNetworkHandler {
    public static double updateRange=10;
    public static double updateRangeBlocks=10;
    public ClientHandler clientHandler;
    public boolean deathSent=false;
    public ConcurrentHashMap<Long,Boolean> sentRemove=new ConcurrentHashMap<>();
    public ServerNetworkHandler(ClientHandler client){
        this.clientHandler =client;
    }
    public void apply(JSONObject o){
        Packet<ServerNetworkHandler> packet= PacketUtil.getC2SPacket(o);
        if(packet!=null){
            packet.apply(this);
            return;
        }
        switch (PacketUtil.fromShortPacketName(PacketUtil.getString(o,"type"))){
            case ("player_move")->{
                handlePlayerMove(o);
            }
            case("player_respawn")->{
                handlePlayerSpawn(o);
            }
            case("want_entity")->{
                handleWantEntity(o);
            }
            case("player_data_other")->{
                handlePlayerData(o);
            }
        }
    }
    public void send(JSONObject o){
        this.clientHandler.send(o);
    }
    public void sendEntitySpawn(Entity e){
        if(e!=null){
            if(!inRange(e)) return;
            JSONObject o2=new JSONObject();
            o2.put(PacketUtil.getShortVariableName("type"),entity_spawn);

            JSONObject o3=new JSONObject();
            o3.put(PacketUtil.getShortVariableName("type"),e.getType());
            o3.put("data",e.toJSON());
            o2.put("entity",o3);

            send(o2);
        }
    }
    public boolean inRange(Entity e){
        double distance=e.prevPosition.distanceTo(clientHandler.player.position)+e.boundingBox.avgSize()/2;
        distance /=clientHandler.player.getFov();
        return e instanceof BlockEntity?distance<updateRangeBlocks:distance<updateRange;
    }
    public void sendEntityRemove(long id){
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
        if(e==null) return;
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
    public void handlePlayerMove(JSONObject o){
        Vec2d position=Vec2d.fromJSON(o.getJSONObject(PacketUtil.getShortVariableName("position")));
        Entity e= cs.entities.get(clientHandler.player.id);
        if(e!=null){
            e.setPosition(position);
        }
    }
    public void handlePlayerSpawn(JSONObject o){
        PlayerEntity e= clientHandler.player;
        if(e!=null){
            if(e.isAlive) return;
            e.isAlive=true;
            e.setPosition(EntityUtils.getRandomSpawnPosition(e.team));
            e.health=PlayerEntity.healthMax;
            e.noEnemyTimer=10;
            e.score*=0.5;
            sendPlayerRespawn(e);
            /*cs.multiClientHandler.clients.forEach(c->{
                if(c.player.id!=e.id){
                    c.serverNetworkHandler.sendEntityRemove(e.id);
                }
            });*/
        }else{
            ServerPlayerEntity player=new ServerPlayerEntity(new Vec2d(0,0));
            player.team=cs.getTeam();
            cs.addEntity(player);
            clientHandler.player=player;
            sendPlayerRespawn(clientHandler.player);
        }
    }
    public void handleWantEntity(JSONObject o){
        Entity e=cs.entities.get(o.getLong(PacketUtil.getShortVariableName("id")));
        if(e!=null){
            sendEntitySpawn(e);
        }
    }
    /*public void handleBulletShoot(JSONObject o){
        Vec2d pos=Vec2d.fromJSON(o.getJSONObject(PacketUtil.getShortVariableName("position")));
        Vec2d velocity=Vec2d.fromJSON(o.getJSONObject(PacketUtil.getShortVariableName("velocity")));
        double size=o.getDouble("size");
        Entity e= clientHandler.player;
        if(e!=null&&e.isAlive){
            BulletEntity b=new BulletEntity(pos,velocity,new Box(pos,size,size),o.getDouble(PacketUtil.getShortVariableName("health")),o.getDouble(PacketUtil.getShortVariableName("damage")),e.team);
            b.ownerId=e.id;
            cs.addEntity(b);
        }
    }*/
    public void handlePlayerData(JSONObject o){
        ServerPlayerEntity e= clientHandler.player;
        if(e!=null){
            e.name= PacketUtil.getString(o,"name");
            if(ServerMain.connectedPlayersEntity.containsKey(e.name.hashCode())){
                ServerMain.connectedPlayersEntity.get(e.name.hashCode()).set(e);
            }
            cs.multiClientHandler.clients.forEach(c -> {
                if(c.player.id!=e.id){
                    c.serverNetworkHandler.sendPlayerData(e);
                }
            });
        }
    }
}
