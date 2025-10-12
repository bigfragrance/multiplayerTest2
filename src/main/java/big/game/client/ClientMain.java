package big.game.client;

import big.engine.util.Setting;
import big.engine.modules.EngineMain;
import big.engine.render.Screen;

import javax.swing.*;
import java.awt.*;

import static big.engine.modules.EngineMain.cs;

public class ClientMain {
    public static void main(String[] args) {
        /*if (args.length  < 1) {
            System.err.println("Usage:  java game.client.ClientMain <big.server-ip>");
            System.exit(1); 
        }*/
        
        SwingUtilities.invokeLater(()  -> {
            Setting.init();
            Setting o=Setting.INSTANCE;
            Screen.frame = new JFrame(o.isServer()?"Server":"Client");
            Screen panel = new Screen();
            panel.setBackground(new Color(200,200,200));
            Screen.frame.add(panel);
            Screen.frame.setSize(1000, 1000);
            Screen.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Screen.frame.setVisible(true);
            Thread thread = new Thread(panel);
            new EngineMain(o.getServerAddress(),o.getServerPort(),o.isServer());
            cs.setting=o;
            Screen.mouseOffset=o.getMouseOffset();
            Screen.TARGET_FPS=o.getFps();
            //new EngineMain("localhost",8088,false);
            new Thread(()->{
                cs.run();
            }).start();
            //cm.run();
            thread.start();
        });
    }
}