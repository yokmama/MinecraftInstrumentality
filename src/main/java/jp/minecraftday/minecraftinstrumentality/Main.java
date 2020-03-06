package jp.minecraftday.minecraftinstrumentality;

import jp.minecraftday.minecraftinstrumentality.command.*;
import jp.minecraftday.minecraftinstrumentality.plugin.DiscordSRVHandler;
import jp.minecraftday.minecraftinstrumentality.plugin.EssentialsHandler;
import jp.minecraftday.minecraftinstrumentality.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main extends JavaPlugin implements Listener {
    GameCommandExecutor gameCommandExecutor;
    UserConfiguration userConfiguration;
    static AtomicInteger counter = new AtomicInteger();
    static Map<String, Integer> playerIDs = new ConcurrentHashMap<>();

    private JavaPlugin discordSRV = null;
    private JavaPlugin essentials = null;

    @Override
    public void onEnable() {
        super.onEnable();

        gameCommandExecutor = new GameCommandExecutor(this);

        userConfiguration = new UserConfiguration(getDataFolder());

        final PluginManager pluginManager = getServer().getPluginManager();
        Plugin ess = pluginManager.getPlugin("Essentials");
        if (ess != null && !ess.isEnabled()) {
            essentials = (JavaPlugin) ess;
        }

        Plugin srv = pluginManager.getPlugin("DiscordSRV");
        if (srv != null && !srv.isEnabled()) {
            discordSRV = (JavaPlugin) srv;
        }

        //イベントリスナー登録
        getServer().getPluginManager().registerEvents(this, this);
        //コマンド登録
        getCommand("md").setExecutor(new MainCommandExecutor(this));
        getCommand("minigame").setExecutor(gameCommandExecutor);
        getCommand("setitem").setExecutor(new SetItemNameCommandExecutor(this));
        CommandExecutor vote = new VoteCommandExecutor(this);
        getCommand("vote").setExecutor(vote);
        getCommand("voteyes").setExecutor(vote);
        getCommand("voteno").setExecutor(vote);

        ChatCommandExecutor chat = new ChatCommandExecutor(this);
        getCommand("hiragana").setExecutor(chat);

        saveDefaultConfig();
    }

    public Configuration getUserConfiguration(Player player) {
        return userConfiguration.getUserConfig(player);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        Integer playerID = playerIDs.get(event.getPlayer().getName());
        if (playerID == null) {
            playerID = counter.incrementAndGet();
            playerIDs.put(event.getPlayer().getName(), playerID);
        }

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
        broadcastMessage(event.getPlayer(), event.getMessage(), true);
    }

    public void broadcastMessage(Player sender, String msg, boolean allworld) {
        Configuration configuration = getUserConfiguration(sender);
        boolean hiragana = configuration.getBoolean("hiragana");
        if (hiragana) {
            msg = KanaConverter.conv(msg);
        }

        boolean useRaw = false;

        final String fixedMsg;
        if(useRaw){
            //ex: Gm yokmama »
            TextRawGenerator builder = new TextRawGenerator();
            if (essentials != null) {
                builder.append(new EssentialsHandler(essentials).getPrefix(sender) + " ", true);
            }
            builder.append(sender.getDisplayName(), true, sender.getName());
            builder.append("&f>");
            builder.append(msg);
            fixedMsg = builder.toString();
        }else {
            StringBuilder builder = new StringBuilder();

            if (essentials != null) {
                builder.append(new EssentialsHandler(essentials).getPrefix(sender) + " ");
            }
            builder.append(sender.getDisplayName());
            builder.append("&f>");
            builder.append(msg);
            fixedMsg = ChatColor.translateAlternateColorCodes('&',builder.toString());
        }
        Collection<? extends Player> players = allworld ? Bukkit.getOnlinePlayers() : sender.getWorld().getPlayers();
        players.forEach(p -> {
            if (useRaw) {
                StringBuilder buf = new StringBuilder();
                buf.append("tellraw ").append(p.getName()).append(" ").append(fixedMsg);
                Threading.postToServerThread(new Threading.Task(this) {
                    @Override
                    public void execute() {
                        p.performCommand(buf.toString());
                    }
                });
            } else {
                p.sendMessage(fixedMsg);
            }
        });

        if (discordSRV != null) {
            new DiscordSRVHandler(discordSRV).processChatMessage(sender, msg, false);
        }

    }

    public Integer getPlayerNo(String name){
        Integer playerID = playerIDs.get(name);
        if (playerID == null) return null;
        return playerID;
    }

    public Boolean isMuted(Player player) {
        if (essentials != null) return new EssentialsHandler(essentials).isMuted(player);
        return null;
    }

    public Boolean isJailed(Player player) {
        if (essentials != null) return new EssentialsHandler(essentials).isJailed(player);
        return null;
    }
}
