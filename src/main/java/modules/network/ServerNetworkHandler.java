package modules.network;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import modules.entity.BulletEntity;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
import org.json.JSONObject;
import server.ClientHandler;

import static engine.modules.EngineMain.cs;

public class ServerNetworkHandler {
    public ClientHandler clientHandler;
    public boolean deathSent=false;
    public ServerNetworkHandler(ClientHandler client){
        this.clientHandler =client;
    }
    public void apply(JSONObject o){
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
            case("bullet_shoot")->{
                handleBulletShoot(o);
            }
            case("player_data")->{
                handlePlayerData(o);
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
        o2.put(PacketUtil.getShortString("id"),id);
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
            send(e.getUpdate());
        }
    }
    public void checkDeath(){
        if(clientHandler.player!=null){
            if(!clientHandler.player.isAlive){
                JSONObject o=new JSONObject();
                o.put("type","player_death");
                send(o);
                deathSent=true;
            }
        }
    }
    public void sendPlayerRespawn(PlayerEntity player){
        JSONObject o=new JSONObject();
        o.put("type","player_status");
        player.addJSON(o);
        send(o);
    }
    public void sendPlayerData(PlayerEntity player){
        JSONObject o=new JSONObject();
        o.put("type","player_data");
        o.put("name",player.name);
        o.put(PacketUtil.getShortString("id"),player.id);
        send(o);
    }
    public void handlePlayerMove(JSONObject o){
        Vec2d position=Vec2d.fromJSON(o.getJSONObject(PacketUtil.getShortString("position")));
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
            e.setPosition(EntityUtils.getRandomSpawnPosition());
            e.health=PlayerEntity.healthMax;
            e.noEnemyTimer=10;
            e.score*=0.5;
            sendPlayerRespawn(e);
        }else{
            PlayerEntity player=new PlayerEntity(new Vec2d(0,0));
            player.team=cs.getTeam();
            cs.addEntity(player);
            clientHandler.player=player;
            sendPlayerRespawn(clientHandler.player);
        }
    }
    public void handleWantEntity(JSONObject o){
        Entity e=cs.entities.get(o.getLong(PacketUtil.getShortString("id")));
        if(e!=null){
            sendEntitySpawn(e);
        }
    }
    public void handleBulletShoot(JSONObject o){
        Vec2d pos=Vec2d.fromJSON(o.getJSONObject(PacketUtil.getShortString("position")));
        Vec2d velocity=Vec2d.fromJSON(o.getJSONObject(PacketUtil.getShortString("velocity")));
        double size=o.getDouble("size");
        Entity e= clientHandler.player;
        if(e!=null&&e.isAlive){
            BulletEntity b=new BulletEntity(pos,velocity,new Box(pos,size,size),o.getDouble(PacketUtil.getShortString("health")),o.getDouble(PacketUtil.getShortString("damage")),e.team);
            b.ownerId=e.id;
            cs.addEntity(b);
        }
    }
    public void handlePlayerData(JSONObject o){
        PlayerEntity e= clientHandler.player;
        if(e!=null){
            e.name=o.getString("name");
            cs.multiClientHandler.clients.forEach(c -> {
                if(c.player.id!=e.id){
                    c.serverNetworkHandler.sendPlayerData(e);
                }
            });
        }
    }
}
