package jp.minecraftday.minecraftinstrumentality.plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.perm.PermissionsHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsHandler {
    Essentials essentials;
    public EssentialsHandler(JavaPlugin plugin){
        this.essentials = (Essentials)plugin;
    }

    public String getPrefix(Player base) {
        PermissionsHandler handler = essentials.getPermissionsHandler();
        return handler.getPrefix(base);
    }

    public boolean isMuted(Player player) {
        return essentials.getUser(player).isMuted();
    }

    public boolean isJailed(Player player) {
        return essentials.getUser(player).isJailed();
    }

    public String getChatColor(Player player){
        PermissionsHandler handler = essentials.getPermissionsHandler();
        String grp = handler.getGroup(player);
        if(grp.equals("gm")) return "&6";
        else if(grp.equals("sensei")) return "&6";
        return "&f";
    }

}
