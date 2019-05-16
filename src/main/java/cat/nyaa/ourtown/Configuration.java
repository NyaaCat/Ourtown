package cat.nyaa.ourtown;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.ourtown.spawn.PlayerConfig;
import cat.nyaa.ourtown.spawn.SpawnConfig;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

enum SPAWN_MODE {SELECT, RANDOM}

public class Configuration extends PluginConfigure {
    private final OurTown plugin;

    @Serializable
    public String language = "en_US";
    @Serializable
    public SPAWN_MODE mode = SPAWN_MODE.RANDOM;
    @Serializable
    public boolean force_spawn = false;
    @Serializable
    public boolean override_command = false;
    @Serializable
    public boolean lock_spawn = false;
    @Serializable
    public int save_interval_ticks = 1800 * 20;
    @Serializable
    public boolean gui_random = true;
    @Serializable
    public Material gui_item = Material.OAK_SIGN;
    @Serializable
    public boolean auto_open_gui = true;
    @Serializable
    public int select_fee = 0;
    @Serializable
    public boolean handle_player_respawn = true;
    @Serializable
    public EventPriority respawn_listener_priority = EventPriority.HIGH;
    @StandaloneConfig
    public SpawnConfig spawnConfig;
    @StandaloneConfig
    public PlayerConfig playerConfig;

    public Configuration(OurTown plugin) {
        this.plugin = plugin;
        spawnConfig = new SpawnConfig(plugin);
        playerConfig = new PlayerConfig(plugin);
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
    }
}
