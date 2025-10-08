package big.game.server;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.test.MazeGenerator;
import big.engine.math.test.MazeGenerator2;
import big.engine.math.util.AfterCheckTask;
import big.engine.math.util.Util;
import big.engine.math.util.timer.IntTimer;
import big.engine.math.util.timer.Timer;
import big.engine.math.util.timer.TimerList;
import big.engine.modules.EngineMain;
import big.game.ctrl.InputManager;
import big.game.entity.Entity;
import big.game.entity.RockEntity;
import big.game.entity.bullet.BulletEntity;
import big.game.entity.bullet.BulletType;
import big.game.network.packet.s2c.TanksDataS2CPacket;
import big.game.weapon.GunList;
import big.game.world.blocks.Block;
import big.game.world.BlockState;
import big.game.world.Blocks;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static java.lang.Math.floor;

public class ServerController {
    public static ServerController sctrl=new ServerController();
    public TimerList timers=new TimerList();
    private Timer mazeGenTimer=timers.add(new IntTimer(5));
    public InputManager inputManager;
    public double blockSize=1;
    public int mazeSize=48;
    public static double off=0.000001;
    private Vec2d lastMousePos=null;
    private Timer saveTimer=timers.add(new IntTimer(5));
    private Timer actionTimer=timers.add(new IntTimer(5));
    private boolean showingCurrentBlock=false;
    public double currentRarity=0.1;
    private double currentPlaceRadius=1;
    public ServerController(){
        inputManager= sc.inputManager;
        //loadWorld();
    }
    public void update(){
        timers.update();
        if(inputManager.isTickSpeeding()){
            EngineMain.TPS =10000000;
        }else{
            EngineMain.TPS =20;
        }
        if(inputManager.isReloading()){
            GunList.init();
            for(Entity e:cs.world.getEntities()){
                e.kill();
            }
            cs.multiClientHandler.clients.forEach(clientHandler -> clientHandler.send(new TanksDataS2CPacket(GunList.data,GunList.presetData).toJSON())); ;
        }
        if(inputManager.isGeneratingMaze()&&mazeGenTimer.passed()){
            mazeGenTimer.reset();
            generateMaze();
        }
        if(inputManager.isSaving()&&saveTimer.passed()){
            saveTimer.reset();
            saveWorld();
        }
        if(inputManager.isLoading()&&saveTimer.passed()){
            saveTimer.reset();
            loadWorld();
        }
        if(actionTimer.passed()&&inputManager.isChangingShowingCurrentBlock()){
            showingCurrentBlock=!showingCurrentBlock;
            actionTimer.reset();
        }
        if(inputManager.isIncreasingMobRarity()){
            currentRarity+=0.02;
            sc.renderString="currentRarity: "+Util.getRoundedDouble(currentRarity,3);
        }
        if(inputManager.isDecreasingMobRarity()){
            currentRarity-=0.02;
            sc.renderString="currentRarity: "+Util.getRoundedDouble(currentRarity,3);
        }
        if(inputManager.isIncreasingPlaceRadius()){
            currentPlaceRadius+=0.1;
            sc.renderString="currentRadius: "+Util.getRoundedDouble(currentPlaceRadius,3);
        }
        if(inputManager.isDecreasingPlaceRadius()){
            currentPlaceRadius-=0.1;
            sc.renderString="currentRadius: "+Util.getRoundedDouble(currentPlaceRadius,3);
        }

        Vec2d mousePos=inputManager.getMouseVec().add(cs.getCamPos());
        if(lastMousePos==null) lastMousePos=mousePos;
        if(showingCurrentBlock){
            BlockPos blockPos= BlockPos.ofFloor(mousePos);
            sc.renderTasks2.add(g->{
                Blocks.TEST.render(g,null,blockPos.x,blockPos.y);
                BlockPos last=null;
                for(double d=0;d<=1;d+=0.01){
                    Vec2d pos= Util.lerp(lastMousePos,mousePos,d);
                    BlockPos p= BlockPos.ofFloor(pos);
                    if(p.equals(last)) continue;
                    last=p;
                    Blocks.TEST.render(g,null,p.x,p.y);
                }
                g.setColor(new Color(80,80,80,100));
                for(int i=-10;i<=10;i++){
                    for(int j=-10;j<=10;j++){
                        Box b=new Box(i+blockPos.x,i+1+blockPos.x,j+blockPos.y,j+1+blockPos.y);
                        Util.renderCubeLine(g,b.switchToJFrame());
                    }
                }
            });
        }
        if(!inputManager.isLocking()) {
            if (inputManager.isPlacingMaze()) {
                setBlock(lastMousePos == null ? mousePos : lastMousePos, mousePos, Blocks.STONE,currentPlaceRadius);
            }
            if(inputManager.isPlacingBase()){
                BlockState state=new BlockState(Blocks.BASE_BLOCK);
                state.setTeam((int)Math.floor(currentRarity));
                setBlock(lastMousePos == null ? mousePos : lastMousePos, mousePos,state,currentPlaceRadius);
                put(mousePos,currentPlaceRadius,blockState -> blockState.setTeam((int)Math.floor(currentRarity)));
            }
            if (inputManager.isRemovingMaze()) {
                setBlock(lastMousePos == null ? mousePos : lastMousePos, mousePos, Blocks.AIR,currentPlaceRadius);
            }
            if(inputManager.isPuttingMobRarity()){
                put(mousePos,currentPlaceRadius,blockState -> blockState.setSpawnMobRarity(currentRarity));
            }
            lastMousePos = mousePos;
        }
        if(inputManager.isSpawningBullet()){
            for(int i=0;i<10;i++) {
                cs.addEntity(new BulletEntity(mousePos, Util.randomVec().limit(0.5), 5, BulletType.KILLER));
            }
            //cs.addEntity(new RockEntity(mousePos,currentPlaceRadius));
        }
        int[] in=inputManager.getPlayerInput();
        cs.prevCamPos.set(cs.camPos);
        cs.camPos.offset(in[0]*10/sc.getRealZoom(),in[1]*10/sc.getRealZoom());
    }
    public void saveWorld(){
        Util.write("world.txt",cs.world.toJSON().toString());
        /*File world=new File("world.txt");
        String settingData=cs.world.toJSON().toString();
        if(!world.exists()){
            try {
                world.createNewFile();
                settingData= Setting.create();
                Files.write(world.toPath(),settingData.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                Files.write(world.toPath(),settingData.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }
    public void loadWorld(){
        try{
            String data= Util.read("world.txt");
            cs.world.fromJSON(new JSONObject(data));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setBlock(Vec2d start, Vec2d end, Block block,double radius){
        int r= (int) Math.ceil(radius);
        for(double d=0;d<=1;d+=0.1){
            Vec2d pos= Util.lerp(start,end,d);
            for(int x=-r;x<=r;x++){
                for(int y=-r;y<=r;y++){
                    Vec2d p=pos.add(x,y);
                    BlockPos bPos=BlockPos.ofFloor(p);
                    if(bPos.toCenterPos().distanceTo(pos)<=radius){
                        if(cs.world.getBlock(bPos)!=block){
                            cs.world.setBlockState(bPos.x,bPos.y,new BlockState(block));
                        }
                    }
                }
            }
        }
    }
    public void setBlock(Vec2d start, Vec2d end, BlockState state,double radius){
        int r= (int) Math.ceil(radius);
        for(double d=0;d<=1;d+=0.1){
            Vec2d pos= Util.lerp(start,end,d);
            for(int x=-r;x<=r;x++){
                for(int y=-r;y<=r;y++){
                    Vec2d p=pos.add(x,y);
                    BlockPos bPos=BlockPos.ofFloor(p);
                    if(bPos.toCenterPos().distanceTo(pos)<=radius){
                        cs.world.setBlockState(bPos.x,bPos.y,state);
                    }
                }
            }
        }
    }
    public void generateMaze(){
        MazeGenerator2 maze=new MazeGenerator2(mazeSize*2+1,mazeSize*2+1);

        //List<Double> placed=new ArrayList<>();
        for(int x=0;x<mazeSize*2+1;x++){
            for(int y=0;y<mazeSize*2+1;y++){
                cs.world.setBlockState(x-mazeSize,y-mazeSize,new BlockState(isWall(x,y,maze.maze)? Blocks.STONE:Blocks.AIR));
            }
        }
    }
    public void put(Vec2d center, double radius, AfterCheckTask<BlockState> task) {
        int r= (int) Math.ceil(radius);
        for(int x=-r;x<=r;x++){
            for(int y=-r;y<=r;y++){
                Vec2d pos=center.add(x,y);
                BlockPos bPos=BlockPos.ofFloor(pos);
                if(bPos.toCenterPos().distanceTo(center)<=radius){
                    task.run(cs.world.getBlockState(bPos));
                }
            }
        }
    }

    /*public BlockEntity get(int x,int y,List<Double> placed){
        for(int r=maxMerge;r>=0;r--){
            for(int i=0;i<=r;i++){
                int x1=x+i;
                int y1=y+r-i;
                if(canMerge(x,y,x1,y1)){
                    Vec2d center=new Vec2d(x+x1-mazeSize*2,y+y1-mazeSize*2).multiply(blockSize);
                    putAllPlaced(x,y,x1,y1,placed);
                    return new BlockEntity(new Box(center,blockSize*(x1-x+1),blockSize*(y1-y+1)));
                }
            }
        }
        return null;
    }
    public boolean canMerge(int fromX,int fromY,int toX,int toY){
        for(int x=Math.min(fromX,toX);x<=Math.max(fromX,toX);x++){
            for(int y=Math.min(fromY,toY);y<=Math.max(fromY,toY);y++){
                if(!isWall(x,y)) return false;
            }
        }
        return true;
    }
    public void putAllPlaced(int fromX,int fromY,int toX,int toY,List<Double> placed){
        for(int x=Math.min(fromX,toX);x<=Math.max(fromX,toX);x++){
            for(int y=Math.min(fromY,toY);y<=Math.max(fromY,toY);y++){
                placed.add((x+off)*(y+off));
            }
        }
    }*/
    public boolean isWall(int x,int y,int[][] maze){
        if(x<0||y<0||x>=maze.length||y>=maze[0].length) return false;
        return maze[x][y]==MazeGenerator.WALL;
    }
    public boolean isWall(double x,double y,int[][] maze){
        int rX=(int) floor(0.5*x/blockSize+0.5)+mazeSize;
        int rY=(int) floor(0.5*y/blockSize+0.5)+mazeSize;
        return isWall(rX,rY,maze);
    }

    public boolean isWall(double minX, double minY) {
        return cs.world.getBlockState((int) floor(minX), (int) floor(minY)).getBlock().solid;
    }
}
