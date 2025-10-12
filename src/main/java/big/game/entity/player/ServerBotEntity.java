package big.game.entity.player;

import big.engine.math.Vec2d;
import big.engine.util.Util;
import big.game.weapon.GunList;

import java.util.ArrayList;
import java.util.Iterator;

public class ServerBotEntity extends ServerPlayerEntity{
    public static String[] names={};
    public AutoController<ServerBotEntity> autoController;
    public ServerBotEntity(Vec2d position,int team) {
        super(position);
        this.weaponID=getRandom();
        this.team=team;
        this.name="Bot-"+this.weaponID+" "+Util.random.nextInt(100);
        autoController=new AutoController<>(this,this.inputManager);
    }
    public void tick(){
        if(this.isAlive)autoController.tick();
        super.tick();
       // this.speed=this.speed*2;
        /*if(!this.isAlive){

            respawn();
            this.weaponID=getRandom();
            this.name="Bot-"+this.weaponID;
            this.weapon = GunList.fromID(this, weaponID);
            System.out.println("respawn");
        }*/
    }
    private String getFixed(int team){
        switch (team){
            case 0:
                return "OverDrive";
            case 1:
                return "Overlord";
            case 2:
                return "shotgun";
            case 3:
                return "MachineGun";
        }
        return "none";
    }
    private String getRandom(){
        ArrayList<String> list=new ArrayList<>();
        for (Iterator<String> it = GunList.data.keys(); it.hasNext(); ) {
            String s = it.next();
            if(s.contains("test")) continue;
            if(s.contains("visitor")) continue;
            //if(s.equals("MachineGun")) continue;
            list.add(s);
        }
        int r= Math.clamp((int) Math.floor(Util.random(0,list.size())),0,list.size()-1);
        return list.get(r);
    }
    public boolean killed(){
        return !isAlive;
    }
}
