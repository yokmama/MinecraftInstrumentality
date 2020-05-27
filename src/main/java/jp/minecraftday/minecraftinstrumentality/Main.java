package jp.minecraftday.minecraftinstrumentality;

import jp.minecraftday.minecraftinstrumentality.command.*;
import jp.minecraftday.minecraftinstrumentality.core.MainPlugin;
import jp.minecraftday.minecraftinstrumentality.core.utils.I18n;
import jp.minecraftday.minecraftinstrumentality.login.BasicIncome;
import jp.minecraftday.minecraftinstrumentality.plugin.DiscordSRVHandler;
import jp.minecraftday.minecraftinstrumentality.plugin.EssentialsHandler;
import jp.minecraftday.minecraftinstrumentality.plugin.MultiverseHandler;
import jp.minecraftday.minecraftinstrumentality.utils.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public final class Main extends MainPlugin implements Listener {
    GameCommandExecutor gameCommandExecutor;
    UserConfiguration userConfiguration;

    private JavaPlugin discordSRV = null;
    private JavaPlugin essentials = null;
    private JavaPlugin worldEdit = null;
    private NgMatcher ngMatcher;
    private DesignMarkDatabase designMarkDb;

    private Economy econ = null;
    private Permission perms = null;
    private Chat chat = null;



    public JavaPlugin getWorldEdit(){ return worldEdit;}

    public DesignMarkDatabase getDesignMarkDb(){ return designMarkDb;}

    @Override
    public void onEnable() {
        super.onEnable();

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            LOGGER.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupEconomy();
        setupPermissions();
        setupChat();

        gameCommandExecutor = new GameCommandExecutor(this);

        userConfiguration = new UserConfiguration(getDataFolder());

        designMarkDb = new DesignMarkDatabase(this);
        designMarkDb.connect();


        final PluginManager pluginManager = getServer().getPluginManager();

        Plugin ess = pluginManager.getPlugin("Essentials");
        if (ess != null){// && ess.isEnabled()) {
            this.essentials = (JavaPlugin) ess;
        }

        Plugin srv = pluginManager.getPlugin("DiscordSRV");
        if (srv != null){// && srv.isEnabled()) {
            this.discordSRV = (JavaPlugin) srv;
        }

        Plugin worldEdit = pluginManager.getPlugin("WorldEdit");
        if (worldEdit != null){
            this.worldEdit = (JavaPlugin) worldEdit;
        }

        //イベントリスナー登録
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new BasicIncome(this), this);
        //コマンド登録
        getCommand("md").setExecutor(new MineCraftDayCommandExecutor(this));
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
        getCommand("estate").setExecutor(new EstateCommandExecutor(this));

        getCommand("designmark").setExecutor(new DesignMarkCommandExecutor(this));


        ngMatcher = new NgMatcher(getConfig().getStringList("ng.words").toArray(new String[0]));

        saveDefaultConfig();
    }

    public Configuration getUserConfiguration(Player player) {
        return userConfiguration.getUserConfig(player);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        designMarkDb.disconnect();
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        String msg = getConfig().getString("welcome.message");
        if (msg == null) msg = "";

        event.getPlayer().sendMessage(I18n.tl("message.md.welcome1"));
        StringBuilder buf = new StringBuilder();
        buf.append("tellraw ").append(event.getPlayer().getName()).append(" ").append(msg);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), buf.toString());
