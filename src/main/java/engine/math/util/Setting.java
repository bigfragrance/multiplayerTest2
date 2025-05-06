package engine.math.util;

import org.json.JSONObject;

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
    public int getChosenTank(){
        return data.getInt("chosen_tank");
    }
    public boolean isServer(){
        return data.getBoolean("is_server");
    }
    public static String create(){
        JSONObject o=new JSONObject();
        o.put("server_address","frp-sea.com");
        o.put("server_port",48887);
        o.put("name","Player-"+Math.ceil(Math.random()*1000));
        o.put("chosen_tank",0);
        o.put("is_server",false);
        return o.toString();
    }
}
