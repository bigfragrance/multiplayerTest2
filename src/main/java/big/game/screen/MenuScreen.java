package big.game.screen;

import big.engine.math.Box;
import big.engine.math.Direction;
import big.engine.math.Vec2d;
import big.engine.modules.EngineMain;
import big.engine.render.Screen;
import big.engine.util.Getter;
import big.engine.util.Util;
import big.events.MouseClickEvent;
import big.events.RenderEvent;
import big.events.TickEvent;
import big.game.entity.DominatorEntity;
import big.game.entity.Entity;
import big.game.entity.EntityType;
import big.game.weapon.GunList;
import big.game.world.Block;
import big.game.world.WorldEditMode;
import big.game.world.Blocks;
import big.game.world.blocks.BaseBlock;
import big.game.world.blocks.PushBlock;
import big.server.ClientHandler;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.renderFix;
import static big.engine.render.Screen.sc;
import static big.game.entity.Entity.sizeMultiplier;

public class MenuScreen {
    public static MenuScreen INSTANCE=null;
    public static int textSize=16;
    public static int lineGap=28;
    public ArrayList<Button> currentItems=new ArrayList<>();
    public WorldEditMode currentMode=new WorldEditMode();
    public Stack<Button> backButtons=new Stack<>();
    public Button homeButton=new Button("Home",()->{
        clearItems();
        initItems();
        backButtons.clear();
    });
    public int selectedItem=-1;
    public static void init(){
        INSTANCE=new MenuScreen();
        INSTANCE.homeButton.onClick();
    }
    public MenuScreen(){
        cs.EVENT_BUS.subscribe(this);
        System.out.println("MenuScreen init");
    }
    @EventHandler
    public void onRender(RenderEvent event){
        sc.storeAndSetDef();
        Vec2d pos= getMenuRenderPosition0();
        for(int i=0;i<currentItems.size();i++){
            event.g.setColor(Color.blue);
            String s=currentItems.get(i).getText();
            Util.renderString(event.g,s,pos,Util.round(textSize* sc.zoom*sizeMultiplier),false);
            if(i==selectedItem){
                FontMetrics fm = event.g.getFontMetrics();
                int width = fm.stringWidth(s);
                int height = fm.getHeight()-fm.getDescent();
                event.g.setColor(Color.RED);
                Util.renderCubeLine(event.g,new Box(pos.x,pos.x+width,pos.y- (double) height /2,pos.y+ (double) height /2));
            }

            pos.y+=lineGap/sc.zoom2;
        }

        sc.restoreZoom();
    }
    @EventHandler
    public void onTick(TickEvent event){
        if(event.isPre()) return;
        Vec2d pos = getMenuRenderPosition();
        Vec2d mousePos=Screen.mousePosJF;
        selectedItem=Util.ceil((mousePos.y-pos.y)/lineGap);
        if(selectedItem<0||selectedItem>=currentItems.size()||mousePos.x<pos.x) selectedItem=-1;

    }
    @EventHandler
    public void onMouseClick(MouseClickEvent event){
        if(event.button==1){
            if(selectedItem>=0){
                Button b=currentItems.get(selectedItem);
                if(b.shouldClearItems())clearItems();
                b.onClick();
                if(b.shouldClearItems())addHomeButton();
                if(currentItems.size()<=1){
                    homeButton.onClick();
                }
                backButtons.push(b);
            }
        }
    }
    public void addItem(String text,Runnable action){
        currentItems.add(new Button(text,action));
    }
    public void addItem(Getter<String> text,Runnable action){
        currentItems.add(new Button(text,action,true));
    }
    private void addItem(String s,Runnable r, boolean b) {
        addItem(new Button(s,r,b));
    }
    private void addItem(Getter<String> s, Runnable r, boolean b) {
        addItem(new Button(s,r,b));
    }
    public void addItem(Button name){
        currentItems.add(name);
    }
    public void clearItems(){
        currentItems.clear();
    }
    public void addHomeButton(){
        addItem(homeButton);
    }
    private Vec2d getMenuRenderPosition(){
        return Screen.SCREEN_BOX.getMaxXMinY().add(-500,100);
    }
    private Vec2d getMenuRenderPosition0(){
        return Screen.SCREEN_BOX.getMaxXMinY().add(-500,100).subtract(sc.getMiddle()).multiply(renderFix).add(sc.getMiddle());
    }
    public void initItems(){
        addItem("Block",()->{
            for(Block b:Blocks.blocks_id.values()){
                addItem(b.name,()->currentMode.setBlock(b));
            }
        });
        addItem("BlockData",()->{
            addItem("BaseBlock",()->{
                for(int i=-1;i<=3;i++){
                    int finalI = i;
                    addItem(()->"Base's Team "+finalI+(currentMode.getCurrentTeam()==finalI?" <--":""),()->currentMode.setCurrentTeam(finalI),false);
                }
                if(!currentMode.data.has("damage")) currentMode.data.put("damage",false);
                addItem(()->"Switch CanDamage:"+currentMode.data.getBoolean("damage"),()->currentMode.data.put("damage",!currentMode.data.getBoolean("damage")),false);
                addItem("Apply",()->{
                    currentMode.setTask((state)->{
                        state.setTeam(currentMode.data.getInt("team"));
                        BaseBlock.setDealDamage(state,currentMode.data.getBoolean("damage"));
                    });
                },false);
            });
            addItem("PushBlock",()->{
                for(Direction dir:Direction.values()){
                    addItem("Direction "+dir.getName(),()->currentMode.setTask(state-> PushBlock.setPushDirection(state,dir)));
                }
            });
        });
        addItem("PlaceMode",()->{
            addItem("SpawnMobRarity",()->{
                addItem(()->"Current(Click to input):"+String.format("%.2f",currentMode.spawnMobRarity),()->{
                    Thread t=new Thread(()->{
                        currentMode.spawnMobRarity=Double.parseDouble(InputDialog.getInputFromDialog("SpawnMobRarity"));
                    });
                    t.start();
                },false);
            });
            addItem("PlaceRadius",()->{
                addItem(()->"Current(Click to input):"+String.format("%.2f",cs.serverController.currentPlaceRadius),()->{
                    Thread t=new Thread(()->{
                        cs.serverController.currentPlaceRadius=Double.parseDouble(InputDialog.getInputFromDialog("PlaceRadius"));
                    });
                    t.start();
                },false);
            });
        });

        addItem(()->"Entity("+currentMode.getCurrentEntity()+")",()->{
            addItem("Polygon",()->{
                addItem(()->"CurrentType:"+currentMode.getCurrentPType(),()->{
                    currentMode.setCurrentPType(Integer.parseInt(InputDialog.getInputFromDialog("PolygonType")));
                },false);
                addItem(()->"CurrentSide:"+currentMode.getCurrentPSide(),()->{
                    currentMode.setCurrentPSide(Integer.parseInt(InputDialog.getInputFromDialog("PolygonSide")));
                },false);
                addItem("Select",()->{
                    currentMode.setCurrentEntity("POLYGON");
                });
            });
            addItem("Dominator",()->{
                for(String name: DominatorEntity.dominatorTypes){
                    addItem(()->name+(currentMode.getDominatorType().equals(name)?" <--":""),()->currentMode.setDominatorType(name),false);
                }
                addItem("Select",()->{
                    currentMode.setCurrentEntity("DOMINATOR");
                });
            });
            addItem(()->"CenterPlacing:"+currentMode.isCenterPlacing(),()->currentMode.setCenterPlacing(!currentMode.isCenterPlacing()),false);
        });
        addItem("World",()->{
            addItem("Save",()->{
                addItem("AreYouSureToSave?",()->{

                },false);
                addItem("Yes",()->{
                    cs.serverController.saveWorld();
                });
                addItem("Cancel",()->{
                });
            });
            addItem("Load",()->{
                cs.serverController.loadWorld();
            });
            addItem("Reload",()->{
                cs.serverController.reload();
            });
        });
        addItem("WorldEditMode",()->{
            addItem("Enable",()->{
                EngineMain.isWorldEditMode=true;
            });
            addItem("Disable",()->{
                EngineMain.isWorldEditMode=false;
            });
        });
    }

}
