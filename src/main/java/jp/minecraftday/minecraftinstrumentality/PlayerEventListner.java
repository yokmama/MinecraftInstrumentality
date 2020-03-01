package jp.minecraftday.minecraftinstrumentality;

import org.bukkit.entity.Player;

public interface PlayerEventListner {
    void onLogin(Player player);
    void onLogout(Player player);

}
