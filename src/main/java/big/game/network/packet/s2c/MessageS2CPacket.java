package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.events.MessageReceiveEvent;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import big.server.ClientHandler;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static big.engine.modules.EngineMain.cs;

public class MessageS2CPacket implements Packet<ClientNetworkHandler> {
    public static List<String> chatHistory=new ArrayList<>();
    public String text;
    public int aheadTime;
    public MessageS2CPacket(String text){
        this(text,true,0);
    }
    public MessageS2CPacket(String text,boolean time,int aheadTime){
        this.text= (time? Util.timeString()+" | ":"")+text;
        this.aheadTime =aheadTime;
    }
    public MessageS2CPacket addHistory(){
        chatHistory.add(text);
        if(chatHistory.size()>1000){
            chatHistory.remove(0);
        }
        return this;
    }
    public MessageS2CPacket(JSONObject o){
        this.text=PacketUtil.getString(o,"text");
        this.aheadTime=PacketUtil.getInt(o,"aheadTime");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"text",text);
        PacketUtil.put(o,"aheadTime", aheadTime);
        PacketUtil.putPacketType(o,getType());
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        cs.EVENT_BUS.post(MessageReceiveEvent.get(text,aheadTime));
    }

    @Override
    public String getType() {
        return "message";
    }
    public static void sendHistory(ClientHandler c){
        for(String s:chatHistory){
            c.send(new MessageS2CPacket(s,false,15000));
        }
    }
}
