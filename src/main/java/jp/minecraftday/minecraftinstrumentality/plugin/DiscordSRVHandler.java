package jp.minecraftday.minecraftinstrumentality.plugin;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordSRVHandler {
    DiscordSRV discordSRV;
    public DiscordSRVHandler(JavaPlugin plugin){
        discordSRV = (DiscordSRV)plugin;
    }

    public void processChatMessage(Player player, String message, boolean cancelled) {
        discordSRV.processChatMessage(player, message, DiscordSRV.getPlugin().getChannels().size() == 1 ? null : "global", cancelled);
    }

}
