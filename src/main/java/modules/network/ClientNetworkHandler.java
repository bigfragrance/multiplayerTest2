package modules.network;

import engine.math.Vec2d;
import modules.client.ClientNetwork;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
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
        o.put("position",pos.toJSON());
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
        o.put("id",id);
        send(o);
    }
    public void sendKeepAlive(){
        if(System.currentTimeMillis()-lastSend<1000) return;
        JSONObject o=new JSONObject();
        o.put("type","ka");
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
        }
    }
    public void handleEntityUpdate(JSONObject o){
        Entity e=cs.entities.get(o.getJSONObject("basic").getLong("id"));
        if(e!=null){
            if(cs.player!=null&& e.id==cs.player.id) return;
            System.out.println("entity update");
            e.update(o);
        }else{
            System.out.println("entity want");
            sendWantEntity(o.getJSONObject("basic").getLong("id"));
        }
    }
    public void handleEntitySpawn(JSONObject o){
        JSONObject o2=o.getJSONObject("entity");
        switch (o2.getString("type")){
            case("player")->{
                System.out.println("Spawned");
                cs.addEntity(PlayerEntity.fromJSON(o2.getJSONObject("data")));
            }
        }
    }
    public void handleEntityRemove(JSONObject o){
        cs.removeEntity(o.getLong("id"));
    }
    public void handlePlayerRespawn(JSONObject o){
        JSONObject o2=o.getJSONObject("entity");
        switch (o2.getString("type")){
            case("player")->{
                PlayerEntity player=PlayerEntity.fromJSON(o2.getJSONObject("data"));
                cs.player=player;
                cs.addEntity(player);
            }
        }
    }
}
