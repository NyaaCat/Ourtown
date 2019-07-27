package cat.nyaa.ourtown.spawn;


import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.ourtown.I18n;
import cat.nyaa.ourtown.OurTown;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpawnCommands extends CommandReceiver {
    private OurTown plugin;

    public SpawnCommands(Object plugin, LanguageRepository i18n) {
        super((OurTown) plugin, i18n);
        this.plugin = (OurTown) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "spawn";
    }

    @SubCommand(value = "set", permission = "town.admin")
    public void setSpawn(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (args.length() < 3) {
            msg(sender, "manual.spawn.set.usage");
            return;
        }
        String name = args.nextString();
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn == null) {
            msg(sender, "user.spawn.not_found", name);
            return;
        }
        if (args.length() == 4) {
            spawn.setDescription(args.nextString());
        }
        spawn.setLocation(player.getLocation());
        plugin.config.spawnConfig.spawns.put(name, spawn);
        plugin.config.save();
        msg(sender, "user.spawn.set", name, spawn.getWorld(), (int) spawn.getX(), (int) spawn.getY(), (int) spawn.getZ());
        plugin.getLogger().info(I18n.format("log.set", spawn.getName(), spawn.getDescription(),
                spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch()));
    }

    @SubCommand(value = "list", permission = "town.admin")
    public void listSpawn(CommandSender sender, Arguments args) {
        for (String k : plugin.config.spawnConfig.spawns.keySet()) {
            SpawnLocation s = plugin.config.spawnConfig.spawns.get(k);
            String msg = I18n.format("user.spawn.list.info", s.getName(), s.getDescription(),
                    s.getWorld(), (int) s.getX(), (int) s.getY(), (int) s.getZ());
            msg += I18n.format("user.spawn.list.status", s.isAvailable() ?
                    I18n.format("user.status.available") : I18n.format("user.status.unavailable"));
            sender.sendMessage(msg);
        }
    }

    @SubCommand(value = "add", permission = "town.admin")
    public void addSpawn(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (args.length() < 3) {
            msg(sender, "manual.spawn.add.usage");
            return;
        }
        String name = args.nextString();
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn != null) {
            msg(sender, "user.spawn.exist", name);
            return;
        }
        spawn = new SpawnLocation();
        spawn.setName(name);
        spawn.setLocation(player.getLocation());
        if (args.length() == 4) {
            spawn.setDescription(args.nextString());
        }
        plugin.config.spawnConfig.spawns.put(name, spawn);
        plugin.config.save();
        msg(sender, "user.spawn.add", name, spawn.getWorld(), (int) spawn.getX(), (int) spawn.getY(), (int) spawn.getZ());
        plugin.getLogger().info(I18n.format("log.add", spawn.getName(), spawn.getDescription(),
                spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch()));
    }

    @SubCommand(value = "del", permission = "town.admin")
    public void delSpawn(CommandSender sender, Arguments args) {
        if (args.length() < 3) {
            msg(sender, "manual.spawn.del.usage");
            return;
        }
        String name = args.nextString();
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn == null) {
            msg(sender, "user.spawn.not_found", name);
            return;
        }
        plugin.config.spawnConfig.spawns.remove(name);
        for (UUID uuid : plugin.config.playerConfig.playerSpawn.keySet()) {
            if (plugin.config.playerConfig.playerSpawn.get(uuid).equals(name)) {
                plugin.config.playerConfig.playerSpawn.remove(uuid);
            }
        }
        plugin.config.save();
        msg(sender, "user.spawn.del", name);
        plugin.getLogger().info(I18n.format("log.del", spawn.getName(), spawn.getDescription(),
                spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch()));
    }

    @SubCommand(value = "setdescription", permission = "town.admin")
    public void delSetDescription(CommandSender sender, Arguments args) {
        if (args.length() < 4) {
            msg(sender, "manual.spawn.setdescription.usage");
            return;
        }
        String name = args.nextString();
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn == null) {
            msg(sender, "user.spawn.not_found", name);
            return;
        }
        spawn.setDescription(args.nextString());
        plugin.config.save();
    }
}
