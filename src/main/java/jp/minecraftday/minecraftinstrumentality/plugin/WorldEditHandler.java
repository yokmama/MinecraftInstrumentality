package jp.minecraftday.minecraftinstrumentality.plugin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import jp.minecraftday.minecraftinstrumentality.utils.Region;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

public class WorldEditHandler {
    WorldEdit worldEdit;

    public WorldEditHandler(JavaPlugin plugin) {
        WorldEditPlugin worldEditPlugin = (WorldEditPlugin) plugin;
        this.worldEdit = worldEditPlugin.getWorldEdit();
    }


    public Region getSelection(Player player) throws Exception {
        com.sk89q.worldedit.bukkit.BukkitPlayer wePlayer = BukkitAdapter.adapt(player);
        LocalSession session = worldEdit.getSessionManager().get(wePlayer);

        try {
            com.sk89q.worldedit.regions.Region region = session.getSelection(wePlayer.getWorld());

            BlockVector max = new BlockVector(
                    region.getMaximumPoint().getBlockX(),
                    region.getMaximumPoint().getBlockY(),
                    region.getMaximumPoint().getBlockZ());

            BlockVector min = new BlockVector(
                    region.getMinimumPoint().getBlockX(),
                    region.getMinimumPoint().getBlockY(),
                    region.getMinimumPoint().getBlockZ());

            BlockVector3 center3 = region.getCenter().toBlockPoint();
            BlockVector center = new BlockVector(
                    center3.getBlockX(),
                    center3.getBlockY(),
                    center3.getBlockZ());

            return new Region(max, min, center, player.getWorld());

        } catch (IncompleteRegionException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }
}

