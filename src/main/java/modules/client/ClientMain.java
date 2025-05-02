package modules.client;

import engine.modules.EngineMain;
import engine.render.Screen;

import javax.swing.*;
import java.awt.*;

public class ClientMain {
    public static void main(String[] args) {
        /*if (args.length  < 1) {
            System.err.println("Usage:  java modules.client.ClientMain <server-ip>");
            System.exit(1); 
        }*/
        
        SwingUtilities.invokeLater(()  -> {
            Screen.frame = new JFrame("client");
            Screen panel = new Screen();
            panel.setBackground(new Color(255,255,255));
            Screen.frame.add(panel);
            Screen.frame.setSize(1000, 1000);
            Screen.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Screen.frame.setVisible(true);
            Thread thread = new Thread(panel);

            new EngineMain("frp-sea.com",48887,false);
            new Thread(()->{
                EngineMain.cs.run();
            }).start();
            //cm.run();
            thread.start();
        });
    }
}