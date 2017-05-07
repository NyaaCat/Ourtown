package cat.nyaa.ourtown;

import cat.nyaa.ourtown.spawn.SpawnGUI;
import cat.nyaa.ourtown.spawn.SpawnInventoryHolder;
import cat.nyaa.ourtown.spawn.SpawnLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {

    private OurTown plugin = null;

    public EventListener(OurTown pl) {
        this.plugin = pl;
        pl.getServer().getPluginManager().registerEvents(this, pl);
        if (plugin.config.handle_player_respawn) {
            plugin.getServer().getPluginManager().registerEvent(PlayerRespawnEvent.class, this,
                    plugin.config.respawn_listener_priority, new EventExecutor() {
                        @Override
                        public void execute(Listener listener, Event event) throws EventException {
                            ((EventListener) listener).onPlayerRespawn((PlayerRespawnEvent) event);
                        }
                    }, plugin, true);
        }
    }

    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.config.handle_player_respawn) {
            return;
        }
        Location respawnLocation = event.getRespawnLocation();
        Location defaultLocation = respawnLocation.getWorld().getSpawnLocation();
        if (respawnLocation.equals(defaultLocation) || respawnLocation.distance(defaultLocation) <= 2) {
            SpawnLocation spawn = plugin.getPlayerSpawn(event.getPlayer());
            event.setRespawnLocation(spawn.getLocation());
            plugin.getLogger().info(I18n.format("log.respawn", event.getPlayer().getName(), spawn.getName()));
        }
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
