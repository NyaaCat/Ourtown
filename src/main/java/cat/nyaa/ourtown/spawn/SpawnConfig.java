package cat.nyaa.ourtown.spawn;


import cat.nyaa.ourtown.ourtown;
import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SpawnConfig extends FileConfigure {
    public static String DEFAULT = "default";
    public HashMap<String, SpawnLocation> spawns = new HashMap<>();
    private ourtown plugin;

    public SpawnConfig(ourtown pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "spawn.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    public SpawnLocation getDefaultSpawnPoint() {
        return spawns.get(DEFAULT);
    }

    public SpawnLocation getRandomSpawnPoint() {
        if (spawns.size() <= 1) {
            return null;
        }
        ArrayList<String> tmp = new ArrayList<>();
        tmp.addAll(spawns.keySet());
        tmp.remove(DEFAULT);
        Collections.shuffle(tmp);
        return spawns.get(tmp.get(0));
    }

    public ArrayList<String> getSpawns() {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.addAll(spawns.keySet());
        tmp.remove(DEFAULT);
        return tmp;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        spawns.clear();
        ISerializable.deserialize(config, this);
        for (String idx : config.getKeys(false)) {
            SpawnLocation tmp = new SpawnLocation();
            tmp.deserialize(config.getConfigurationSection(idx));
            spawns.put(tmp.getName(), tmp);
        }
        if (!spawns.containsKey(DEFAULT) || !spawns.get(DEFAULT).isValid()) {
            plugin.getLogger().warning("default spawn point not exist");
            spawns.put(DEFAULT, new SpawnLocation(DEFAULT, "", Bukkit.getWorld("world").getSpawnLocation()));
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        for (String k : config.getKeys(false)) {
            config.set(k, null);
        }
        ISerializable.serialize(config, this);
        for (String k : spawns.keySet()) {
            spawns.get(k).serialize(config.createSection(spawns.get(k).getName()));
        }
    }
}
