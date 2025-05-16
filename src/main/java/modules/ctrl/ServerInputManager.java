package modules.ctrl;

import engine.math.Vec2d;
import modules.network.packet.c2s.PlayerInputC2SPacket;

import static engine.modules.EngineMain.cs;

public class ServerInputManager {
    public int forward;
    public int side;
    public Vec2d aimPos;
    public boolean shoot;
    public int upgradingSkill;
    public ServerInputManager(){
        this.forward=0;
        this.side=0;
        this.aimPos=new Vec2d(0,0);
        this.shoot=false;
        this.upgradingSkill=-1;
    }
    public void sendUpdate(){
        cs.networkHandler.send(new PlayerInputC2SPacket(forward,side,aimPos,shoot,upgradingSkill));
    }
}
