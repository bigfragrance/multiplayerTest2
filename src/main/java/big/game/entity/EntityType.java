package big.game.entity;

import big.engine.util.PacketUtil;
import big.engine.util.T2UInterface;
import big.game.entity.bullet.BulletEntity;
import big.game.entity.player.ClientPlayerEntity;
import big.game.entity.player.PlayerEntity;
import org.json.JSONObject;

public enum EntityType {
    PLAYER("player",(obj)-> PlayerEntity.fromJSON(obj)),
    CLIENT_PLAYER("client_player",(obj)->ClientPlayerEntity.fromJSON(obj)),
    BULLET("bullet",(obj)-> BulletEntity.fromJSON(obj)),
    POLYGON("polygon",(obj)-> PolygonEntity.fromJSON(obj)),
    ROCK("rock",(obj)-> RockEntity.fromJSON(obj)),
    BLOCK("block",(obj)-> BlockEntity.fromJSON(obj));
    private final String name;
    private final T2UInterface<JSONObject,Entity> entityFactory;
    private EntityType(String name, T2UInterface<JSONObject,Entity> entityFactory){
        this.name=name;
        this.entityFactory=entityFactory;
    }
    public String getName(){
        return name;
    }
    public String toString(){
        return String.valueOf(this.ordinal());
    }
    public static Entity createEntity(JSONObject json){
        EntityType type=fromName(PacketUtil.getString(json,"type"));
        return type.entityFactory.get(json.getJSONObject("data"));
    }
    public static EntityType fromName(String name){
        return Enum.valueOf(EntityType.class,name);
    }
    public static EntityType fromID(int id){
        return values()[id];
    }
     public int getID(){
        return ordinal();
    }
}
