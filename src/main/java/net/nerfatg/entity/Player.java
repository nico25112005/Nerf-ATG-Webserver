package net.nerfatg.entity;
import net.nerfatg.Utils.GPS;
import net.nerfatg.Utils.WeaponType;

public class Player {

    private final String id;
    private final String name;
    private WeaponType weaponType;
    private GPS gps;
    private byte health;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public WeaponType getWeaponType() { return weaponType; }

    public void setWeaponType(WeaponType weaponType) {this.weaponType = weaponType; }

    public GPS getGps(){ return gps; }

    public void setGps(GPS gps){ this.gps = gps; }

    public byte getHealth() { return health;}

    public void setHealth(byte health) { this.health = health; }



}
