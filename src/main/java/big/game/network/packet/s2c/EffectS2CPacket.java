package big.game.network.packet.s2c;

import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.events.MessageReceiveEvent;
import big.game.entity.effect.Effect;
import big.game.network.ClientNetworkHandler;
import big.game.network.PacketType;
import big.game.network.packet.Packet;
import big.game.screen.EffectScreen;
import big.server.NettyClientHandler;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static big.engine.modules.EngineMain.cs;

public class EffectS2CPacket implements Packet<ClientNetworkHandler> {
    public String name;
    public int time;
    public EffectS2CPacket(String name, int time){
        this.name=name;
        this.time=time;
    }
    public EffectS2CPacket(JSONObject o){
        this.name=PacketUtil.getString(o,"name");
        this.time=PacketUtil.getInt(o,"time");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"name",name);
        PacketUtil.put(o,"time", time);
        PacketUtil.putPacketType(o,getType());
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(time==0){
            EffectScreen.INSTANCE.removeEffect(name);
        }else {
            EffectScreen.INSTANCE.addEffect(new Effect(name, time, time));
        }
    }

    @Override
    public PacketType getType() {
        return PacketType.EFFECT_S2C;
    }
}
