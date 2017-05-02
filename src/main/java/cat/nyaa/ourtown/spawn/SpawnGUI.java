package cat.nyaa.ourtown.spawn;


import cat.nyaa.ourtown.I18n;
import cat.nyaa.ourtown.ourtown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SpawnGUI extends SpawnInventoryHolder {
    public static String market_lore_code = ChatColor.translateAlternateColorCodes('&', "&f&f&9&e&c&1&4&a&5&1&1&2&0&7&4&r");
    public int currentPage = 1;
    public HashMap<Integer, String> spawnPoints = new HashMap<>();
    private ourtown plugin;
    private Player player;

    public SpawnGUI(ourtown pl, Player player) {
        this.plugin = pl;
        this.player = player;
        ArrayList<String> tmp = plugin.config.spawnConfig.getSpawns();
        if (plugin.config.gui_random) {
            Collections.shuffle(tmp);
        }
        for (int i = 0; i < tmp.size(); i++) {
            spawnPoints.put(i, tmp.get(i));
        }
    }

    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (event.getInventory().getSize() == 54 && slot >= 0 && slot < 54) {
            ItemStack item = event.getInventory().getItem(slot);
            if (slot >= 0 && slot < 45 && item != null && !item.getType().equals(Material.AIR)) {
                this.clickItem(player, event.getRawSlot(), event.isShiftClick());
                return;
            }
            if (event.getRawSlot() == 45 && event.getCurrentItem().getType() != Material.AIR) {
                this.openGUI(player, this.getCurrentPage() - 1);
            } else if (event.getRawSlot() == 53 && event.getCurrentItem().getType() != Material.AIR) {
                this.openGUI(player, this.getCurrentPage() + 1);
            } else {
                this.closeGUI(player);
            }
        } else {
            this.closeGUI(player);
        }
    }

    public void openGUI(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(this, 54, I18n.format("user.select.gui.title"));
        int pageCount;
        pageCount = (spawnPoints.size() + 45 - 1) / 45;
        int offset = 0;
        if (page < 1 || page > pageCount) {
            page = 1;
        }
        if (page > 1) {
            offset = (page - 1) * (45);
        }
        setCurrentPage(page);
        for (int i = 0; i < 45; i++) {
            if (spawnPoints.size() > (offset + i)) {
                String spawnName = spawnPoints.getOrDefault(offset + i, null);
                if (spawnName != null) {
                    ItemStack itemStack = new ItemStack(plugin.config.gui_item);
                    addLore(plugin.config.spawnConfig.spawns.get(spawnName), itemStack);
                    inventory.setItem(i, itemStack);
                    continue;
                }
            }
            break;
        }
        if (page > 1) {
            ItemStack back = new ItemStack(Material.ARROW);
            ItemMeta backItemMeta = back.getItemMeta();
            backItemMeta.setDisplayName(I18n.format("user.info.back"));
            back.setItemMeta(backItemMeta);
            inventory.setItem(45, back);
        }
        if (page < pageCount) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            nextPageMeta.setDisplayName(I18n.format("user.info.next_page"));
            nextPage.setItemMeta(nextPageMeta);
            inventory.setItem(53, nextPage);
        }
        player.openInventory(inventory);
    }

    public void closeGUI(Player player) {
        player.closeInventory();
    }

    public boolean clickItem(Player player, int slot, boolean shift) {
        if (plugin.config.lock_spawn && plugin.hasSpawn(player)) {
            closeGUI(player);
            return false;
        }
        int itemId = slot;
        if (currentPage > 1) {
            itemId = ((currentPage - 1) * 45) + slot;
        }
        String name = this.spawnPoints.get(itemId);
        if (name != null) {
            SpawnLocation spawnLocation = plugin.config.spawnConfig.spawns.get(name);
            if (spawnLocation == null || !spawnLocation.isValid()) {
                this.openGUI(player, 1);
            } else {
                plugin.setPlayerSpawn(player, spawnLocation);
                plugin.teleport(player, spawnLocation);
                player.closeInventory();
            }
        }
        return false;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    public ItemStack addLore(SpawnLocation spawnLocation, ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
        } else {
            lore = new ArrayList<>();
        }
        meta.setDisplayName(I18n.format("user.select.gui.item.name", spawnLocation.getName()));
        lore.add(0, market_lore_code + ChatColor.RESET +
                I18n.format("user.select.gui.item.description", spawnLocation.getDescription()));

        lore.add(1, I18n.format("user.select.gui.item.click", player.getName()));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
