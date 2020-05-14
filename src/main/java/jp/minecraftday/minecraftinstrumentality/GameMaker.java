package jp.minecraftday.minecraftinstrumentality;

import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.utils.TitleSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class GameMaker implements Runnable {
    private static final Logger logger = Logger.getLogger("gamemaker");
    private static final String OBJECTIVE_NAME = "minecraftday.mg";
    private GameCommandExecutor gameCommandExecutor;
    private Scoreboard scoreboard;
    private Objective sidebar;
    final String hostplayer;
    final Set<String> players = new HashSet<>();
    Future future;
    boolean cancel = false;
    long startedTime;
    int time;
    int max;
    TitleSender titleSender = new TitleSender();

    public GameMaker(GameCommandExecutor gameCommandExecutor, Player hostplayer, String rule) {
        this.gameCommandExecutor = gameCommandExecutor;
        this.hostplayer = hostplayer.getName();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        if(rule.equalsIgnoreCase("pvp")) rule = "playerKillCount";
        else if(rule.equalsIgnoreCase("mob")) rule = "totalKillCount";
        else if(rule.equalsIgnoreCase("life")) rule = "health";
        sidebar = scoreboard.registerNewObjective(OBJECTIVE_NAME, rule, "");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        hostplayer.setScoreboard(scoreboard);
    }

    public void updateSidebarName() {
        StringBuilder name = new StringBuilder();

        if(startedTime!=0) {
            if(time>0) {
                long limitTime = (time * 60 * 1000) - (Calendar.getInstance().getTime().getTime() - startedTime);
                int m = new Double(Math.floor(limitTime / (60 * 1000))).intValue();
                int s = new Double(Math.floor((limitTime - m * (60 * 1000)) / 1000)).intValue();

                name.append(String.format("残り時間:%02d:%02d", m, s));
            }else{
                long elapsedTime = Calendar.getInstance().getTime().getTime() - startedTime;
                int m = new Double(Math.floor(elapsedTime / (60 * 1000))).intValue();
                int s = new Double(Math.floor((elapsedTime - m * (60 * 1000)) / 1000)).intValue();
                name.append(String.format("経過時間:%02d:%02d", m, s));
            }
        }else {
            if(time>0) {
                name.append(String.format("制限時間:%02d:00", time));
            }else{
                name.append("時間制限なし");
            }
        }

        sidebar.setDisplayName(name.toString());
    }

    public void setTime(int time) {
        this.time = time;
        this.startedTime = 0;
        this.cancel = false;
        updateSidebarName();
    }

    public void setMaxPlayer(int max) {
        this.max = max;
    }

    public int getMaxPlayer() {
        return max;
    }

    public Set<String> getPlayers(){ return players;}

    public void addPlayer(Player player){
        sidebar.getScore(player.getName()).setScore(1);

        this.players.add(player.getName());

        player.setScoreboard(scoreboard);
    }

    public void removePlayer(Player player){
        this.players.remove(player.getName());

        removeScores(player.getName());
        if(!player.getName().equals(hostplayer)){
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @SuppressWarnings("deprecation")
    private Score getScoreItem(Objective obj, String name) {
        if ( name.length() > 16 ) {
            name = name.substring(0, 16);
        }
        return obj.getScore(name);
    }

    @SuppressWarnings("deprecation")
    private void removeScores(String name) {
        if ( name.length() > 16 ) {
            name = name.substring(0, 16);
        }
        scoreboard.resetScores(name);
    }

    public void setScoreItem(String name, int setNo) {
        sidebar.getScore(name).setScore(setNo);
    }

    public String getHostplayer() {
        return hostplayer;
    }

    public boolean isInGame() {
        return future != null;
    }

    public boolean isJoining(String name) {
        for (String p : players) {
            if (p.equals(name)) return true;
        }
        return false;
    }

    public boolean cancel() {
        cancel = true;
        if (future != null) {
            future.cancel(true);
            future = null;

            return true;
        }
        return false;
    }

    public void finish() {
        cancel();

        players.stream().forEach(playerName->{
            scoreboard.resetScores(playerName);
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                sendMessage(player.getName(), "&6ゲームを解散しました");
            }
        });
        if(!isJoining(hostplayer)){
            sendMessage(hostplayer, "&6ゲームを解散しました");
        }
    }


    public void start(ExecutorService pool) {
        this.future = pool.submit(this);
        showTitle("ゲームスタート！", "");
    }

    public void end() {
        showTitle("ゲーム終了", "");
        finish();
    }

    public void gather() {
        Player host = Bukkit.getPlayerExact(hostplayer);
        Location l = host.getLocation();
        for (String p : players) {
            Player player = Bukkit.getPlayerExact(p);
            if (player != null && player != host) {
                player.teleport(l);
            }
        }
    }

    public void showTitle(String title, String subtitle) {
        for (String p : players) {
            Player player = Bukkit.getPlayerExact(p);
            if (player != null) {
                titleSender.sendTitle(player, "§b" + title, "§3" + subtitle, "");
            }
        }
        //ホストにも通知する
        if (!players.contains(hostplayer)) {
            Player player = Bukkit.getPlayerExact(hostplayer);
            if (player != null) {
                titleSender.sendTitle(player, "§b" + title, "§3" + subtitle, "");
            }
        }
    }

    public void showActionbar(String actionbar) {
        for (String p : players) {
            Player player = Bukkit.getPlayerExact(p);
            if (player != null) {
                titleSender.sendTitle(player, null, null, "§b"+actionbar);
            }
        }
    }

    public void sendMessage(String playerName, String msg) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }

    @Override
    public void run() {
        startedTime = Calendar.getInstance().getTime().getTime();
        long TIME_SPAN = time * 60 * 1000;
        try {
            while (!gameCommandExecutor.isShutdown()) {
                long distance = Calendar.getInstance().getTime().getTime() - startedTime;
                if (time>0 && distance >= TIME_SPAN) {
                    //終了処理
                    end();
                    gameCommandExecutor.remove(hostplayer);
                    break;
                }

                updateSidebarName();

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.info("Interrupted - "
                    + Thread.currentThread().getId());
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

}
