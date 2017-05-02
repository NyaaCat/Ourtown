package cat.nyaa.ourtown.spawn;


import cat.nyaa.utils.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SpawnLocation implements ISerializable {
    @Serializable
    private String world = "";
    @Serializable
    private String name = "";
    @Serializable
    private String description = "";
    @Serializable
    private double x = 0;
    @Serializable
    private double y = 0;
    @Serializable
    private double z = 0;
    @Serializable
    private double yaw = 0;
    @Serializable
    private double pitch = 0;

    public SpawnLocation() {
    }

    public SpawnLocation(String name, String description, Location location) {
        setName(name);
        setDescription(description);
        setLocation(location);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return (float) yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return (float) pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return world != null && Bukkit.getWorld(world) != null;
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(getWorld()), getX(), getY(), getZ(), (float) getYaw(), (float) getPitch());
    }

    public void setLocation(Location location) {
        setWorld(location.getWorld().getName());
        setX(location.getX());
        setY(location.getY());
        setZ(location.getZ());
        setYaw(location.getYaw());
        setPitch(location.getPitch());
    }
}
