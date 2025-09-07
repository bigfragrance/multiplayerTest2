package big.modules.ctrl;

import big.engine.math.Vec2d;
import big.engine.render.Screen;


public class InputManager {
    public InputManager(){

    }
    public int[] getPlayerInput(){
        int[] input={0,0};
        if(Screen.isKeyPressed('w')){
            input[1]=1;
        }
        if(Screen.isKeyPressed('s')){
            input[1]=-1;
        }
        if(Screen.isKeyPressed('a')){
            input[0]=-1;
        }
        if(Screen.isKeyPressed('d')){
            input[0]=1;
        }
        return input;
    }
    public boolean isShooting(){
        return Screen.isKeyPressed(' ')||Screen.isMousePressed(1);
    }
    public boolean isDefending(){
        return Screen.isKeyPressed('z')||Screen.isMousePressed(3);
    }
    public boolean isRespawning(){
        return Screen.isKeyPressed('r');
    }
    public boolean enableAutoFire(){
        return Screen.isKeyPressed('g');
    }
    public boolean isOpeningSendMsg(){
        return Screen.isKeyClicked('t');
    }
    public Vec2d getMouseVec(){
        return Screen.mousePos.switchToGame1();
    }
    public boolean isUpgrading(int skill){
        char c;
        switch(skill){
            case(0)->{
                c='z';
            }
            case(1)->{
                c='x';
            }
            case(2)->{
                c='c';
            }
            case(3)->{
                c='v';
            }
            case(4)->{
                c='b';
            }
            case(5)-> {
                c = 'n';
            }
            case(6)-> {
                c = 'm';
            }
            case(7)-> {
                c = ',';
            }
            case(8)-> {
                c = '.';
            }
            case(9)-> {
                c = '/';
            }
            default -> {
                return false;
            }
        }
        return Screen.isKeyPressed(c);
    }
    public boolean isGeneratingMaze(){
        return Screen.isKeyPressed('m');
    }
    public boolean isPlacingMaze(){
        return Screen.isKeyPressed('b');
    }
    public boolean isPlacingBase(){
        return Screen.isKeyPressed('f');
    }
    public boolean isRemovingMaze(){
        return Screen.isKeyPressed('n');
    }
    public boolean isSaving(){
        return Screen.isKeyPressed('z');
    }
    public boolean isLoading(){
        return Screen.isKeyPressed('c');
    }
    public boolean isLocking() {
        return Screen.isKeyPressed('v');
    }
    public boolean isChangingShowingCurrentBlock() {
        return Screen.isKeyPressed('l');
    }
    public boolean isIncreasingMobRarity() {
        return Screen.isKeyPressed('k');
    }
    public boolean isDecreasingMobRarity() {
        return Screen.isKeyPressed('j');
    }
    public boolean isIncreasingPlaceRadius() {
        return Screen.isKeyPressed('o');
    }
    public boolean isDecreasingPlaceRadius() {
        return Screen.isKeyPressed('p');
    }
    public boolean isPuttingMobRarity() {
        return Screen.isKeyPressed('h');
    }
    public boolean isRenderingMobRarity() {
        return Screen.isKeyPressed('g');
    }
    public boolean isTickSpeeding(){
        return Screen.isKeyPressed('t');
    }
    public boolean isSpawningBullet(){
        return Screen.isKeyPressed('r');
    }
    public boolean isReloading(){
        return Screen.isKeyClicked('x')&&Screen.isKeyPressed('/');
    }
    public void unFocus(){
        for(char c:Screen.keyPressed.keySet()){
            Screen.keyPressed.put(c,false);
        }
    }
    /*private final ConcurrentHashMap<Integer, Boolean> keyStates = new ConcurrentHashMap<>();
    private final JPanel targetPanel;

    public InputManager(JPanel panel) {
        this.targetPanel  = panel;
        initializeListeners();
        configurePanelFocus();
    }

    private void configurePanelFocus() {
        targetPanel.setFocusable(true);
        targetPanel.requestFocusInWindow();

        targetPanel.addFocusListener(new  FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                keyStates.clear();
            }
        });
    }

    private void initializeListeners() {

        targetPanel.addKeyListener(new  KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyStates.put(e.getKeyCode(),  true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keyStates.put(e.getKeyCode(),  false);
            }
        });


        targetPanel.addMouseListener(new  MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                keyStates.put(e.getButton(),  true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                keyStates.put(e.getButton(),  false);
            }
        });
    }


    public boolean doKeyPressed(int keyCode) {
        return keyStates.getOrDefault(keyCode,  false);
    }*/
}
