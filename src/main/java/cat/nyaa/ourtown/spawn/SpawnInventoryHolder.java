package cat.nyaa.ourtown.spawn;


import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SpawnInventoryHolder implements InventoryHolder {
    /**
     * Get the object's inventory.
     *
     * @return The inventory.
     */
    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(null, 54);
    }
}
