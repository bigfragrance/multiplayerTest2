package big.game.world;

import big.engine.math.Vec2d;
import big.engine.math.Vec2i;
import big.engine.util.AfterCheckTask;
import big.game.entity.DominatorEntity;
import big.game.entity.PolygonEntity;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class WorldEditMode {
    public Block currentBlock=Blocks.AIR;
    public AfterCheckTask<BlockState> task=null;
    public double spawnMobRarity=0.1;
    public JSONObject data=new JSONObject();
    public WorldEditMode(){

    }
    public void setTask(AfterCheckTask<BlockState> task){
        this.task=task;
    }
    public void check(BlockState state){
        if(task!=null){
            task.run(state);
        }
    }
    public void setBlock(Block block){
        currentBlock=block;
    }
    public Block getBlock(){
        return currentBlock;
    }
    public void apply(Vec2i pos){
        if(currentBlock!=Blocks.AIR){
            BlockState state=new BlockState(currentBlock);
            if(task!=null)task.run(state);
            cs.world.setBlockState(pos.x,pos.y,state);
        }else{
            BlockState state=new BlockState(Blocks.AIR);
            //state.setSpawnMobRarity(spawnMobRarity);
            cs.world.setBlockState(pos.x,pos.y,state);
        }
    }
    public void putEntity(Vec2d pos){
        if(isCenterPlacing()){
            pos=Vec2i.ofFloor(pos).toCenterPos();
        }
        switch(getCurrentEntity()){
            case("POLYGON")->{
                PolygonEntity entity=new PolygonEntity(pos,getCurrentPSide(),getCurrentPType());
                cs.addEntity(entity);
            }
            case("DOMINATOR")->{
                DominatorEntity entity=new DominatorEntity(pos,getCurrentTeam(),getDominatorType(),getDominatorType());
                cs.addEntity(entity);
                System.out.println("1");
            }
        }
    }
    public Runnable getCheckRunnable(BlockState state){
        return ()->check(state);
    }
    //-----------------------------------------------------------------------
    public int getCurrentTeam(){
        if(!data.has("team")) data.put("team",0);
        return data.getInt("team");
    }
    public void setCurrentTeam(int team){
        data.put("team",team);
    }
    public int getCurrentPType(){
        if(!data.has("ptype")) data.put("ptype",0);
        return data.getInt("ptype");
    }
    public void setCurrentPType(int ptype){
        data.put("ptype",ptype);
    }
    public int getCurrentPSide(){
        if(!data.has("pside")) data.put("pside",0);
        return data.getInt("pside");
    }
    public void setCurrentPSide(int pside){
        data.put("pside",pside);
    }
    public void setCurrentEntity(String type){
        data.put("entity",type);
    }
    public String getCurrentEntity() {
        if (!data.has("entity")) data.put("entity", "POLYGON");
        return data.getString("entity");
    }
    public boolean isCenterPlacing(){
        if(!data.has("centerPlacing")) data.put("centerPlacing",false);
        return data.getBoolean("centerPlacing");
    }
    public void setCenterPlacing(boolean centerPlacing){
        data.put("centerPlacing",centerPlacing);
    }
    public String getDominatorType(){
        if(!data.has("dominatorType")) data.put("dominatorType","");
        return data.getString("dominatorType");
    }
    public void setDominatorType(String dominatorType){
        data.put("dominatorType",dominatorType);
    }
}
