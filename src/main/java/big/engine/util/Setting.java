package big.engine.util;

import big.engine.math.Vec2d;

import org.json.JSONObject;

import java.io.Reader;


public class Setting {
    public static String SETTING_PATH="setting.json";
    public static String SERVER_SETTING_PATH="setting_server.json";
    public static Setting INSTANCE=null;
    public JSONObject data;
    public JSONObject serverData=null;
    public Setting(JSONObject obj){
        this.data=obj;
    }
    public String getServerAddress(){
        return data.getString("server_address");
    }
    public int getServerPort(){
        return data.getInt("server_port");
    }
    public String getName(){
        return data.getString("name");
    }
    public String getChosenTank(){
        return data.getString("chosen_tank");
    }
    public boolean isServer(){
        return data.getBoolean("is_server");
    }
    public Vec2d getMouseOffset(){
        return Vec2d.fromJSON(data.getJSONObject("mouse_offset"));
    }
    public void setMouseOffset(Vec2d mouseOffset){
        data.put("mouse_offset",mouseOffset.toJSON());
    }
    public void setChosenTank(String tank) {
        data.put("chosen_tank", tank);
    }
    public double getFps(){
        return data.getDouble("fps");
    }
    //server
    public int getMaxPolygon(){
        return serverData.getInt("max_polygon");
    }
    public int getBotCount(){
        return serverData.getInt("bot_count");
    }
    public int getMaxVisitor(){
        return serverData.getInt("max_visitor");
    }
    public int getVisitorSpawnDelay(){
        return serverData.getInt("visitor_spawn_delay");
    }
    public double getVisitorSpawnPossibility(){
        return serverData.getDouble("visitor_spawn_possibility");
    }
    public int getVisitorSpawnTime(){
        return serverData.getInt("visitor_spawn_time");
    }
    public boolean isSiege(){
        return serverData.getBoolean("is_siege");
    }
    public int getMaxTeam(){
        return serverData.getInt("max_team");
    }
    public int getRandomTickSpeed(){
        return serverData.getInt("random_tick_speed");
    }
    public double getDamageExchangeSpeed(){
        return serverData.getDouble("damage_exchange_speed");
    }
    public int getPolygonSplit(){
        return serverData.getInt("polygon_split");
    }
    public double getPolygonSide(){
        return serverData.getDouble("polygon_side");
    }
    public double getPolygonType(){
        return serverData.getDouble("polygon_type");
    }
    public double getPolygonRandomPow(){
        return serverData.getDouble("polygon_random_pow");
    }
    public void save(){
        try {
            Util.write(SETTING_PATH, data.toString().replaceAll(",",",\n"));
            if(serverData!=null){
                Util.write(SERVER_SETTING_PATH, serverData.toString().replaceAll(",",",\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String create(){
        JSONObject o=new JSONObject();
        o.put("server_address","frp-run.com");
        o.put("server_port",48887);
        o.put("name","Player-"+Util.random.nextInt(1000));
        o.put("chosen_tank","single");
        o.put("is_server",false);
        o.put("mouse_offset", Vec2d.zero().toJSON());
        o.put("fps",60);
        return o.toString();
    }
    public static String createServer(){
        JSONObject o=new JSONObject();
        o.put("max_polygon",300);
        o.put("bot_count",0);
        o.put("max_visitor",1);
        o.put("visitor_spawn_delay",10);
        o.put("visitor_spawn_possibility",0.01);
        o.put("visitor_spawn_time",1000);
        o.put("is_siege",false);
        o.put("max_team",2);
        o.put("random_tick_speed",80);
        o.put("damage_exchange_speed",1);
        o.put("maze_bullet_break",true);
        o.put("polygon_split", true);
        o.put("polygon_side", 3);
        o.put("polygon_type", 4);
        o.put("polygon_random_pow", 2);
        o.put("maze_bullet_rebound",false);
        return o.toString();
    }
    public boolean isMazeBulletBreak(){
        return data.getBoolean("maze_bullet_break");
    }
    public boolean isMazeBulletRebound(){
        return data.getBoolean("maze_bullet_rebound");
    }
    public static void init(){
        String data=Util.read(SETTING_PATH);
        String data2=Util.read(SERVER_SETTING_PATH);
        if(data==null){
            data=create();
        }
        JSONObject obj=new JSONObject(data);
        INSTANCE=new Setting(obj);
        if(INSTANCE.isServer()){
            if(data2==null)data2=createServer();
            INSTANCE.serverData= new JSONObject(data2);
            Util.fixSettings(INSTANCE.serverData,new JSONObject(createServer()));
        }
        Util.fixSettings(INSTANCE.data,new JSONObject(create()));
        INSTANCE.save();
    }

}
