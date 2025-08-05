package modules.screen;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.PacketVariable;
import engine.render.Screen;
import modules.ctrl.InputManager;
import modules.entity.player.PlayerEntity;
import modules.network.packet.c2s.UpdateWeaponC2SPacket;
import modules.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import modules.weapon.GunList;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import static engine.modules.EngineMain.cs;
import static engine.render.Screen.mousePos;
import static engine.render.Screen.sc;

public class TankChooseScreen extends GUI {
    public static TankChooseScreen INSTANCE=new TankChooseScreen();
    public static long staticID=1234561451;
    public static Vec2d start=new Vec2d(-5,4);
    public static double distance=1.5;
    public static int tankPerPage=9;
    public static JSONObject tanksList=new JSONObject();
    public int currentIndex=0;
    public ArrayList<PlayerEntity> toShow=new ArrayList<>();
    public double sizeMultiplier=1;
    public TankChooseScreen(){
    }
    public void tick(){
        sizeMultiplier=cs.player==null?1: cs.player.getFov();
        for(int i=0;i<toShow.size();i++){
            PlayerEntity player=toShow.get(i);
            player.prevPosition.set(player.position);
            player.prevBoundingBox=player.boundingBox.copy();
            player.prevRotation=player.rotation;
            player.position=(getPos(i%tankPerPage));
            player.boundingBox=new Box(player.position,PlayerEntity.SIZE*sizeMultiplier);
            player.rotation+=10;
            if(player.weapon!=null)player.weapon.setSize(sizeMultiplier);
        }
        if(Screen.isKeyClinked(Screen.MOUSECHAR)){
            Vec2d mouse=sc.inputManager.getMouseVec().add(cs.getCamPos());
            for(int i=currentIndex*tankPerPage;i<(currentIndex+1)*tankPerPage;i++){
                if(i>=toShow.size())break;
                PlayerEntity player=toShow.get(i);
                if(player.position.distanceTo(mouse)<0.5*(sizeMultiplier)){
                    if(player.name.equals("PageBack")){
                        currentIndex--;
                        if(currentIndex<0) currentIndex=0;
                        break;
                    }
                    if(player.name.equals("PageNext")){
                        currentIndex++;
                        if(currentIndex>=toShow.size()/tankPerPage) currentIndex=toShow.size()/tankPerPage-1;
                        break;
                    }
                    if(player.name.equals("Close")){
                        sc.closeScreen();
                        break;
                    }
                    cs.networkHandler.sendPacket(new UpdateWeaponC2SPacket(player.name));
                    cs.setting.setChosenTank(player.name);
                    cs.setting.save();
                    sc.closeScreen();
                }
            }
        }
    }
    public void render(Graphics g){
        for(int i=currentIndex*tankPerPage;i<(currentIndex+1)*tankPerPage;i++){
            if(i>=toShow.size())break;
            PlayerEntity player=toShow.get(i);
            player.render(g);
        }
    }
    public void init(){
        int i=0;
        int j=0;
        for(String s:tanksList.keySet()){
            j++;
            if(Objects.equals(s, PacketVariable.type)) continue;
            PlayerEntity player=null;
            try{
                JSONObject o=tanksList.getJSONObject(s);
                PlayerEntity playerEntity=new PlayerEntity(getPos(i));
                cs.entities.put(staticID,playerEntity);
                playerEntity.id=staticID;
                playerEntity.rotation=0;
                playerEntity.prevRotation=0;
                playerEntity.weapon= GunList.fromJSONServer(playerEntity,o);
                playerEntity.name=s;
                playerEntity.team=0;
                player=playerEntity;
            }catch (Exception e){
                e.printStackTrace();
            }
            cs.entities.remove(staticID);
            if(player!=null){
                toShow.add(player);
                i++;
                if(i%(tankPerPage-3)==0||j==tanksList.keySet().size()){
                    PlayerEntity playerEntity=new PlayerEntity(getPos(i));
                    playerEntity.id=staticID;
                    playerEntity.rotation=0;
                    playerEntity.prevRotation=0;
                    playerEntity.name="PageBack";
                    playerEntity.team=1;
                    toShow.add(playerEntity);
                    i++;
                    playerEntity=new PlayerEntity(getPos(i));
                    playerEntity.id=staticID;
                    playerEntity.rotation=0;
                    playerEntity.prevRotation=0;
                    playerEntity.name="PageNext";
                    playerEntity.team=1;
                    toShow.add(playerEntity);
                    i++;
                    playerEntity=new PlayerEntity(getPos(i));
                    playerEntity.id=staticID;
                    playerEntity.rotation=0;
                    playerEntity.prevRotation=0;
                    playerEntity.name="Close";
                    playerEntity.team=2;
                    toShow.add(playerEntity);
                    i=0;
                }
            }
        }
    }
    private Vec2d getPos(int index){
        return cs.camPos.add(start.add(index*distance,0).multiply(sizeMultiplier));
    }
}
