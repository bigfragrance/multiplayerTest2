package big.modules.network.packet.c2s;

import big.engine.math.Vec2d;
import big.engine.math.util.PacketUtil;
import big.modules.entity.player.ServerPlayerEntity;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import org.json.JSONObject;

public class PlayerInputC2SPacket implements Packet<ServerNetworkHandler> {
    public int forward;
    public int side;
    public Vec2d aimPos;
    public boolean shoot;
    public boolean defend;
    public int upgradingSkill;
    public PlayerInputC2SPacket(int forward, int side, Vec2d aimPos, boolean shoot,boolean defend,int upgradingSkill) {
        this.forward = forward;
        this.side = side;
        this.aimPos = aimPos;
        this.shoot = shoot;
        this.defend=defend;
        this.upgradingSkill=upgradingSkill;
    }
    public PlayerInputC2SPacket(JSONObject o) {
        this.forward = PacketUtil.getInt(o,"forward");
        this.side = PacketUtil.getInt(o,"side");
        this.aimPos = Vec2d.fromJSON(PacketUtil.getJSONObject(o,"aimPos"));
        this.shoot = PacketUtil.getBoolean(o,"shoot");
        this.defend=PacketUtil.getBoolean(o,"defend");
        this.upgradingSkill=PacketUtil.getInt(o,"upgradingSkill");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"forward",forward);
        PacketUtil.put(o,"side",side);
        PacketUtil.put(o,"aimPos",aimPos);
        PacketUtil.put(o,"shoot",shoot);
        PacketUtil.put(o,"defend",defend);
        PacketUtil.put(o,"upgradingSkill",upgradingSkill);
        return o;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        ServerPlayerEntity player=serverNetworkHandler.clientHandler.player;
        if(player!=null){
            player.inputManager.forward=forward;
            player.inputManager.side=side;
            player.inputManager.aimPos=aimPos;
            player.inputManager.shoot=shoot;
            player.inputManager.defend=defend;
            player.inputManager.upgradingSkill=upgradingSkill;
            player.rotation=aimPos.angle();
            //System.out.println("input:"+forward+","+side+","+aimPos+","+shoot+","+upgradingSkill);
        }
    }

    @Override
    public String getType() {
        return "player_input";
    }
}
