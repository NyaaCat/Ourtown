package cat.nyaa.ourtown;

import cat.nyaa.ecore.EconomyCore;
import cat.nyaa.ourtown.spawn.SpawnConfig;
import cat.nyaa.ourtown.spawn.SpawnLocation;
import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class OurTown extends JavaPlugin {
    public static OurTown instance;
    public Configuration config;
    public I18n i18n;
    public Essentials ess;
    private CommandHandler commandHandler;
    private EventListener eventListener;
    private AutoSave autoSave;
    public boolean reload = false;
    @Nullable
    public static EconomyCore economyProvider;

    @Override
    public void onEnable() {
        instance = this;
        config = new Configuration(this);
        config.load();
        i18n = new I18n(this, this.config.language);
        commandHandler = new CommandHandler(this, this.i18n);
        getCommand("town").setExecutor(commandHandler);
        getCommand("town").setTabCompleter(commandHandler);
        eventListener = new EventListener(this);
        ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        if (!setupEconomy()) {
            this.getLogger().severe("ECore is not installed!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        autoSave = new AutoSave(this);
    }

    private boolean setupEconomy() {
        var rsp = Bukkit.getServicesManager().getRegistration(EconomyCore.class);
        if (rsp != null) {
            economyProvider = rsp.getProvider();
        }
        return economyProvider != null;
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getCommand("town").setExecutor(null);
        getCommand("town").setTabCompleter(null);
        HandlerList.unregisterAll(this);
        config.save();
    }

    public void reload() {
        reload = true;
        getServer().getScheduler().cancelTasks(this);
        getCommand("town").setExecutor(null);
        getCommand("town").setTabCompleter(null);
        HandlerList.unregisterAll(this);
        onEnable();
    }

    public void teleport(Player player, SpawnLocation loc) {
        try {
            ess.getUser(player).getAsyncTeleport().now(loc.getLocation(), false, PlayerTeleportEvent.TeleportCause.PLUGIN,new CompletableFuture<>());
            player.sendMessage(I18n.format("user.teleport", loc.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlayerSpawn(OfflinePlayer player, SpawnLocation loc) {
        config.playerConfig.playerSpawn.put(player.getUniqueId(), loc.getName());
        if (config.save_interval_ticks <= 0) {
            config.save();
        }
        if (player.isOnline()) {
            Bukkit.getPlayer(player.getUniqueId()).sendMessage(I18n.format("user.select.set", loc.getName()));
        }
        getLogger().info(I18n.format("log.select", player.getName(), loc.getName()));
    }

    public boolean hasSpawn(OfflinePlayer player) {
        return config.playerConfig.playerSpawn.containsKey(player.getUniqueId());
    }

    public SpawnLocation getPlayerSpawn(OfflinePlayer player) {
        SpawnLocation s = config.spawnConfig.spawns.get(config.playerConfig.playerSpawn.getOrDefault(player.getUniqueId(), SpawnConfig.DEFAULT));
        if (s == null) {
            return config.spawnConfig.spawns.get(SpawnConfig.DEFAULT);
        }
        return s;
    }

    public static Location getPlayerSpawnLocation(OfflinePlayer player) {
        if (OurTown.instance == null) return null;
        return OurTown.instance.getPlayerSpawn(player).getLocation();
    }
}
