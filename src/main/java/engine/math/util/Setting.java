package engine.math.util;

import org.json.JSONObject;

import static engine.modules.EngineMain.SETTING_PATH;

public class Setting {
    public JSONObject data;
    public Setting(String str){
        this.data=new JSONObject(str);
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
    public void setChosenTank(String tank) {
        data.put("chosen_tank", tank);
    }
    public void save(){
        try {
            Util.write(SETTING_PATH, data.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static String create(){
        JSONObject o=new JSONObject();
        o.put("server_address","frp-sea.com");
        o.put("server_port",48887);
        o.put("name","Player-"+Math.ceil(Math.random()*1000));
        o.put("chosen_tank","single");
        o.put("is_server",false);
        return o.toString();
    }
}
