package modules.entity.player;

import engine.math.Vec2d;
import engine.math.util.Util;
import modules.weapon.GunList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static engine.modules.EngineMain.cs;

public class ServerBotEntity extends ServerPlayerEntity{
    public static String[] names={};
    public AutoController autoController;
    public ServerBotEntity(Vec2d position) {
        super(position);
        this.weaponID=getRandom();

        this.name="Bot-"+this.weaponID;
        autoController=new AutoController(this,this.inputManager);
    }
    public void tick(){
        autoController.tick();
        super.tick();
       // this.speed=this.speed*2;
        if(!this.isAlive){
            cs.removeEntity(this);
        }
    }
    private String getRandom(){
        ArrayList<String> list=new ArrayList<>();
        for (Iterator<String> it = GunList.data.keys(); it.hasNext(); ) {
            String s = it.next();
            if(s.equals("test")) continue;
            list.add(s);
        }
        int r= Math.clamp((int) Math.floor(Util.random(0,list.size())),0,list.size()-1);
        return list.get(r);
    }
}
