package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import org.json.JSONObject;

public class TickS2CPacket implements Packet<ClientNetworkHandler> {
    public static long lastTime=0;
    public long timeMillis;
    public TickS2CPacket(long timeMillis){
        this.timeMillis=timeMillis;
    }
    public TickS2CPacket(JSONObject o){
        this.timeMillis=o.getLong("time");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        o.put("time",timeMillis);
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(lastTime>0){
            long delta=timeMillis-lastTime;
            double tps=1000.0/delta;
            //EngineMain.TPS=tps;
        }
        lastTime=timeMillis;
    }

    @Override
    public String getType() {
        return "tick";
    }
}
