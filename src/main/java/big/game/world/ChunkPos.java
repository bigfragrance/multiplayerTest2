package big.game.world;

import big.engine.math.Vec2i;

public class ChunkPos extends Vec2i {
    public ChunkPos(int x, int y) {
        super(x, y);
    }
    public ChunkPos(Vec2i pos){
        this(Chunk.toChunk(pos.x),Chunk.toChunk(pos.y));
    }

    public static long toLong(int chunkX, int chunkZ) {
        return (long)chunkX & 4294967295L | ((long)chunkZ & 4294967295L) << 32;
    }
    public static int[] fromLong(long l){
        return new int[]{(int)(l&4294967295L),(int)(l>>>32)};
    }
}
