package big.game.ctrl;

import big.engine.math.Vec2d;
import big.game.network.packet.c2s.PlayerInputC2SPacket;

import static big.engine.modules.EngineMain.cs;

public class ServerInputManager {
    public int forward;
    public int side;
    public Vec2d aimPos;
    public boolean shoot;
    public boolean defend;
    public int upgradingSkill;
    public ServerInputManager(){
        this.forward=0;
        this.side=0;
        this.aimPos=new Vec2d(0,0);
        this.shoot=false;
        this.upgradingSkill=-1;
        this.defend=false;
    }
    public void sendUpdate(){
        cs.networkHandler.send(new PlayerInputC2SPacket(forward,side,aimPos,shoot,defend,upgradingSkill));
    }
}
