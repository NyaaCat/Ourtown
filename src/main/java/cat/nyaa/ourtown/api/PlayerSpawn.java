package cat.nyaa.ourtown.api;

import cat.nyaa.ourtown.OurTown;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

@Deprecated
public abstract class PlayerSpawn {
    @Deprecated
    public static Location getPlayerSpawn(OfflinePlayer player){
        if(OurTown.instance == null)return null;
        return OurTown.instance.getPlayerSpawn(player).getLocation();
    }
}
