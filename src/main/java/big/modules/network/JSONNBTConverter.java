package big.modules.network;

import net.querz.nbt.io.*;
import net.querz.nbt.tag.*;
import org.json.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

public class JSONNBTConverter {

    /** JSONObject -> CompoundTag */
    public static CompoundTag toCompound(JSONObject json) {
        CompoundTag tag = new CompoundTag();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            tag.put(key, toNBT(value));
        }
        return tag;
    }


    private static Tag<?> toNBT(Object value) {
        if (value instanceof JSONObject) {
            return toCompound((JSONObject) value);

        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            if (array.isEmpty()) {
                return new ListTag<>(StringTag.class);
            }

            Object first = array.get(0);


            if (first instanceof Integer && isAllType(array, Integer.class)) {

                int[] arr = new int[array.length()];
                for (int i = 0; i < array.length(); i++) arr[i] = array.getInt(i);
                return new IntArrayTag(arr);

            } else if (first instanceof Long && isAllType(array, Long.class)) {

                long[] arr = new long[array.length()];
                for (int i = 0; i < array.length(); i++) arr[i] = array.getLong(i);
                return new LongArrayTag(arr);

            } else if (first instanceof Byte && isAllType(array, Byte.class)) {

                byte[] arr = new byte[array.length()];
                for (int i = 0; i < array.length(); i++) arr[i] = ((Number) array.get(i)).byteValue();
                return new ByteArrayTag(arr);
            }


            if (first instanceof Integer) {
                ListTag<IntTag> list = new ListTag<>(IntTag.class);
                for (int i = 0; i < array.length(); i++) list.add(new IntTag(array.getInt(i)));
                return list;
            } else if (first instanceof Long) {
                ListTag<LongTag> list = new ListTag<>(LongTag.class);
                for (int i = 0; i < array.length(); i++) list.add(new LongTag(array.getLong(i)));
                return list;
            } else if (first instanceof Double) {
                ListTag<DoubleTag> list = new ListTag<>(DoubleTag.class);
                for (int i = 0; i < array.length(); i++) list.add(new DoubleTag(array.getDouble(i)));
                return list;
            } else if (first instanceof String) {
                ListTag<StringTag> list = new ListTag<>(StringTag.class);
                for (int i = 0; i < array.length(); i++) list.add(new StringTag(array.getString(i)));
                return list;
            } else if (first instanceof JSONObject) {
                ListTag<CompoundTag> list = new ListTag<>(CompoundTag.class);
                for (int i = 0; i < array.length(); i++) list.add(toCompound(array.getJSONObject(i)));
                return list;
            } else {

                ListTag<StringTag> list = new ListTag<>(StringTag.class);
                for (int i = 0; i < array.length(); i++) list.add(new StringTag(array.get(i).toString()));
                return list;
            }

        } else if (value instanceof String) {
            return new StringTag((String) value);
        } else if (value instanceof Integer) {
            return new IntTag((Integer) value);
        } else if (value instanceof Long) {
            return new LongTag((Long) value);
        } else if (value instanceof Double) {
            return new DoubleTag((Double) value);
        } else if (value instanceof Float) {
            return new FloatTag((Float) value);
        } else if (value instanceof Boolean) {
            return new ByteTag((byte) ((boolean) value ? 1 : 0));
        } else if (value == JSONObject.NULL) {
            return new StringTag("");
        } else {
            return new StringTag(value.toString());
        }
    }


    public static JSONObject toJSON(CompoundTag tag) {
        JSONObject json = new JSONObject();
        for (String key : tag.keySet()) {
            Tag<?> value = tag.get(key);
            json.put(key, fromNBT(value));
        }
        return json;
    }

    private static Object fromNBT(Tag<?> tag) {
        if (tag instanceof CompoundTag) {
            return toJSON((CompoundTag) tag);

        } else if (tag instanceof ListTag) {
            JSONArray arr = new JSONArray();
            for (Tag<?> elem : (ListTag<?>) tag) {
                arr.put(fromNBT(elem));
            }
            return arr;

        } else if (tag instanceof ByteArrayTag) {
            JSONArray arr = new JSONArray();
            for (byte b : ((ByteArrayTag) tag).getValue()) arr.put(b);
            return arr;

        } else if (tag instanceof IntArrayTag) {
            JSONArray arr = new JSONArray();
            for (int i : ((IntArrayTag) tag).getValue()) arr.put(i);
            return arr;

        } else if (tag instanceof LongArrayTag) {
            JSONArray arr = new JSONArray();
            for (long l : ((LongArrayTag) tag).getValue()) arr.put(l);
            return arr;

        } else if (tag instanceof StringTag) {
            return ((StringTag) tag).getValue();
        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).asInt();
        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).asLong();
        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).asDouble();
        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).asFloat();
        } else if (tag instanceof ByteTag) {
            byte b = ((ByteTag) tag).asByte();
            if (b == 0 || b == 1) return b == 1;
            return b;
        }
        return tag.toString();
    }


    private static boolean isAllType(JSONArray arr, Class<?> clazz) {
        for (int i = 0; i < arr.length(); i++) {
            Object val = arr.get(i);
            if (!clazz.isInstance(val)) return false;
        }
        return true;
    }
    /*public static String writeCompoundTagToStringFile(CompoundTag tag, File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (NBTOutputStream nbtOut = new NBTOutputStream(baos)) {
            nbtOut.writeTag(tag, true); // true = GZip 压缩
        }

        byte[] bytes = baos.toByteArray();

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }

        // 将二进制转换为 String（乱码）
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    // 从 String（乱码）恢复 CompoundTag
    public static CompoundTag readCompoundTagFromString(String data) throws IOException {
        // 先把 String 转回二进制
        byte[] bytes = data.getBytes(StandardCharsets.ISO_8859_1);

        try (NBTInputStream nbtIn = new NBTInputStream(new ByteArrayInputStream(bytes))) {
            NamedTag named = nbtIn.readTag();
            return (CompoundTag) named.getTag();
        }
    }*/

    public static void main(String[] args) throws IOException {
        String jsonStr = "{ \"name\":\"Steve\", \"health\":20, \"flying\":true, " +
                "\"pos\":[1,64,-30], " +
                "\"colors\":[255,128,64], " +
                "\"bigNumbers\":[1234567890123,9876543210], " +
                "\"inv\":[ {\"id\":\"minecraft:stone\",\"Count\":64}, {\"id\":\"minecraft:apple\",\"Count\":5} ] }";

        JSONObject json = new JSONObject(jsonStr);
        CompoundTag nbt = toCompound(json);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        String result = PrintStreamCapture.capture(ps,()->{
            try {
                new NBTSerializer(true).toStream(new NamedTag(null,nbt),ps);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.print(result);




        /*try {
            NBTUtil.write(nbt,new File("test.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("\nNBT 输出: " + nbt);

        JSONObject back = toJSON(nbt);
        System.out.println("还原回 JSON:\n" + back.toString(4));*/
    }
}
