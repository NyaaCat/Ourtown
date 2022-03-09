package cat.nyaa.ourtown;

import cat.nyaa.ecore.EconomyCore;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

//https://github.com/NyaaCat/NyaaUtils/blob/1.17/src/main/java/cat/nyaa/nyaautils/commandwarpper/TeleportCmdWarpper.java
public class TeleportCmdListener implements Listener {
    private final OurTown plugin;
    private final net.ess3.api.IEssentials ess;
    private final EconomyCore economyCore;

    public TeleportCmdListener(@NotNull OurTown pl, net.ess3.api.IEssentials ess, EconomyCore economyCore) {
        this.plugin = pl;
        this.ess = ess;
        this.economyCore = economyCore;
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    private static @NotNull Location PlayerSpawn(OfflinePlayer player, @NotNull World world) {
        Location spawn = OurTown.getPlayerSpawnLocation(player);
        if (spawn == null || spawn.getWorld() == null) return world.getSpawnLocation();
        if (spawn.getWorld().getName().equals(world.getName())) {
            return spawn;
        }
        return world.getSpawnLocation();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (!plugin.config.teleportEnable) return;
        String cmd = e.getMessage().toLowerCase().trim();
        Player p = e.getPlayer();
        User iu = ess.getUser(p);
        Location curLoc = p.getLocation();
        if (cmd.equals("/home") || cmd.startsWith("/home ")) {
            e.setCancelled(true);
            List<String> homes = iu.getHomes();
            //For /home bed
            if (cmd.equals("/home bed") || (cmd.equals("/home") && homes.size() < 1)) {
                Location bedLoc = p.getBedSpawnLocation();
                if (bedLoc == null) {
                    msg(p, "user.teleport.bed_not_set_yet");
                    return;
                }
                doHome(p, iu, bedLoc, curLoc);
                return;
            }

            if (homes.size() < 1) {
                msg(p, "user.teleport.not_set_yet");
            } else if (homes.size() == 1 && cmd.equals("/home")) {
                Location homeLoc;
                try {
                    homeLoc = iu.getHome(homes.get(0));
                    if (homeLoc == null) {
                        msg(p, "user.teleport.invalid_home");
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    msg(p, "user.teleport.error");
                    return;
                }
                doHome(p, iu, homeLoc, curLoc);
            } else {
                String to = cmd.substring(5).trim();
                for (String home : homes) {
                    if (home.equals(to)) {
                        Location homeLoc;
                        try {
                            homeLoc = iu.getHome(to);
                            if (homeLoc == null) {
                                msg(p, "user.teleport.invalid_home");
                                return;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            msg(p, "user.teleport.error");
                            return;
                        }
                        doHome(p, iu, homeLoc, curLoc);
                        return;
                    }
                }
                msg(p, "user.teleport.homes", String.join(", ", homes.toArray(new String[0])));
            }
        } else if (cmd.equals("/sethome") || cmd.startsWith("/sethome ")) {
            e.setCancelled(true);
            String name = cmd.replace("/sethome", "").trim();
            if (name.equals("")) {
                name = "home";
            }
            doSetHome(p, iu, curLoc, name);
        } else if (cmd.equals("/back") || cmd.startsWith("/back ")) {
            e.setCancelled(true);
            Location lastLoc = iu.getLastLocation();
            if (lastLoc == null) {
                msg(p, "user.teleport.no_loc");
                return;
            }
            doBack(p, iu, curLoc, lastLoc);
        }
    }

    private void doSetHome(Player p, User iu, Location curLoc, String name) {
        int n = checkHomeLimit(iu, name);
        if (n == 1) {
            if (!name.equals("home"))
                msg(p, "user.teleport.home_limit_one");
            name = "home";
        } else if (n != 0) {
            msg(p, "user.teleport.home_limit", n);
            return;
        }
        if ("bed".equals(name) || NumberUtil.isInt(name)) {
            msg(p, "user.teleport.invalid_name");
            return;
        }
        if (!ess.getSettings().isTeleportSafetyEnabled() && LocationUtil.isBlockUnsafeForUser(ess, iu, curLoc.getWorld(), curLoc.getBlockX(), curLoc.getBlockY(), curLoc.getBlockZ())) {
            msg(p, "user.teleport.unsafe");
            return;
        }

        double fee = plugin.config.setHomeMax;
        World defaultWorld = Bukkit.getWorld(plugin.config.setHomeDefaultWorld);
        if (defaultWorld == null) {
            defaultWorld = Bukkit.getWorlds().get(0);
        }
        if (curLoc.getWorld() != defaultWorld) {
            fee += plugin.config.setHomeWorld;
            fee -= curLoc.distance(PlayerSpawn(p, curLoc.getWorld())) * (double) plugin.config.setHomeDecrement / plugin.config.setHomeDistance;
        } else {
            fee -= curLoc.distance(PlayerSpawn(p, defaultWorld)) * (double) plugin.config.setHomeDecrement / plugin.config.setHomeDistance;
        }
        if (fee < plugin.config.setHomeMin) fee = plugin.config.setHomeMin;
        fee = new BigDecimal(fee).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
        if (!economyCore.withdrawPlayer(p.getUniqueId(), fee)) {
            msg(p, "user.teleport.money_insufficient", fee);
            return;
        }
        iu.setHome(name, curLoc);
        msg(p, "user.teleport.ok", fee, I18n.format("user.teleport.sethome"));
        if (fee > 0) {
            economyCore.depositSystemVault(fee);
        }
    }

    private void doBack(Player p, @NotNull User iu, Location curLoc, @NotNull Location lastLoc) {
        if (iu.getWorld() != lastLoc.getWorld() && ess.getSettings().isWorldTeleportPermissions() && !iu.isAuthorized("essentials.worlds." + lastLoc.getWorld().getName())) {
            msg(p, "internal.error.no_required_permission", "essentials.worlds." + lastLoc.getWorld().getName());
            return;
        }

        double fee = plugin.config.backBase;
        if (curLoc.getWorld() != lastLoc.getWorld()) {
            fee += plugin.config.backWorld;
            fee += lastLoc.distance(PlayerSpawn(p, lastLoc.getWorld())) * (double) plugin.config.backIncrement / plugin.config.backDistance;
        } else {
            fee += lastLoc.distance(curLoc) * (double) plugin.config.backIncrement / plugin.config.backDistance;
        }
        if (fee > plugin.config.backMax) fee = plugin.config.backMax;
        fee = new BigDecimal(fee).setScale(2, RoundingMode.HALF_DOWN).doubleValue();

        double finalFee = fee;
        if (!economyCore.withdrawPlayer(p.getUniqueId(), finalFee)) {
            msg(p, "user.teleport.money_insufficient", finalFee);
            return;
        }
        var future = new CompletableFuture<Boolean>();
        future.exceptionally((e) -> {
            asyncUtils.callSyncAndGet(() -> {
                p.sendMessage(e.getMessage());
                return false;
            });
            return false;
        });

        future.thenAccept(aBoolean -> {
                    if (!aBoolean) {
                        asyncUtils.callSyncAndGet(() ->
                                economyCore.depositPlayer(p.getUniqueId(), finalFee)
                        );
                    } else {
                        asyncUtils.callSyncAndGet(() -> {
                                    msg(p, "user.teleport.ok", finalFee, I18n.format("user.teleport.back"));
                                    return economyCore.depositSystemVault(finalFee);
                                }
                        );
                    }
                }
        );
        iu.getAsyncTeleport().back(new Trade(0, ess), future);


    }

    private void doHome(Player p, @NotNull User iu, @NotNull Location homeLoc, Location curLoc) {
        if (iu.getWorld() != homeLoc.getWorld() && ess.getSettings().isWorldHomePermissions() && !iu.isAuthorized("essentials.worlds." + homeLoc.getWorld().getName())) {
            msg(p, "internal.error.no_required_permission", "essentials.worlds." + homeLoc.getWorld().getName());
            return;
        }

        double fee = plugin.config.homeBase;
        if (homeLoc.getWorld() != curLoc.getWorld()) {
            fee += plugin.config.homeWorld;
            fee += homeLoc.distance(PlayerSpawn(p, homeLoc.getWorld())) * (double) plugin.config.homeIncrement / plugin.config.homeDistance;
        } else {
            fee += homeLoc.distance(curLoc) * (double) plugin.config.homeIncrement / plugin.config.homeDistance;
        }
        if (fee > plugin.config.homeMax) fee = plugin.config.homeMax;
        fee = new BigDecimal(fee).setScale(2, RoundingMode.HALF_DOWN).doubleValue();

        var finalFee = fee;
        if (!economyCore.withdrawPlayer(p.getUniqueId(), finalFee)) {
            msg(p, "user.teleport.money_insufficient", finalFee);
            return;
        }

        var future = new CompletableFuture<Boolean>();
        future.exceptionally((e) -> {
            asyncUtils.callSyncAndGet(() -> {
                p.sendMessage(e.getMessage());
                return false;
            });
            return false;
        });
        future.thenAccept(aBoolean -> {
                    if (!aBoolean) {
                        asyncUtils.callSyncAndGet(() ->
                                economyCore.depositPlayer(p.getUniqueId(), finalFee)
                        );
                    } else {
                        asyncUtils.callSyncAndGet(() -> {
                                    msg(p, "user.teleport.ok", finalFee, I18n.format("user.teleport.home"));
                                    return economyCore.depositSystemVault(finalFee);
                                }
                        );
                    }
                }
        );
        iu.getAsyncTeleport().teleport(homeLoc, new Trade(0, ess), PlayerTeleportEvent.TeleportCause.PLUGIN, future);

    }

    private int checkHomeLimit(final @NotNull User user, String name) {
        if (!user.isAuthorized("essentials.sethome.multiple.unlimited")) {
            int limit = ess.getSettings().getHomeLimit(user);
            if (user.getHomes().size() == limit && user.getHomes().contains(name)) {
                return 0;
            }
            if (user.getHomes().size() >= limit) {
                return limit;
            }
            if (limit == 1) {
                return 1;
            }
        }
        return 0;
    }

    private void msg(@NotNull CommandSender target, String template, Object... args) {
        target.sendMessage(I18n.format(template, args));
    }

    public static class asyncUtils {
        public static <T> T callSyncAndGet(@NotNull Callable<T> callable) {
            return callSyncAndGet(callable, null);
        }

        @Nullable
        public static <T> T callSyncAndGet(@NotNull Callable<T> callable, @Nullable Plugin plugin) {
            if (Bukkit.isPrimaryThread()) {
                try {
                    return callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                var p = plugin;
                if (p == null) p = OurTown.instance;
                if (p == null) return null;
                try {
                    var future = Bukkit.getScheduler().callSyncMethod(p, callable);
                    return future.get();
                } catch (CancellationException cancellationException) {
                    p.getLogger().warning("Task cancelled");
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

}
