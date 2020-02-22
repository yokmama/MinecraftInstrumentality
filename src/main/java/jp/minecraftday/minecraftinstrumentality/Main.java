package jp.minecraftday.minecraftinstrumentality;

import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.command.MainCommandExecutor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        super.onEnable();

        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("minecraftday").setExecutor(new MainCommandExecutor(this));
        getCommand("mg").setExecutor(new GameCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        String msg = getConfig().getString("welcome.message");
        if(msg == null) msg = "";

        msg = ChatColor.translateAlternateColorCodes('&', msg);

        event.getPlayer().sendMessage(msg);

    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<String> worlds = getConfig().getStringList("tntcanceller.worlds");
        worlds.forEach( name -> {
            if (event.getLocation().getWorld().getName().equals(name)) {
                event.setCancelled(true);
            }
        });
    }
}
