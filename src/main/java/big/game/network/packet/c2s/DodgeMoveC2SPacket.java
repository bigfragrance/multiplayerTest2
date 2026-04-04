package big.game.network.packet.c2s;

import big.engine.math.Vec2d;
import big.engine.util.PacketUtil;
import big.game.network.PacketType;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import org.json.JSONObject;

public class DodgeMoveC2SPacket implements Packet<ServerNetworkHandler> {
    private final Vec2d direction;
    public DodgeMoveC2SPacket(Vec2d direction){
        this.direction = direction;
    }
    public DodgeMoveC2SPacket(JSONObject json){
        direction = PacketUtil.getVec2d(json,"direction");
    }


    public Vec2d getDirection() {
        return direction;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        PacketUtil.put(json,"direction",direction);
        PacketUtil.putPacketType(json,getType());
        return json;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        serverNetworkHandler.clientHandler.player.dodgeMove(direction);
    }

    @Override
    public PacketType getType() {
        return PacketType.DODGE_MOVE_C2S;
    }
}
