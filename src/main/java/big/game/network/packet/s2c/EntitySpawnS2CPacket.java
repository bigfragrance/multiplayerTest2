package big.game.network.packet.s2c;

import big.engine.util.PacketUtil;
import big.game.entity.Entity;
import big.game.entity.EntityType;
import big.game.entity.player.ClientPlayerEntity;
import big.game.network.ClientNetworkHandler;
import big.game.network.PacketType;
import big.game.network.packet.Packet;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;
import static big.engine.util.PacketName.entity_spawn;

public class EntitySpawnS2CPacket implements Packet<ClientNetworkHandler> {
    public Entity entity;
    public EntitySpawnS2CPacket(Entity entity) {
        this.entity=entity;
    }
    public EntitySpawnS2CPacket(JSONObject o) {
        JSONObject o3=o.getJSONObject("entity");
        this.entity=EntityType.createEntity(o3);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());

        JSONObject o3=new JSONObject();
        PacketUtil.put(o3,"type",entity.getType());
        PacketUtil.put(o3,"data",entity.toJSON());
        PacketUtil.put(o,"entity",o3);
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(entity==null) System.out.println("null ete");
        cs.addEntity(entity);
    }

    @Override
    public PacketType getType() {
        return PacketType.ENTITY_SPAWN_S2C;
    }
}
