package big.engine.util;


import java.io.*;

public class FileUtil {
    public static FileUtil instance=null;
    public FileUtil(String name) throws IOException {
        instance=this;
    }
    public static <T> void FileWrite(String name,Object list) {
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(name));
            ObjectOutputStream stream = new ObjectOutputStream(outputStream);
            stream.writeObject(list);
            //stream.close();
            //outputStream.close();
        } catch (Exception ignored) {
        }
    }
    public static <T> Object FileInput(String name) {
        Object o=null;
        try {
            FileInputStream inputStream = new FileInputStream(new File(name));
            ObjectInputStream stream = new ObjectInputStream(inputStream);
            o = stream.readObject();
            //inputStream.close();
            //stream.close();
        } catch (Exception ignored) {
        }
        return o;
    }
}
