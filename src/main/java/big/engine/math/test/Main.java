package big.engine.math.test;

/*import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            CompoundTag sword = new CompoundTag();
            sword.putString("id", "minecraft:diamond_sword");
            sword.putByte("Count", (byte)1);

            CompoundTag display = new CompoundTag();
            display.putString("Name", "{\"text\":\"Excalibur\"}");
            sword.put("display", display);

            File file = new File("sword.nbt");
            NBTUtil.write(sword, file);
            System.out.println("NBT 写入完成: " + file.getAbsolutePath());


            CompoundTag readTag = (CompoundTag) NBTUtil.read(file).getTag();
            System.out.println("读取 NBT 数据: " + readTag);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/