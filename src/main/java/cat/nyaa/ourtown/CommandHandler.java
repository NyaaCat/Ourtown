package cat.nyaa.ourtown;

import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.ourtown.spawn.SpawnCommands;
import cat.nyaa.ourtown.spawn.SpawnConfig;
import cat.nyaa.ourtown.spawn.SpawnGUI;
import cat.nyaa.ourtown.spawn.SpawnLocation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler extends CommandReceiver {
    private final OurTown plugin;
    @SubCommand("spawn")
    public SpawnCommands spawnCommands;

    public CommandHandler(OurTown plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "tp", permission = "town.player.tp")
    public void commandTP(CommandSender sender, Arguments args) {
        Player player = null;
        SpawnLocation spawnLocation = null;
        if (sender.hasPermission("town.admin") && args.length() >= 2) {
            String name = args.nextString();
            spawnLocation = plugin.config.spawnConfig.spawns.get(name);
            if (spawnLocation == null) {
                msg(sender, "user.spawn.not_found", name);
                return;
            }
            if (args.length() == 3) {
                String playerName = args.nextString();
                player = Bukkit.getPlayer(playerName);
                if (player == null) {
                    msg(sender, "user.info.player_not_found", playerName);
                    return;
                }
            }
        }
        if (player == null) {
            player = asPlayer(sender);
        }
        if (spawnLocation == null) {
            spawnLocation = plugin.getPlayerSpawn(player);
        }
        if (!spawnLocation.isValid()) {
            spawnLocation = plugin.config.spawnConfig.getDefaultSpawnPoint();
        }
        plugin.teleport(player, spawnLocation);
    }

    @SubCommand(value = "select", permission = "town.player.select")
    public void commandSelect(CommandSender sender, Arguments args) {
        if (args.length() == 3 && sender.hasPermission("town.admin")) {
            String name = args.nextString();
            SpawnLocation spawnLocation = plugin.config.spawnConfig.spawns.get(name);
            if (spawnLocation == null || (SpawnConfig.DEFAULT.equals(name) && !sender.hasPermission("town.admin"))) {
                msg(sender, "user.spawn.not_found", name);
                return;
            }
            OfflinePlayer offlinePlayer = args.nextOfflinePlayer();
            if (offlinePlayer != null) {
                plugin.setPlayerSpawn(offlinePlayer, spawnLocation);
                if (offlinePlayer.isOnline() && plugin.hasSpawn(offlinePlayer)) {
                    plugin.teleport(offlinePlayer.getPlayer(), plugin.getPlayerSpawn(offlinePlayer));
                }
            } else {
                msg(sender, "user.info.player_not_found", name);
            }
        } else if (!plugin.config.lock_spawn || !plugin.hasSpawn(asPlayer(sender))) {
            Player player = asPlayer(sender);
            SpawnGUI spawnGUI = new SpawnGUI(this.plugin, player);
            spawnGUI.openGUI(player, 1);
        } else {
            msg(sender, "user.select.lock");
        }
    }

    @SubCommand(value = "reload", permission = "town.admin")
    public void commandReload(CommandSender sender, Arguments args) {
        plugin.reload();
    }

    @SubCommand(value = "save", permission = "town.admin")
    public void commandSave(CommandSender sender, Arguments args) {
        plugin.config.save();
    }

    @SubCommand(value = "status", permission = "town.admin")
    public void commandStatus(CommandSender sender, Arguments args) {
        if (args.length() == 3) {
            String name = args.nextString();
            SpawnLocation spawnLocation = plugin.config.spawnConfig.spawns.get(name);
            if (spawnLocation == null || (SpawnConfig.DEFAULT.equals(name))) {
                msg(sender, "user.spawn.not_found", name);
                return;
            }
            String status = args.nextString().toUpperCase();
            if ("ENABLE".equals(status)) {
                spawnLocation.setAvailable(true);
            } else if ("DISABLE".equals(status)) {
                spawnLocation.setAvailable(false);
            }
            if (spawnLocation.isAvailable()) {
                status = I18n.format("user.status.available");
            } else {
                status = I18n.format("user.status.unavailable");
            }
            msg(sender, "user.status.set", status);
            plugin.config.save();
        } else {
            msg(sender, "manual.status.usage");
            return;
        }
    }
}
