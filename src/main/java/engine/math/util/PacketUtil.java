package engine.math.util;

public class PacketUtil {
    public static String getShortString(String name){
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
        }
        return name;
    }
}
