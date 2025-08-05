package modules.entity.player;

public class PlayerData {
    public double score;
    public double[] skillPoints;
    public double[] skillPointLevels;
    public int skillPointUsed;
    public PlayerData(ServerPlayerEntity player){
        this.score=player.score;
        this.skillPoints=player.skillPoints;
        this.skillPointLevels=player.skillPointLevels;
        this.skillPointUsed=player.skillPointUsed;
    }
    public void set(ServerPlayerEntity player){
        player.score=this.score;
        player.skillPoints=this.skillPoints;
        player.skillPointLevels=this.skillPointLevels;
        player.skillPointUsed=this.skillPointUsed;
    }
}
