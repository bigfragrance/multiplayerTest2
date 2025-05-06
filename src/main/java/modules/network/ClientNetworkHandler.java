package modules.network;

import engine.math.Vec2d;
import engine.math.util.PacketUtil;
import modules.client.ClientNetwork;
import modules.entity.BulletEntity;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
import modules.entity.PolygonEntity;
import org.json.JSONObject;

import java.io.IOException;

import static engine.modules.EngineMain.cs;

public class ClientNetworkHandler {
    public ClientNetwork clientNetwork;
    private long lastSend=0;
    public ClientNetworkHandler(String ip, int port) throws IOException {
        this.clientNetwork = new ClientNetwork(this);
        clientNetwork.connect(ip,port);
    }
    public void send(JSONObject o){
        this.clientNetwork.send(o);
        lastSend=System.currentTimeMillis();
    }
    public void sendPlayerMove(Vec2d pos){
        JSONObject o=new JSONObject();
        o.put("type","player_move");
        o.put(PacketUtil.getShortString("position"),pos.toJSON());
        send(o);
    }
    public void sendPlayerRespawn(){
        JSONObject o=new JSONObject();
        o.put("type","player_respawn");
        send(o);
    }
    public void sendWantEntity(Long id){
        JSONObject o=new JSONObject();
        o.put("type","want_entity");
        o.put(PacketUtil.getShortString("id"),id);
        send(o);
    }
    public void sendKeepAlive(){
        if(System.currentTimeMillis()-lastSend<1000) return;
        JSONObject o=new JSONObject();
        o.put("type","ka");
        send(o);
    }
    public void sendBulletShoot(Vec2d pos,Vec2d velocity,double size,double health,double damage){
        JSONObject o=new JSONObject();
        o.put("type","bullet_shoot");
        o.put(PacketUtil.getShortString("position"),pos.toJSON());
        o.put(PacketUtil.getShortString("velocity"),velocity.toJSON());
        o.put("size",size);
        o.put(PacketUtil.getShortString("health"),health);
        o.put(PacketUtil.getShortString("damage"),damage);
        send(o);
    }
    public void sendPlayerData(PlayerEntity player){
        JSONObject o=new JSONObject();
        o.put("type","player_data");
        o.put("name",player.name);
        send(o);
    }
    public void apply(JSONObject o){
        switch (o.getString("type")){
            case ("entity_update")->{
                handleEntityUpdate(o);
            }
            case("entity_spawn")->{
                handleEntitySpawn(o);
            }
            case("entity_remove")->{
                handleEntityRemove(o);
            }
            case("player_respawn")->{
                handlePlayerRespawn(o);
            }
            case("player_status")->{
                handlePlayerStatus(o);
            }
            case("player_death")->{
                cs.player.isAlive=false;
            }
            case("player_data")->{
                handlePlayerData(o);
            }
        }
    }
    public void handleEntityUpdate(JSONObject o){
        Entity e=cs.entities.get(o.getJSONObject("basic").getLong(PacketUtil.getShortString("id")));
        if(e!=null){
            if(cs.player!=null&& e.id==cs.player.id) {
                cs.player.update2(o);
                return;
            }
            //System.out.println("entity update");
            e.update(o);
        }else{
            System.out.println("entity want");
            sendWantEntity(o.getJSONObject("basic").getLong(PacketUtil.getShortString("id")));
        }
    }
    public void handleEntitySpawn(JSONObject o){
        JSONObject o2=o.getJSONObject("entity");
        switch (o2.getString("type")){
            case("player")->{
                cs.addEntity(PlayerEntity.fromJSON(o2.getJSONObject("data")));
            }
            case("bullet")->{
                //System.out.println("Spawned bullet");
                cs.addEntity(BulletEntity.fromJSON(o2.getJSONObject("data")));
            }
            case("polygon")->{
                cs.addEntity(PolygonEntity.fromJSON(o2.getJSONObject("data")));
            }
        }
    }
    public void handleEntityRemove(JSONObject o){
        cs.removeEntity(o.getLong(PacketUtil.getShortString("id")));
    }
    public void handlePlayerStatus(JSONObject o){
        if(cs.player!=null){
            cs.player.updateStatus(o);
            cs.generateGroundBlocks(true);
        }
    }
    public void handlePlayerRespawn(JSONObject o){
        JSONObject o2=o.getJSONObject("entity");
        switch (o2.getString("type")){
            case("player")->{
                PlayerEntity player=PlayerEntity.fromJSON(o2.getJSONObject("data"));
                cs.player=player;
                cs.addEntity(player);
                cs.generateGroundBlocks(true);
            }
        }
    }
    public void handlePlayerData(JSONObject o){
        Entity e=cs.entities.get(o.getLong(PacketUtil.getShortString("id")));
        if(e instanceof PlayerEntity player){
            player.name=o.getString("name");
        }
    }
}
