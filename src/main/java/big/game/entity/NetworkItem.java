package big.game.entity;

import org.json.JSONObject;

public interface NetworkItem {
    public void update(JSONObject o);
    public JSONObject toJSON();
}
