package jp.minecraftday.minecraftinstrumentality.plugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiverseHandler {
    MultiverseCore multiverseCore;
    public MultiverseHandler(JavaPlugin plugin){
        this.multiverseCore = (MultiverseCore)plugin;
    }


    public Location getSpawnPosition(String name){
        MultiverseWorld world = multiverseCore.getMVWorldManager().getMVWorld(name);
        if(world!=null){
            Location location = world.getSpawnLocation();
            return location;
        }
        return null;
    }

}
