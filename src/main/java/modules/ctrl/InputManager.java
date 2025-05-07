package modules.ctrl;

import engine.math.Vec2d;
import engine.render.Screen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.ConcurrentHashMap;


public class InputManager {
    public InputManager(){

    }
    public Vec2d getPlayerInput(){
        Vec2d input=new Vec2d(0,0);
        if(Screen.isKeyPressed('w')){
            input.y=1;
        }
        if(Screen.isKeyPressed('s')){
            input.y=-1;
        }
        if(Screen.isKeyPressed('a')){
            input.x=-1;
        }
        if(Screen.isKeyPressed('d')){
            input.x=1;
        }
        return input.limit(1);
    }
    public boolean isShooting(){
        return Screen.isKeyPressed(' ')||Screen.isKeyPressed(Screen.MOUSECHAR);
    }
    public boolean isRespawning(){
        return Screen.isKeyPressed('r');
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
            default -> {
                return false;
            }
        }
        return Screen.isKeyPressed(c);
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
