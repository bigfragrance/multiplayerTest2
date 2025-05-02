package modules.network;

import engine.math.Vec2d;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
import org.json.JSONObject;
import server.ClientHandler;

import static engine.modules.EngineMain.cs;

public class ServerNetworkHandler {
    public ClientHandler clientHandler;

    public ServerNetworkHandler(ClientHandler client){
        this.clientHandler =client;
    }
    public void apply(JSONObject o){
        System.out.println("received");
        switch (o.getString("type")){
            case ("player_move")->{
                handlePlayerMove(o);
            }
            case("player_respawn")->{
                handlePlayerSpawn(o);
            }
            case("want_entity")->{
                handleWantEntity(o);
            }
        }
    }
    public void send(JSONObject o){
        this.clientHandler.send(o);
    }
    public void sendEntitySpawn(Entity e){
        if(e!=null){
            JSONObject o2=new JSONObject();
            o2.put("type","entity_spawn");

            JSONObject o3=new JSONObject();
            o3.put("type",e.getType());
            o3.put("data",e.toJSON());
            o2.put("entity",o3);

            send(o2);
        }
    }
    public void sendEntityRemove(long id){
        JSONObject o2=new JSONObject();
        o2.put("type","entity_remove");
        o2.put("id",id);
        send(o2);
    }
    public void sendPlayerSpawn(Entity e){
        if(e!=null){
            JSONObject o2=new JSONObject();
            o2.put("type","player_respawn");

            JSONObject o3=new JSONObject();
            o3.put("type",e.getType());
            o3.put("data",e.toJSON());
            o2.put("entity",o3);

            send(o2);
        }
    }
    public void sendEntityUpdate(Entity e){
        if(e!=null){
            if(e.id== clientHandler.player.id) return;
            send(e.getUpdate());
        }
    }
    public void handlePlayerMove(JSONObject o){
        Vec2d position=Vec2d.fromJSON(o.getJSONObject("position"));
        Entity e= cs.entities.get(clientHandler.player.id);
        if(e!=null){
            e.setPosition(position);
        }
    }
    public void handlePlayerSpawn(JSONObject o){
        Entity e= clientHandler.player;
        if(e!=null){
            e.isAlive=true;
        }else{
            PlayerEntity player=new PlayerEntity(new Vec2d(0,0));
            cs.addEntity(player);
            clientHandler.player=player;
        }
    }
    public void handleWantEntity(JSONObject o){
        Entity e=cs.entities.get(o.getLong("id"));
        if(e!=null){
            sendEntitySpawn(e);
        }
    }
}
