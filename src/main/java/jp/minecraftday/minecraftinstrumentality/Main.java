package jp.minecraftday.minecraftinstrumentality;

import com.earth2me.essentials.perm.PermissionsHandler;
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
import java.util.stream.Collectors;

public final class Main extends JavaPlugin implements Listener {
    GameCommandExecutor gameCommandExecutor;
    UserConfiguration userConfiguration;

    private JavaPlugin discordSRV = null;
    private JavaPlugin essentials = null;

    @Override
    public void onEnable() {
        super.onEnable();

        gameCommandExecutor = new GameCommandExecutor(this);

        userConfiguration = new UserConfiguration(getDataFolder());

        final PluginManager pluginManager = getServer().getPluginManager();
        Plugin ess = pluginManager.getPlugin("Essentials");
        if (ess != null){// && ess.isEnabled()) {
            essentials = (JavaPlugin) ess;
        }

        Plugin srv = pluginManager.getPlugin("DiscordSRV");
        if (srv != null){// && srv.isEnabled()) {
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
        getCommand("voteauto").setExecutor(vote);

        ChatCommandExecutor chat = new ChatCommandExecutor(this);
        getCommand("hiragana").setExecutor(chat);
        getCommand("shout").setExecutor(chat);

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
        broadcastMessage(event.getPlayer(), event.getMessage(), false);
    }

    public void broadcastMessage(Player sender, String msg, boolean isShout) {
        Configuration configuration = getUserConfiguration(sender);
        boolean hiragana = configuration!=null?configuration.getBoolean("hiragana"): false;
        if (hiragana) {
            msg = new KanaConverter().convert(msg);
        }

        String color = "&f";
        final Collection<? extends Player> players;
        if (essentials != null) color = new EssentialsHandler(essentials).getChatColor(sender);
        if(isShout) {
            color = "&c";
            players = null;//Bukkit.getOnlinePlayers();
        } else {
            final GameMaker hostGame = gameCommandExecutor.getGameMaker(sender);
            final GameMaker joinGame = gameCommandExecutor.getJoiningGame(sender.getName());
            if(hostGame != null){
                color = "&a";
            }

            players = Bukkit.getOnlinePlayers().stream().filter(pl->{
                if (sender.getName().equals(pl.getName())) return true;
                else if (joinGame != null) return joinGame.isJoining(pl.getName());
                else if (hostGame!=null) return hostGame.isJoining(pl.getName()) || gameCommandExecutor.getJoiningGame(pl.getName()) == null;
                else return gameCommandExecutor.getJoiningGame(pl.getName()) == null;
            }).collect(Collectors.toList());
        }

        boolean useRaw = false; //ホバーで名前を表示するメッセージは一度やってみたけど微妙だったのでフラグでオフにしています
        final String fixedMsg;
        if(useRaw){
            //ex: Gm yokmama »
            TextRawGenerator builder = new TextRawGenerator();
            if (essentials != null) {
                builder.append(new EssentialsHandler(essentials).getPrefix(sender) + " ", true);
            }
            builder.append(sender.getDisplayName(), true, sender.getName());
            builder.append(color+">");
            builder.append(msg);
            fixedMsg = builder.toString();
        }else {
            StringBuilder builder = new StringBuilder();

            if (essentials != null) {
                builder.append(new EssentialsHandler(essentials).getPrefix(sender) + " ");
            }
            builder.append(sender.getDisplayName());
            builder.append(color+">");
            builder.append(msg);
            fixedMsg = ChatColor.translateAlternateColorCodes('&',builder.toString());
        }

        if(players != null) {
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
        }else{
            Bukkit.broadcastMessage(fixedMsg);
        }

        if (discordSRV != null) {
            new DiscordSRVHandler(discordSRV).processChatMessage(sender, msg, false);
        }

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
