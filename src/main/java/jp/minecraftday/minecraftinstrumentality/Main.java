package jp.minecraftday.minecraftinstrumentality;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.perm.PermissionsHandler;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.core.entities.PermissionOverride;
import jp.minecraftday.minecraftinstrumentality.command.*;
import jp.minecraftday.minecraftinstrumentality.utils.Configuration;
import jp.minecraftday.minecraftinstrumentality.utils.KanaConverter;
import jp.minecraftday.minecraftinstrumentality.utils.TellRawGenerator;
import jp.minecraftday.minecraftinstrumentality.utils.UserConfiguration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {
    GameCommandExecutor gameCommandExecutor;
    UserConfiguration userConfiguration;

    private DiscordSRV discordSRV = null;
    private Essentials essentials = null;

    @Override
    public void onEnable() {
        super.onEnable();

        gameCommandExecutor = new GameCommandExecutor(this);

        userConfiguration = new UserConfiguration(getDataFolder());

        Essentials ess = getPlugin(Essentials.class);
        if (ess!=null && !ess.isEnabled()) {
            essentials = ess;
        }

        discordSRV = DiscordSRV.getPlugin();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("md").setExecutor(new MainCommandExecutor(this));
        getCommand("minigame").setExecutor(gameCommandExecutor);
        getCommand("setitem").setExecutor(new SetItemNameCommandExecutor(this));
        CommandExecutor vote = new VoteCommandExecutor(this);
        getCommand("vote").setExecutor(vote);
        getCommand("voteyes").setExecutor(vote);
        getCommand("voteno").setExecutor(vote);
        getCommand("hiragana").setExecutor(new TranslateCommandExecutor(this));

        saveDefaultConfig();
    }

    public Configuration getUserConfiguration(Player player){
        return userConfiguration.getUserConfig(player);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        String msg = getConfig().getString("welcome.message");
        if (msg == null) msg = "";

        StringBuilder buf = new StringBuilder();
        buf.append("tellraw ").append(event.getPlayer().getName()).append(" ").append(msg);
        event.getPlayer().performCommand(buf.toString());

        gameCommandExecutor.onLogin(event.getPlayer());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<String> worlds = getConfig().getStringList("tntcanceller.worlds");
        worlds.forEach(name -> {
            if (event.getLocation().getWorld().getName().equals(name)) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        String fixString = event.getMessage();
        Configuration configuration = getUserConfiguration(event.getPlayer());
        boolean hiragana = configuration.getBoolean("hiragana");
        if(hiragana) {
            fixString = KanaConverter.conv(fixString);
        }

        PermissionsHandler handler = essentials.getPermissionsHandler();

        //Gm yokmama »
        TellRawGenerator builder = new TellRawGenerator();
        builder.append(handler.getPrefix(event.getPlayer())+" ", true);
        builder.append(event.getPlayer().getDisplayName(), true, event.getPlayer().getName());
        builder.append("&f>");
        builder.append(fixString);

        String msg = builder.toString();
        event.getPlayer().getWorld().getPlayers().forEach(p->{
            StringBuilder buf = new StringBuilder();
            buf.append("tellraw ").append(p.getName()).append(" ").append(msg);

            Threading.postToServerThread(new Threading.Task(this) {
                @Override
                public void execute() {
                    p.performCommand(buf.toString());
                }
            });

        });

        if(discordSRV!=null) {
            discordSRV.processChatMessage(event.getPlayer(), fixString, DiscordSRV.getPlugin().getChannels().size() == 1 ? null : "global", false);
        }
    }

}
