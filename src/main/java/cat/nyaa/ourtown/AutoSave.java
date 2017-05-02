package cat.nyaa.ourtown;

import org.bukkit.scheduler.BukkitRunnable;

public class AutoSave extends BukkitRunnable {
    private final ourtown plugin;

    public AutoSave(ourtown pl) {
        plugin = pl;
        if (plugin.config.save_interval_ticks > 0) {
            runTaskTimer(plugin, 20, plugin.config.save_interval_ticks);
        }
    }

    @Override
    public void run() {
        plugin.getLogger().info("Save data to config file...");
        plugin.config.save();
    }
}
