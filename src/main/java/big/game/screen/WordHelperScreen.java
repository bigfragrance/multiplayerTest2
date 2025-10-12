package big.game.screen;

import big.engine.math.Vec2d;
import big.engine.util.Util;
import big.engine.render.Screen;
import big.game.entity.player.PlayerEntity;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class WordHelperScreen extends GUI {
    public static String woldsPath="after-1.txt";
    public static int checkAgainTime=10*20;
    public static WordHelperScreen INSTANCE=new WordHelperScreen();
    public static long staticID=1234561451;
    public static Vec2d start=new Vec2d(-5,4);
    public static double distance=1.8;
    public static JSONObject wordsList=new JSONObject();
    public static ArrayList<String> wordsKeyList=new ArrayList<>();
    public ArrayList<String> needCheckAgain=new ArrayList<>();
    private int timer=0;
    public int currentIndex=0;
    public ArrayList<PlayerEntity> toShow=new ArrayList<>();
    public String currentAnswer="";
    public String currentWord="";
    public WordHelperScreen(){

    }
    public void tick(){
        timer++;
        if(Screen.isMouseClicked(1)){
            Vec2d mouse=sc.inputManager.getMouseVec().add(cs.getCamPos());
            for(int i=0;i<toShow.size();i++){
                PlayerEntity player=toShow.get(i);
                if(player.position.distanceTo(mouse)<0.5){
                    if(player.name.equals("Confirm")||player.name.equals("Start")||player.name.equals("NextTurn")){
                        nextPage();
                        timer=0;
                        break;
                    }
                    boolean shouldCheckAgain=timer>checkAgainTime;
                    if(player.name.equals(currentAnswer)){
                        shouTranslation(shouldCheckAgain);
                    }else{
                        shouTranslation(true);
                        shouldCheckAgain=true;
                    }
                    if(shouldCheckAgain&&wordsList.has(currentWord)){
                        needCheckAgain.add(currentWord);
                    }
                    timer=0;
                    break;
                }
            }
        }
    }
    public void shouTranslation(boolean b){
        toShow.clear();
        toShow.add(get(currentWord+" "+currentAnswer,0,b?0:3));
        toShow.add(get("Confirm",1,4));
        if(b){
            toShow.add(get("NeedToRecheck",2,1));
        }
    }
    public void nextPage(){
        currentIndex++;
        if(currentIndex>=wordsKeyList.size()) {
            showEndPage();
            currentIndex=0;
            wordsKeyList=needCheckAgain;
            needCheckAgain=new ArrayList<>();
            return;
        }
        updateQuestion();
    }
    public void showEndPage(){
        toShow.clear();
        toShow.add(get("Finished",0,5));
        toShow.add(get("Result: "+needCheckAgain.size()+"/"+wordsKeyList.size(),1,4));
        toShow.add(get("NextTurn",2,3));
    }
    public void updateQuestion(){
        String word=wordsKeyList.get(currentIndex);
        String translation=wordsList.getString(word);
        String[] other=getOtherChoice(currentIndex);
        toShow.clear();
        toShow.add(get(word,0,2));
        int randomIndex= (int) Math.floor(Math.max(Math.random()*4-0.1,0));
        int added=0;
        for(;added<randomIndex;){
            toShow.add(get(other[added],added+1,1));
            added++;
        }
        toShow.add(get(translation,added+1,1));
        added++;
        for(;added<4;){
            toShow.add(get(other[added-1],added+1,1));
            added++;
        }
        toShow.add(get(""+currentIndex+"/"+wordsKeyList.size(),5,5));
        currentAnswer=translation;
        currentWord=word;
    }
    public String[] getOtherChoice(int current){
        String[] string=new String[3];
        int got=0;
        while(got<3){
            int random=(int)(Math.random()*wordsKeyList.size());
            if(random==current) continue;
            string[got]=wordsList.getString(wordsKeyList.get(random));
            got++;
        }
        return string;
    }
    public void render(Graphics g){
        for(int i=0;i<toShow.size();i++){
            PlayerEntity player=toShow.get(i);
            if(player==null) continue;
            player.render(g);
        }
    }
    public void init(){
        toShow.add(get("Start",0,0));
        String s=Util.read("output-3.txt");
        wordsList=new JSONObject(s);
        //JSONObject o2= parseVocabulary((Util.read("word-3.txt")));
        //merge(wordsList,o2);
        wordsKeyList.addAll(wordsList.keySet());
        Collections.shuffle(wordsKeyList);
        Util.write("output-3.txt",wordsList.toString());
    }
    public JSONObject getNotRememberWell(){
        JSONObject map=new JSONObject();
        for(String key:wordsKeyList){
            if(needCheckAgain.contains(key)){
                map.put(key,wordsList.getString(key));
            }
        }
        return map;
    }
    public String remove(String s){
        for(int i=0;i<100;i++){
            String left="(";
            String right=")";
            s=s.replaceAll(left+i+right,"");
        }
        return s;
    }
    public void merge(JSONObject obj,JSONObject toMerge){
        for(String key:toMerge.keySet()){
            obj.put(key,toMerge.getString(key));
        }
    }
    public static JSONObject parseVocabularyOld(String input) {
        JSONObject map = new JSONObject();
        String[] lines = input.split("\n");

        Pattern pattern = Pattern.compile("^\\d+\\.\\s*(\\S(?:.*?\\S)?)\\s+(.+)$");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String word = matcher.group(1).trim();
                String translation = matcher.group(2).trim();
                map.put(word, translation);
            }
        }
        return map;
    }
    public static JSONObject parseVocabulary(String input) {
        JSONObject map= new JSONObject();
        String[] lines = input.split("\n");


        Pattern pattern = Pattern.compile("^\\d+\\.\\s*(.+?)\\s{2,}(.+)$");

        for (String line : lines) {

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String word = matcher.group(1).trim();
                String translation = matcher.group(2).trim();
                map.put(word, translation);
                continue;
            }


            Pattern fallbackPattern = Pattern.compile("^\\d+\\.\\s*(\\S+)\\s+(.+)$");
            Matcher fallbackMatcher = fallbackPattern.matcher(line);
            if (fallbackMatcher.find()) {
                String word = fallbackMatcher.group(1).trim();
                String translation = fallbackMatcher.group(2).trim();
                map.put(word, translation);
            }
        }
        return map;
    }

    private PlayerEntity get(String name,int index,int team){
        PlayerEntity playerEntity=new PlayerEntity(getPos(index));
        playerEntity.id=staticID;
        playerEntity.rotation=0;
        playerEntity.prevRotation=0;
        playerEntity.name=name;
        playerEntity.team=team;
        return playerEntity;
    }
    private Vec2d getPos(int index){
        return start.add(0,-index*distance);
    }
}
