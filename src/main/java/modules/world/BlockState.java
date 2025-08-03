package modules.world;

import engine.math.util.PacketUtil;
import org.json.JSONObject;

public class BlockState {
    private Block block;
    private JSONObject data=new JSONObject();
    public BlockState(Block block){
        this.block=block;
        init();
    }
    public void init(){
        data.put("spawnMobRarity",0.1);
        data.put("team",-1);
    }
    public Block getBlock(){
        return block;
    }
    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        PacketUtil.put(json,"block",block.toJSON());
        PacketUtil.put(json,"data",data);
        return json;
    }
    public JSONObject toClientJSON(){
        JSONObject json=new JSONObject();
        PacketUtil.put(json,"block",block.toJSON());
        PacketUtil.put(json,"data",data);
        return json;
    }
    public static BlockState fromJSON(JSONObject o){
        JSONObject block=PacketUtil.getJSONObject(o,"block");
        BlockState b=new BlockState(Blocks.blocks_id.get(PacketUtil.getInt(block,"id")));
        try {
            b.data = PacketUtil.getJSONObject(o, "data");
        }catch (Exception e){}
        return b;
    }
    public double getSpawnMobRarity(){
        return PacketUtil.getDouble(data,"spawnMobRarity");
    }
    public void setSpawnMobRarity(double rarity){
        PacketUtil.put(data,"spawnMobRarity",rarity);
    }
    public int getTeam(){
        return PacketUtil.contains(data,"team0")? PacketUtil.getInt(data,"team0"):-1;
    }
    public void setTeam(int team){
        PacketUtil.put(data,"team0",team);
    }
}
