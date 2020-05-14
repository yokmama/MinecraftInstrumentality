package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.GameMaker;
import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.command.sub.*;
import jp.minecraftday.minecraftinstrumentality.core.MainCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameCommandExecutor extends MainCommandExecutor {
    static AtomicInteger ids = new AtomicInteger();
    final ExecutorService pool;
    static Map<String, GameMaker> games = new ConcurrentHashMap<>();

    public static List<String> getGames(){
        return games.values().stream().map(g->g.getHostplayer()).collect(Collectors.toList());
    }

    public GameCommandExecutor(Main ref) {
        super(ref);

        pool = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "MinecraftDay Thread " + ids.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });

        addSubCommand(new MinigameSet(this));
        addSubCommand(new MinigameJoin(this));
        addSubCommand(new MinigameFinish(this));
        addSubCommand(new MinigameAdd(this));
        addSubCommand(new MinigameKick(this));
        addSubCommand(new MinigameList(this));
        addSubCommand(new MinigameStart(this));
        addSubCommand(new MinigameGather(this));
    }

    @Override
    public String getName() {
        return "minigame";
    }

    @Override
    public String getPermission() {
        return "minecraftday.mg";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        GameMaker hostingGame = getGameMaker(sender.getName());
        if (hostingGame != null) {
            List<String> list = super.onTabComplete(sender, command, alias, args);
            if (hostingGame.isInGame()) {
                list.remove("cancel");
            }
            GameMaker gameMaker = getJoiningGame(sender.getName());
            if (gameMaker == null && hostingGame != null) {
                list.remove("list");
            }

            return list;
        } else if( args.length == 1){
            return Arrays.asList("set");
        }

        return super.onTabComplete(sender, command, alias, args);
    }

    public ExecutorService getPool() { return pool; }

    public boolean isShutdown() {
        return pool.isShutdown();
    }

    public GameMaker getGameMaker(String name) {
        return games.get(name);
    }

    public void putGameMaker(String name, GameMaker gameMaker) {
        games.put(name, gameMaker);
    }

    public GameMaker getJoiningGame(String player) {
        for (GameMaker g : games.values()) {
            if (g.isJoining(player))
                return g;
        }
        return null;
    }

    public void remove(String playerName) {
        games.remove(playerName);
    }

    public void onLogin(Player player) {
        GameMaker gameMaker = getJoiningGame(player.getName());
        if (gameMaker != null) {
            player.setScoreboard(gameMaker.getScoreboard());
        } else {
            gameMaker = getGameMaker(player.getName());
            if (gameMaker != null) {
                player.setScoreboard(gameMaker.getScoreboard());
            }
        }
    }
}
