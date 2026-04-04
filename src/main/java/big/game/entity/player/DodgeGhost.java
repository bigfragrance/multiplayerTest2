package big.game.entity.player;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.render.Screen;
import big.engine.util.ColorUtils;
import big.engine.util.EntityUtils;
import big.engine.util.Util;
import big.game.entity.Entity;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.engine.util.EntityUtils.*;
import static big.engine.util.Util.round;
import static big.game.entity.Entity.sizeMultiplier;

public class DodgeGhost {
    public Box box;
    public double rotation;
    public int tick;
    public DodgeGhost(Box box,double rotation){
        this.box=box;
        this.rotation=rotation;
        this.tick=0;
    }
    public void tick(){
        tick++;
    }
    public void render(Graphics g,PlayerEntity e){
        Vec2d lp=e.position;
        Vec2d lpp=e.prevPosition;
        Box lb=e.boundingBox;
        Box lpb=e.prevBoundingBox;
        double lr=e.rotation;
        double lpr=e.prevRotation;

        Vec2d np=box.getCenter();
        e.position=np;
        e.prevPosition=np;
        e.boundingBox=box;
        e.prevBoundingBox=box;
        e.rotation=rotation;
        e.prevRotation=rotation;
        e.tempSwitchRender=true;

        if(e.weapon!=null) e.weapon.render(g);

        Color team=EntityUtils.getTeamcolor(e.team);
        team=ColorUtils.setAlpha(team,getAlpha(e));
        g.setColor(ColorUtils.darker(team, 0.6));
        Util.render(g, box.switchToJFrame());
        g.setColor(team);
        Util.render(g, smaller(box).switchToJFrame());

        if(e.weapon!=null) e.weapon.renderAfter(g);

        g.setColor(ColorUtils.setAlpha(Color.DARK_GRAY,getAlpha(e)));
        Vec2d renderPos=box.getCenter().add(0,30*sizeMultiplier*e.getSizeMultiplier());
        Util.renderString(g,e.name,renderPos.switchToJFrame(),round(nameSize* sc.zoom*sizeMultiplier*e.getSizeMultiplier()));

        Vec2d scoreRenderPos=e.getRenderPosition().add(0,40*sizeMultiplier*e.getSizeMultiplier());
        Util.renderString(g, String.valueOf(round(e.score)),scoreRenderPos.switchToJFrame(),round(scoreSize* sc.zoom*sizeMultiplier*e.getSizeMultiplier()));

        e.position=lp;
        e.prevPosition=lpp;
        e.boundingBox=lb;
        e.prevBoundingBox=lpb;
        e.rotation=lr;
        e.prevRotation=lpr;
        e.tempSwitchRender=false;
    }
    public double getAlpha(Entity e){
        return Math.clamp((40-tick+e.getTickDelta())/30d,0,1);
    }
    public boolean isExpired(){
        return tick>40;
    }
}