//        event.getPlayer().performCommand(buf.toString());
        event.getPlayer().sendMessage(I18n.tl("message.md.welcome2"));

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

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

    public void broadcastMessage(Player sender, String msg, boolean isShout) {
        Configuration userConfiguration = getUserConfiguration(sender);

        boolean skipJapanize = false;
        if ( msg.startsWith("#") ) {
            skipJapanize = true;
            msg = msg.substring("#".length());
        }

        if(!skipJapanize) {
            //ひらがな処理
            boolean hiragana = userConfiguration != null ? userConfiguration.getBoolean("hiragana") : false;
            if (hiragana) {
                msg = new KanaConverter().convert(msg);
            }
        }

        final int cooldown = getConfig().getInt("ng.cooldown");
        final int maxtime = getConfig().getInt("ng.maxtime");

        long ngtime = userConfiguration.getLong("ng.time");
        int ngcount = userConfiguration.getInt("ng.count");
        if(ngtime>0){
            long sec = new Double(Math.pow(cooldown, ngcount)).longValue();
            if(sec>maxtime) sec = maxtime;
            long now = Calendar.getInstance().getTimeInMillis();
            long limitTime = ngtime + sec*1000;
            if(now < limitTime){
                sender.sendMessage(I18n.tl("message.md.ngword.mute"));
                return;
            } else {
                userConfiguration.set("ng.time", 0);
                userConfiguration.save();
            }
        }

        //NGワードチェック
        String[] msgs = msg.split(" ");
        for(int i=0; i<msgs.length; i++) {
            if(ngMatcher.match(msgs[i])){
                if(ngMatcher.getState() == 1){
                    //完全一致は問答無用でMute
                    long sec = new Double(Math.pow(cooldown, ngcount)).longValue();
                    if (sec > maxtime) sec = maxtime;
                    long now = Calendar.getInstance().getTimeInMillis();
                    long limitTime = now + sec * 1000 * 2;

                    sender.sendMessage(I18n.tl("message.md.ngword.detect"));
                    userConfiguration.set("ng.time", now);
                    if (now < limitTime) {
                        userConfiguration.set("ng.count", ngcount + 1);
                    } else {
                        userConfiguration.set("ng.count", 1);
                    }
                    userConfiguration.save();
                    return;
                }else{
                    //含んでいる場合はマスク
                    msg = ngMatcher.getMaskString(msg);
                }
            }
        }
        /*
        List<String> ngwords = getConfig().getStringList("ng.words");
        for(Iterator<String> ite = ngwords.iterator(); ite.hasNext();){
            String ngword = ite.next();
            if(msg.contains(ngword)){
                //
                long sec = new Double(Math.pow(cooldown, ngcount)).longValue();
                if(sec>maxtime) sec = maxtime;
                long now = Calendar.getInstance().getTimeInMillis();
                long limitTime = now + sec*1000*2;

                sender.sendMessage(I18n.tl("message.md.ngword.detect"));
                userConfiguration.set("ng.time", now);
                if(now < limitTime) {
                    userConfiguration.set("ng.count", ngcount+1);
                }else{
                    userConfiguration.set("ng.count", 1);
                }
                userConfiguration.save();
                return;
            }
        }*/




        String color = "&f";
        final Collection<? extends Player> players;
        if (essentials != null) color = new EssentialsHandler(essentials).getChatColor(sender);
        if (isShout) {
            color = "&c";
            players = null;//Bukkit.getOnlinePlayers();
        } else {
            final GameMaker hostGame = gameCommandExecutor.getGameMaker(sender.getName());
            final GameMaker joinGame = gameCommandExecutor.getJoiningGame(sender.getName());
            if (hostGame != null) {
                color = "&a";
            }

            players = Bukkit.getOnlinePlayers().stream().filter(pl -> {
                if (sender.getName().equals(pl.getName())) return true;
                else if (joinGame != null) return joinGame.isJoining(pl.getName());
                else if (hostGame != null)
                    return hostGame.isJoining(pl.getName()) || gameCommandExecutor.getJoiningGame(pl.getName()) == null;
                else return gameCommandExecutor.getJoiningGame(pl.getName()) == null;
            }).collect(Collectors.toList());
        }

        StringBuilder builder = new StringBuilder();

        if (essentials != null) {
            builder.append(new EssentialsHandler(essentials).getPrefix(sender) + " ");
        }
        builder.append(sender.getDisplayName());
        builder.append(color + ">");
        builder.append(msg);
        final String fixedMsg = ChatColor.translateAlternateColorCodes('&', builder.toString());

        if (players != null) {
            players.forEach(p -> {
                p.sendMessage(fixedMsg);
            });
        } else {
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

    public Economy getEcon(){ return econ;}
}
