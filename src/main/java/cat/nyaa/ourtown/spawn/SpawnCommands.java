package cat.nyaa.ourtown.spawn;


import cat.nyaa.ourtown.ourtown;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommands extends CommandReceiver<ourtown> {
    private ourtown plugin;

    public SpawnCommands(Object plugin, Internationalization i18n) {
        super((ourtown) plugin, i18n);
        this.plugin = (ourtown) plugin;
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
        String name = args.next();
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn == null) {
            msg(sender, "user.spawn.not_found", name);
            return;
        }
        if (args.length() == 4) {
            spawn.setDescription(args.next());
        }
        spawn.setLocation(player.getLocation());
        plugin.config.spawnConfig.spawns.put(name, spawn);
        plugin.config.save();
        msg(sender, "user.spawn.set", name, spawn.getWorld(), (int) spawn.getX(), (int) spawn.getY(), (int) spawn.getZ());
    }

    @SubCommand(value = "list", permission = "town.admin")
    public void listSpawn(CommandSender sender, Arguments args) {
        for (String k : plugin.config.spawnConfig.spawns.keySet()) {
            SpawnLocation s = plugin.config.spawnConfig.spawns.get(k);
            msg(sender, "user.spawn.list.info", s.getName(), s.getDescription(), s.getWorld(), (int) s.getX(), (int) s.getY(), (int) s.getZ());
        }
    }

    @SubCommand(value = "add", permission = "town.admin")
    public void addSpawn(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (args.length() < 3) {
            msg(sender, "manual.spawn.add.usage");
            return;
        }
        String name = args.next();
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn != null) {
            msg(sender, "user.spawn.exist", name);
            return;
        }
        spawn = new SpawnLocation();
        spawn.setName(name);
        spawn.setLocation(player.getLocation());
        if (args.length() == 4) {
            spawn.setDescription(args.next());
        }
        plugin.config.spawnConfig.spawns.put(name, spawn);
        plugin.config.save();
        msg(sender, "user.spawn.add", name, spawn.getWorld(), (int) spawn.getX(), (int) spawn.getY(), (int) spawn.getZ());
    }

    @SubCommand(value = "del", permission = "town.admin")
    public void delSpawn(CommandSender sender, Arguments args) {
        String name = args.next();
        if (args.length() < 3) {
            msg(sender, "manual.spawn.del.usage");
            return;
        }
        SpawnLocation spawn = plugin.config.spawnConfig.spawns.get(name);
        if (spawn == null) {
            msg(sender, "user.spawn.not_found", name);
            return;
        }
        plugin.config.spawnConfig.spawns.remove(name);
        plugin.config.save();
        msg(sender, "user.spawn.del", name);
    }
}
