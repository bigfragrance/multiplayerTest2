package big.game.entity.player;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.EntityUtils;
import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.engine.render.Screen;
import big.game.ctrl.InputManager;
import big.game.ctrl.ServerInputManager;
import big.game.network.packet.c2s.MessageC2SPacket;
import big.game.screen.InputDialog;
import big.game.screen.TankChooseScreen;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class ClientPlayerEntity extends PlayerEntity {
    public static Thread thread=null;
    public ServerInputManager serverInputManager=new ServerInputManager();
    public InputManager inputManager= sc.inputManager;
    protected int playerDataUpdateTimer=10;
    public int skillPointCanUse=0;
    public String skillPointNext="0/0";
    public String currentWeapon="none";
    public int lastAutoFire=0;
    public boolean autoFire=false;
    private Vec2d prevMousePos=new Vec2d(0,0);
    public ClientPlayerEntity(Vec2d position) {
        super(position);
        inputManager= sc.inputManager;
    }
    public void tick(){
        this.name=cs.setting.getName();
        playerDataUpdateTimer--;
        if(playerDataUpdateTimer<0) {
            cs.networkHandler.sendPlayerData(this);
            playerDataUpdateTimer=100000000;
        }
        super.tick();
        cs.updateCamPos();
        sc.zoom=(Screen.defZoom)/this.getFov();
        Screen.tickDelta=0;
        updateInput();
        if(!isAlive) {
            if(inputManager.isRespawning()){
                cs.networkHandler.sendPlayerRespawn();
                sc.setScreen(TankChooseScreen.INSTANCE);
            }
        }
        prevMousePos=inputManager.getMouseVec();
    }
    private void updateInput(){
        updateSkillPoint();
        int[] input = inputManager.getPlayerInput();
        if(inputManager.enableAutoFire()&&lastAutoFire<=0){
            autoFire=!autoFire;
            lastAutoFire=10;
        }
        lastAutoFire--;
        serverInputManager.forward=input[1];
        serverInputManager.side=input[0];
        serverInputManager.aimPos=inputManager.getMouseVec();
        serverInputManager.shoot= sc.currentScreen==null&& (inputManager.isShooting()||autoFire);
        serverInputManager.defend=sc.currentScreen==null&&inputManager.isDefending();
        serverInputManager.sendUpdate();

        if(inputManager.isOpeningSendMsg()){
            if(thread==null||!thread.isAlive()) {
                thread = new Thread(() -> {
                    String msg = InputDialog.getInputFromDialog("send message");
                    if (msg != null && !msg.isEmpty()) {
                        cs.networkHandler.send(new MessageC2SPacket(msg));
                    }
                });
                thread.start();
                inputManager.unFocus();
            }
        }
    }
    private void updateSkillPoint(){
        serverInputManager.upgradingSkill=-1;
        for(int i=0;i<skillPoints.length;i++){
            if(inputManager.isUpgrading(i)){
                serverInputManager.upgradingSkill=i;
                break;
            }
        }
    }

    public void render(Graphics g){
        super.render(g);
        EntityUtils.renderSkillPoints(getSkillPointRenderPosition(),skillPoints,skillPointCanUse,skillPointNext);
        //renderMouse(g);
    }
    private void renderMouse(Graphics g){
        Graphics2D g2d=(Graphics2D) g;
        //g2d.setStroke(new BasicStroke((float) (1f)));
        Vec2d center=Util.lerp(prevMousePos,inputManager.getMouseVec(),Screen.tickDelta);
        Vec2d off=new Vec2d(0.1*getFov(),0.1*getFov());
        Util.renderLine(g,center.add(off).switchToGame1(),center.add(off.multiply(-1)).switchToGame1());
        Util.renderLine(g,center.add(off.rotate(90)).switchToGame1(),center.add(off.rotate(-90)).switchToGame1());
    }
    private Vec2d getSkillPointRenderPosition(){
        return Screen.SCREEN_BOX.getMaxPos().add(-100,-50).subtract(sc.getMiddle()).multiply(1/sc.zoom2).add(sc.getMiddle());
    }
    public static ClientPlayerEntity fromJSON(JSONObject o){
        JSONObject basic=o.getJSONObject(PacketUtil.getShortVariableName("basic"));
        ClientPlayerEntity e=new ClientPlayerEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("position"))));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.boundingBox= Box.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox")));
        e.update(o);
        return e;
    }

}
