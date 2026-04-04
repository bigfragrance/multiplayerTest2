package big.game.network;

import big.engine.util.Getter;
import big.engine.util.T2UInterface;
import big.game.network.packet.Packet;
import big.game.network.packet.c2s.*;
import big.game.network.packet.s2c.*;
import org.json.JSONObject;

public enum PacketType {
    //c2s
    DODGE_MOVE_C2S((obj)->new DodgeMoveC2SPacket(obj)),
    MESSAGE_C2S((obj)->new MessageC2SPacket(obj)),
    PLAYER_INPUT_C2S((obj)->new PlayerInputC2SPacket(obj)),
    PLAYER_RESPAWN_C2S((obj)->new PlayerRespawnC2SPacket()),
    PLAYER_DATA_C2S((obj)->new PlayerDataC2SPacket(obj)),
    UPDATE_WEAPON_C2S((obj)->new UpdateWeaponC2SPacket(obj)),
    WANT_CHUNK_C2S((obj)->new WantChunkC2SPacket(obj)),
    WANT_ENTITY_C2S((obj)->new WantEntityC2SPacket(obj)),
    WANT_WEAPON_C2S((obj)->new WantWeaponC2SPacket(obj)),
    //s2c
    ARRAY_S2C((obj)->new ArrayPacket(obj)),
    ASSETS_S2C((obj)->new AssetsS2CPacket(obj)),
    BLOCK_STATE_UPDATE_S2C((obj)->new BlockStateUpdateS2CPacket(obj)),
    CHUNK_UPDATE_S2C((obj)->new ChunkUpdateS2CPacket(obj)),
    MESSAGE_S2C((obj)->new MessageS2CPacket(obj)),
    PLAYER_DATA_S2C((obj)->new PlayerDataS2CPacket(obj)),
    PLAYER_SPAWN_S2C((obj)->new PlayerSpawnS2CPacket(obj)),
    ENTITY_SPAWN_S2C((obj)->new EntitySpawnS2CPacket(obj)),
    PLAYER_STATUS_S2C((obj)->new PlayerStatusS2CPacket(obj)),
    PLAYER_WEAPON_UPDATE_S2C((obj)->new PlayerWeaponUpdateS2CPacket(obj)),
    SERVER_DATA_S2C((obj)->new ServerDataS2CPacket(obj)),
    TICK_S2C((obj)->new TickS2CPacket(obj)),
    DODGE_GHOST_S2C((obj)->new DodgeGhostS2CPacket(obj)),
    EFFECT_S2C((obj)->new EffectS2CPacket(obj));
    private final T2UInterface<JSONObject, Packet<?>> createPacket;
    PacketType(T2UInterface<JSONObject, Packet<?>>createPacket) {
        this.createPacket = createPacket;
    }
    public Packet<?> createPacket(JSONObject o){
        return createPacket.get(o);
    }
}
