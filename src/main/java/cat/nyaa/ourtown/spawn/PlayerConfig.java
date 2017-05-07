package cat.nyaa.ourtown.spawn;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.ourtown.OurTown;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class PlayerConfig extends FileConfigure {
    public HashMap<UUID, String> playerSpawn = new HashMap<>();
    private OurTown plugin;

    public PlayerConfig(OurTown pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "player.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        playerSpawn.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("spawn")) {
            ConfigurationSection cfg = config.getConfigurationSection("spawn");
            for (String uuid : cfg.getKeys(false)) {
                playerSpawn.put(UUID.fromString(uuid), cfg.getString(uuid));
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        config.set("spawn", null);
        ISerializable.serialize(config, this);
        ConfigurationSection cfg = config.createSection("spawn");
        for (UUID uuid : playerSpawn.keySet()) {
            cfg.set(uuid.toString(), playerSpawn.get(uuid));
        }
    }
}
