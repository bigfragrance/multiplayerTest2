package big.game.network;

import big.engine.math.util.PacketUtil;
import big.game.client.ClientNetwork;
import big.game.entity.*;
import big.game.entity.bullet.BulletEntity;
import big.game.entity.player.ClientPlayerEntity;
import big.game.entity.player.PlayerEntity;
import big.game.network.packet.Packet;
import org.json.JSONObject;

import java.io.IOException;

import static big.engine.math.util.PacketName.*;
import static big.engine.modules.EngineMain.cs;

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
    public void send(Packet<?> packet){
        this.clientNetwork.send(packet.toJSON());
        lastSend=System.currentTimeMillis();
    }
    public void sendPlayerRespawn(){
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),player_respawn);
        send(o);
    }
    public void sendWantEntity(Long id){
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),want_entity);
        o.put(PacketUtil.getShortVariableName("id"),id);
        send(o);
    }
    public void sendKeepAlive(){
        if(System.currentTimeMillis()-lastSend<1000) return;
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),"ka");
        send(o);
    }
    public void sendPlayerData(PlayerEntity player){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"player_data_other");
        PacketUtil.put(o,"name",player.name);
        send(o);
    }
    public void apply(JSONObject o){
        Packet<ClientNetworkHandler> packet= PacketUtil.getS2CPacket(o);
        if(packet!=null){
            packet.apply(this);
            return;
        }
        switch (PacketUtil.fromShortPacketName(PacketUtil.getString(o,"type"))){
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
            case("player_data_other")->{
                handlePlayerData(o);
            }
        }
    }
    public void handleEntityUpdate(JSONObject o){
        long id=PacketUtil.getLong(PacketUtil.getJSONObject(o,"basic"),"id");
        Entity e=cs.entities.get(id);
        if(e!=null){
            e.update(o);
        }else{
            sendWantEntity(id);
        }
        /*JSONObject a=PacketUtil.getJSONObject(o,"data");
        a.keys().forEachRemaining(k->{
            JSONObject o2=a.getJSONObject(k);
            Entity e=cs.entities.get(o2.getJSONObject("basic").getLong(PacketUtil.getShortVariableName("id")));
            if(e!=null){
                e.update(o2);
            }else{
                //System.out.println("entity want");
                sendWantEntity(o2.getJSONObject("basic").getLong(PacketUtil.getShortVariableName("id")));
            }
        });
        for(int i=0;i<PacketUtil.getInt(o,"max");i++){
            JSONObject o2=a.getJSONObject(String.valueOf(i));

        }*/
    }
    public void handleEntitySpawn(JSONObject o){
        JSONObject o2=o.getJSONObject("entity");
        switch (o2.getString(PacketUtil.getShortVariableName("type"))){
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
            case("block")->{
                cs.addEntity(BlockEntity.fromJSON(o2.getJSONObject("data")));
            }
            case("rock")->{
                cs.addEntity(RockEntity.fromJSON(o2.getJSONObject("data")));
            }
        }
    }
    public void handleEntityRemove(JSONObject o){
        cs.removeEntity(o.getLong(PacketUtil.getShortVariableName("id")));
    }
    public void handlePlayerStatus(JSONObject o){
        if(cs.player!=null){
            cs.player.updateStatus(o);
        }
    }
    public void handlePlayerRespawn(JSONObject o){
        JSONObject o2=o.getJSONObject("entity");
        switch (o2.getString(PacketUtil.getShortVariableName("type"))){
            case("player")->{
                ClientPlayerEntity player= ClientPlayerEntity.fromJSON(o2.getJSONObject("data"));
                cs.player=player;
                cs.addEntity(player);
            }
        }
    }
    public void handlePlayerData(JSONObject o){
        Entity e=cs.entities.get(PacketUtil.getLong(o,"id"));
        if(e instanceof PlayerEntity player){
            player.name=PacketUtil.getString(o,"name");
        }
    }

    public void sendPacket(Packet<?> packet) {
        send(packet.toJSON());
    }
}
