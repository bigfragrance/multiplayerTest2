package big.engine.math.test;

import big.engine.util.PacketUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class JsonTest {
    public static void main(String[] args) {
        double[] doubles={1,2,3,4,5};
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"test",doubles);
        System.out.println(o.toString());
        System.out.println(Arrays.toString((double[]) PacketUtil.get(o,"test")));
    }
}
