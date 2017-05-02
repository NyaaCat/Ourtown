package cat.nyaa.ourtown;

import cat.nyaa.ourtown.spawn.SpawnGUI;
import cat.nyaa.ourtown.spawn.SpawnInventoryHolder;
import cat.nyaa.ourtown.spawn.SpawnLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {

    private ourtown plugin = null;

    public EventListener(ourtown pl) {
        this.plugin = pl;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != null && event.getInventory().getHolder() instanceof SpawnInventoryHolder) {
            ((SpawnGUI) event.getInventory().getHolder()).onInventoryClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (plugin.config.override_command && "/spawn".equalsIgnoreCase(event.getMessage())) {
            Player player = event.getPlayer();
            if (player.hasPermission("town.player.tp")) {
                event.setCancelled(true);
                SpawnLocation spawnLocation = plugin.getPlayerSpawn(player);
                if (!spawnLocation.isValid()) {
                    spawnLocation = plugin.config.spawnConfig.getDefaultSpawnPoint();
                }
                plugin.teleport(player, spawnLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isDead()) {
            return;
        }
        if (!plugin.hasSpawn(player)) {
            if (plugin.config.mode.equals(SPAWN_MODE.RANDOM)) {
                SpawnLocation spawnLocation = plugin.config.spawnConfig.getRandomSpawnPoint();
                if (spawnLocation != null && spawnLocation.isValid()) {
                    plugin.setPlayerSpawn(player, spawnLocation);
                }
            } else if (plugin.config.mode.equals(SPAWN_MODE.SELECT) && player.hasPermission("town.player.select")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline() || player.isDead()) {
                            return;
                        }
                        player.sendMessage(I18n.format("user.select.select"));
                        if (plugin.config.auto_open_gui) {
                            SpawnGUI spawnGUI = new SpawnGUI(plugin, player);
                            spawnGUI.openGUI(player, 1);
                        }
                    }
                }.runTaskLater(plugin, 10);
            }
        }
        if (plugin.config.force_spawn) {
            SpawnLocation spawnLocation = plugin.getPlayerSpawn(player);
            if (!spawnLocation.isValid()) {
                spawnLocation = plugin.config.spawnConfig.getDefaultSpawnPoint();
            }
            plugin.teleport(player, spawnLocation);
        }
    }
}
