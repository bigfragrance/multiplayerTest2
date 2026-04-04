package big.engine.util;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.game.network.ClientNetworkHandler;
import big.game.network.PacketType;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.c2s.*;
import big.game.network.packet.s2c.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
        Object type=o.get(getShortVariableName("type"));
        if(!(type instanceof Number)){
            return null;
        }
        int typeInt=(int)type;
        if(typeInt<0||typeInt>=PacketType.values().length){
            return null;
        }
        PacketType packetType=PacketType.values()[typeInt];
        return (Packet<ClientNetworkHandler>) packetType.createPacket(o);
    }
    public static Packet<ServerNetworkHandler> getC2SPacket(JSONObject o){
        try {
            Object type = o.get(getShortVariableName("type"));
            if (!(type instanceof Number)) {
                return null;
            }
            int typeInt = (int) type;
            if (typeInt < 0 || typeInt >= PacketType.values().length) {
                return null;
            }
            PacketType packetType = PacketType.values()[typeInt];
            return (Packet<ServerNetworkHandler>) packetType.createPacket(o);
        }catch (Exception e){
            e.printStackTrace();
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
    public static void put(JSONObject o, String name, Box value){
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
    public static void putPacketType(JSONObject o, PacketType value){
        o.put(getShortVariableName("type"),value.ordinal());
    }
    public static void putPacketType(JSONObject o, String value){
        o.put(getShortVariableName("type"),value);
    }
    public static Object get(JSONObject o,String name){
        if(o.has(name)){
            return o.get(name);
        }
        return o.get(getShortVariableName(name));
    }
    public static double getDouble(JSONObject o,String name){
        if(o.has(name)){
            return o.getDouble(name);
        }
        return o.getDouble(getShortVariableName(name));
    }
    public static int getInt(JSONObject o,String name){
        if(o.has(name)){
            return o.getInt(name);
        }
        return o.getInt(getShortVariableName(name));
    }
    public static long getLong(JSONObject o,String name){
        if(o.has(name)){
            return o.getLong(name);
        }
        return o.getLong(getShortVariableName(name));
    }
    public static boolean getBoolean(JSONObject o,String name){
        if(o.has(name)) {
            return o.getBoolean(name);
        }
        return o.getBoolean(getShortVariableName(name));
    }
    public static String getString(JSONObject o,String name){
        if(o.has(name)) {
            return o.getString(name);
        }
        return o.getString(getShortVariableName(name));
    }
    public static JSONObject getJSONObject(JSONObject o,String name){
        if(o.has(name)) {
            return o.getJSONObject(name);
        }
        return o.getJSONObject(getShortVariableName(name));
    }
    public static JSONArray getJSONArray(JSONObject o,String name){
        if(o.has(name)) {
            return o.getJSONArray(name);
        }
        return o.getJSONArray(getShortVariableName(name));
    }
    public static Vec2d getVec2d(JSONObject o,String name){
        if(o.has(name)) {
            return Vec2d.fromJSON(o.getJSONObject(name));
        }
        return Vec2d.fromJSON(o.getJSONObject(getShortVariableName(name)));
    }
    public static Box getBox(JSONObject o, String name) {
        if(o.has(name)) {
            return Box.fromJSON(o.getJSONObject(name));
        }
        return Box.fromJSON(o.getJSONObject(getShortVariableName(name)));
    }
    public static boolean contains(JSONObject o,String name){
        if(o.has(name)) {
            return true;
        }
        return o.has(getShortVariableName(name));
    }


}
