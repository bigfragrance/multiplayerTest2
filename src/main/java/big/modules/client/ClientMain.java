package big.modules.client;

import big.engine.math.util.Setting;
import big.engine.modules.EngineMain;
import big.engine.render.Screen;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class ClientMain {
    public static void main(String[] args) {
        /*if (args.length  < 1) {
            System.err.println("Usage:  java modules.client.ClientMain <big.server-ip>");
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
            //new EngineMain("localhost",8088,false);
            new Thread(()->{
                cs.run();
            }).start();
            //cm.run();
            thread.start();
        });
    }
}