package big.server;

import org.json.JSONArray;
import org.json.JSONObject; 
import java.awt.Color; 
import java.util.concurrent.locks.ReentrantReadWriteLock; 
 
public class CanvasManager {
    private static final int CANVAS_SIZE = 64;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Color[][] grid = new Color[CANVAS_SIZE][CANVAS_SIZE];
 
    public JSONArray getFullSnapshot() {
        JSONArray snapshot = new JSONArray();
        lock.readLock().lock(); 
        try {
            for (int x = 0; x < CANVAS_SIZE; x++) {
                for (int y = 0; y < CANVAS_SIZE; y++) {
                    if (grid[x][y] != null) {
                        JSONObject pixel = new JSONObject();
                        pixel.put("x",  x);
                        pixel.put("y",  y);
                        pixel.put("color",  grid[x][y].getRGB());
                        snapshot.put(pixel); 
                    }
                }
            }
        } finally {
            lock.readLock().unlock(); 
        }
        return snapshot;
    }
 
    public synchronized boolean updatePixel(int x, int y, Color color) {
        if (x < 0 || x >= CANVAS_SIZE || y < 0 || y >= CANVAS_SIZE) return false;
        
        lock.writeLock().lock(); 
        try {
            grid[x][y] = color;
            return true;
        } finally {
            lock.writeLock().unlock(); 
        }
    }
}