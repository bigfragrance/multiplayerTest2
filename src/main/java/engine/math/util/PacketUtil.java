package engine.math.util;

import engine.math.Vec2d;
import modules.network.ClientNetworkHandler;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.network.packet.c2s.PlayerInputC2SPacket;
import modules.network.packet.c2s.PlayerRespawnC2SPacket;
import modules.network.packet.c2s.UpdateWeaponC2SPacket;
import modules.network.packet.s2c.PlayerDataS2CPacket;
import modules.network.packet.s2c.PlayerSpawnS2CPacket;
import modules.network.packet.s2c.PlayerStatusS2CPacket;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static engine.math.util.PacketName.player_data;
import static engine.math.util.PacketName.player_input;

public class PacketUtil {

    public static String getShortVariableName(String name){
        String s=encode(PacketVariable.class,name);
        if(s==null){
            //System.out.print("  encode failed:"+name);
            return name;
        }
        //System.out.print("  encode success:"+name+"->"+s);
        return s;

        /*
        switch (name){
            case("position")->{
                return "a";
            }
            case("velocity")->{
                return "b";
            }
            case("id")->{
                return "c";
            }
            case("boundingBox")->{
                return "d";
            }
            case("health")->{
                return "e";
            }
            case("damage")->{
                return "f";
            }
            case("team")->{
                return "g";
            }
            case("isAlive")->{
                return "h";
            }
            case("rotation")->{
                return "i";
            }
            case("score")->{
                return "j";
            }
            case("type")->{
                return "k";
            }
            case("forward")->{
                return "l";
            }
            case("side")->{
                return "m";
            }
            case("aimPos")->{
                return "n";
            }
            case("shoot")->{
                return "o";
            }
            case("upgradingSkill")->{
                return "p";
            }
            case("name")->{
                return "q";
            }
            case("skillPoints")->{
                return "r";
            }
        }
        return name;*/
    }
    public static String getShortPacketName(String name){
        String s=encode(PacketName.class,name);
        if(s==null){
            //System.out.print("  encode failed:"+name);
            return name;
        }
        //System.out.print("  encode success:"+name+"->"+s);
        return s;
    }
    public static String fromShortPacketName(String name){
        String s=decode(PacketName.class,name);
        if(s==null){
            //System.out.print("  encode failed:"+name);
            return name;
        }
        //System.out.print("  encode success:"+name+"->"+s);
        return s;
    }
    public static Packet<ClientNetworkHandler> getS2CPacket(JSONObject o){
        String type=o.getString(getShortVariableName("type"));
        String name=decode(PacketName.class,type);
        if(name==null){
            //System.out.print("  decode failed:"+type);
            return null;
        }
        //System.out.print("  decode success:"+type+"->"+name);
        switch (name){
            case("player_data")->{
                return new PlayerDataS2CPacket(o);
            }
            case("player_spawn")->{
                return new PlayerSpawnS2CPacket(o);
            }
            case("player_status")->{
                return new PlayerStatusS2CPacket(o);
            }
        }
        return null;
    }
    public static Packet<ServerNetworkHandler> getC2SPacket(JSONObject o){
        String type=o.getString(getShortVariableName("type"));
        String name=decode(PacketName.class,type);
        if(name==null){
            //System.out.print("  decode failed:"+type);
            return null;
        }
        //System.out.print("  decode success:"+type+"->"+name);
        switch (name){
            case("player_input")->{
                return new PlayerInputC2SPacket(o);
            }
            case("player_respawn")->{
                return new PlayerRespawnC2SPacket();
            }
            case("update_weapon")->{
                return new UpdateWeaponC2SPacket(o);
            }
        }
        return null;
    }
    public static String decode(Class<?> packetClass, String encodedName) {
        try {
            Field[] fields = packetClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()))  {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = field.get(null);

                    if (value.equals(encodedName)) {
                        return fieldName;
                    }
                }
            }
        } catch (Exception  e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String encode(Class<?> packetClass, String fieldName) {
        try {
            Field field = packetClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(null);
            return (String) value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void put(JSONObject o,String name,double value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o,String name,int value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o,String name,long value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o,String name,boolean value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o, String name, Vec2d value){
        o.put(getShortVariableName(name),value.toJSON());
    }
    public static void put(JSONObject o, String name, String value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o, String name,Object value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o, String name,JSONObject value){
        o.put(getShortVariableName(name),value);
    }
    public static void put(JSONObject o, String name, JSONArray value){
        o.put(getShortVariableName(name),value);
    }
    public static void putPacketType(JSONObject o,String value){
        o.put(getShortVariableName("type"),getShortPacketName(value));
    }
    public static Object get(JSONObject o,String name){
        return o.get(getShortVariableName(name));
    }
    public static double getDouble(JSONObject o,String name){
        return o.getDouble(getShortVariableName(name));
    }
    public static int getInt(JSONObject o,String name){
        return o.getInt(getShortVariableName(name));
    }
    public static long getLong(JSONObject o,String name){
        return o.getLong(getShortVariableName(name));
    }
    public static boolean getBoolean(JSONObject o,String name){
        return o.getBoolean(getShortVariableName(name));
    }
    public static String getString(JSONObject o,String name){
        return o.getString(getShortVariableName(name));
    }
    public static JSONObject getJSONObject(JSONObject o,String name){
        return o.getJSONObject(getShortVariableName(name));
    }
    public static JSONArray getJSONArray(JSONObject o,String name){
        return o.getJSONArray(getShortVariableName(name));
    }
}
