package big.engine.math.test;

import big.engine.math.util.PacketUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class JsonTest {
    public static void main(String[] args) {
        float[] floats={1,2,3,4,5};
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"test",floats);
        System.out.println(o.toString());
        System.out.println(Arrays.toString((float[]) PacketUtil.get(o,"test")));
    }
}
