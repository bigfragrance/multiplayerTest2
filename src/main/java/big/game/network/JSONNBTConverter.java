package big.game.network;

import net.querz.nbt.tag.*;
import org.json.*;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class JSONNBTConverter {
    //public static ConcurrentHashMap<JSONObject,CompoundTag> jsonToNBTMap=new ConcurrentHashMap<>();

    /** JSON -> CompoundTag */
    public static CompoundTag toCompound(JSONObject json) {
        CompoundTag tag=new CompoundTag();
        tag.put("j",new StringTag(json.toString()));
        /*//if(jsonToNBTMap.containsKey(json)) return jsonToNBTMap.get(json);
        json = new JSONObject(json.toString());
        CompoundTag tag = new CompoundTag();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            tag.put(key, toNBT(value));
        }
        //jsonToNBTMap.put(json,tag);*/
        return tag;
    }

    private static Tag<?> toNBT(Object value) {
        if (value instanceof JSONObject) {
            return toCompound((JSONObject) value);
        } else if (value instanceof JSONArray array) {
            return arrayToNBT(array);
        } else if (value instanceof String s) {
            return new StringTag(s);
        } else if (value instanceof Integer i) {
            return new IntTag(i);
        } else if (value instanceof Long l) {
            return new LongTag(l);
        } else if (value instanceof Double d) {
            return new DoubleTag(d);
        } else if (value instanceof Float f) {
            return new FloatTag(f);
        } else if (value instanceof Boolean b) {
            return new ByteTag((byte) (b ? 1 : 0));
        } else if (value == JSONObject.NULL || value == null) {
            return new StringTag("");
        } else {
            return new StringTag(value.toString());
        }
    }

    private static Tag<?> arrayToNBT(JSONArray array) {
        if (array.isEmpty()) {
            return new ListTag<>(StringTag.class);
        }

        boolean allBoolean = true;
        boolean allInteger = true;
        boolean allLong = true;
        boolean allDouble = true;
        boolean allString = true;
        boolean allCompound = true;

        for (int i = 0; i < array.length(); i++) {
            Object elem = array.get(i);
            if (!(elem instanceof Boolean)) allBoolean = false;
            if (!(elem instanceof Integer)) allInteger = false;
            if (!(elem instanceof Long || elem instanceof Integer)) allLong = false;
            if (!(elem instanceof Number)) allDouble = false;
            if (!(elem instanceof String)) allString = false;
            if (!(elem instanceof JSONObject)) allCompound = false;
        }

        if (allInteger) {
            int[] arr = new int[array.length()];
            for (int i = 0; i < array.length(); i++) arr[i] = array.getInt(i);
            return new IntArrayTag(arr);
        } else if (allLong && !allInteger) {
            long[] arr = new long[array.length()];
            for (int i = 0; i < array.length(); i++) arr[i] = ((Number) array.get(i)).longValue();
            return new LongArrayTag(arr);
        } else if (allDouble && !allInteger && !allLong) {
            ListTag<DoubleTag> list = new ListTag<>(DoubleTag.class);
            for (int i = 0; i < array.length(); i++) list.add(new DoubleTag(((Number) array.get(i)).doubleValue()));
            return list;
        } else if (allBoolean) {
            ListTag<ByteTag> list = new ListTag<>(ByteTag.class);
            for (int i = 0; i < array.length(); i++) list.add(new ByteTag((byte) ((Boolean) array.get(i) ? 1 : 0)));
            return list;
        } else if (allString) {
            ListTag<StringTag> list = new ListTag<>(StringTag.class);
            for (int i = 0; i < array.length(); i++) list.add(new StringTag(array.getString(i)));
            return list;
        } else if (allCompound) {
            ListTag<CompoundTag> list = new ListTag<>(CompoundTag.class);
            for (int i = 0; i < array.length(); i++) list.add(toCompound(array.getJSONObject(i)));
            return list;
        } else {
            // 混合类型，全部转 StringTag
            ListTag<StringTag> list = new ListTag<>(StringTag.class);
            for (int i = 0; i < array.length(); i++) {
                Object elem = array.get(i);
                if (elem instanceof JSONArray || elem instanceof JSONObject) {
                    list.add(new StringTag(elem.toString())); // 嵌套 JSONArray/JSONObject 转字符串
                } else {
                    list.add(new StringTag(String.valueOf(elem)));
                }
            }
            return list;
        }
    }

    /** CompoundTag -> JSON */
    public static JSONObject toJSON(CompoundTag tag) {
        /*JSONObject json = new JSONObject();
        for (String key : tag.keySet()) {
            Tag<?> value = tag.get(key);
            json.put(key, fromNBT(value));
        }*/
        return new JSONObject(tag.getString("j"));
    }

    private static Object fromNBT(Tag<?> tag) {
        if (tag instanceof CompoundTag compound) {
            return toJSON(compound);
        } else if (tag instanceof ListTag<?> list) {
            JSONArray arr = new JSONArray();
            for (Tag<?> elem : list) {
                arr.put(fromNBT(elem)); // 递归转换
            }
            return arr;
        } else if (tag instanceof ByteArrayTag bat) {
            JSONArray arr = new JSONArray();
            for (byte b : bat.getValue()) arr.put(b);
            return arr;
        } else if (tag instanceof IntArrayTag iat) {
            JSONArray arr = new JSONArray();
            for (int i : iat.getValue()) arr.put(i);
            return arr;
        } else if (tag instanceof LongArrayTag lat) {
            JSONArray arr = new JSONArray();
            for (long l : lat.getValue()) arr.put(l);
            return arr;
        } else if (tag instanceof StringTag st) {
            return st.getValue();
        } else if (tag instanceof IntTag it) {
            return it.asInt();
        } else if (tag instanceof LongTag lt) {
            return lt.asLong();
        } else if (tag instanceof DoubleTag dt) {
            return dt.asDouble();
        } else if (tag instanceof FloatTag ft) {
            return ft.asFloat();
        } else if (tag instanceof ByteTag bt) {
            byte b = bt.asByte();
            if (b == 0) return false;
            if (b == 1) return true;
            return b;
        }
        return tag.toString();
    }
}
