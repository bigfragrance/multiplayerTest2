package modules.client;

import engine.math.util.Setting;
import engine.modules.EngineMain;
import engine.render.Screen;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static engine.modules.EngineMain.cs;

public class ClientMain {
    public static void main(String[] args) {
        /*if (args.length  < 1) {
            System.err.println("Usage:  java modules.client.ClientMain <server-ip>");
            System.exit(1); 
        }*/
        
        SwingUtilities.invokeLater(()  -> {
            File setting=new File("setting.txt");
            String settingData=null;
            if(!setting.exists()){
                try {
                    setting.createNewFile();
                    settingData=Setting.create();
                    Files.write(setting.toPath(),settingData.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE,   // 文件不存在时创建
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    settingData=Files.readString(setting.toPath(),  StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(settingData==null) return;
            Setting o=new Setting(settingData);

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
            //new EngineMain("localhost",8088,false);
            new Thread(()->{
                cs.run();
            }).start();
            //cm.run();
            thread.start();
        });
    }
}