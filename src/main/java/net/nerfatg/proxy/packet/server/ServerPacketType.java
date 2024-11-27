package net.nerfatg.proxy.packet.server;

public enum ServerPacketType {
    GameInfo,
    /*
    GameType type (int)
    String id size(5)
    String name size(16)
    int playerCount
    int maxPlayer
     */
    PlayerBundle,
    /*
    UUID playerID
    int teamIndex
     */
    PlayerStatus,
    /*
    UUID targetID
    String name size 16
    int teamIndex
    double longitude
    double latitude
    int health
     */
}
