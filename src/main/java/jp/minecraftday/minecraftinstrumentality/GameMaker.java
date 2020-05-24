package jp.minecraftday.minecraftinstrumentality;

import com.sun.jna.CallbackParameterContext;
import jp.minecraftday.minecraftinstrumentality.command.GameCommandExecutor;
import jp.minecraftday.minecraftinstrumentality.utils.TitleSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import javax.security.auth.callback.CallbackHandler;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GameMaker implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("gamemaker");
    private static final String OBJECTIVE_NAME = "minecraftday.mg";
    private GameCommandExecutor gameCommandExecutor;
    private Scoreboard scoreboard;
    private Objective sidebar;
    private String rule;
    final String hostPlayerName;
    final Set<String> players = new HashSet<>();
    Future future;
    boolean cancel = false;
    long startedTime = 0;
    int time = 5;
    int max = 6;
    TitleSender titleSender = new TitleSender();

    public static String[][] RULES = {
            {"mob", "totalKillCount"},
            {"pvp", "playerKillCount"},
            {"fly", "minecraft.custom:minecraft.aviate_one_cm"},
            {"cake", "minecraft.crafted:minecraft.cake"},
            {"diamond", "minecraft.mined:diamond_ore:"},
            {"zombie", "minecraft.killed:minecraft.zombie"}
    };

    public static ChatColor[] teamColors = {
            ChatColor.WHITE,
            ChatColor.RED,
            ChatColor.BLUE,
            ChatColor.GREEN,
            ChatColor.YELLOW,
            ChatColor.LIGHT_PURPLE,
            ChatColor.GRAY,
            ChatColor.GOLD
    };

    public GameMaker(GameCommandExecutor executor, Player player, String rule) {
        this.gameCommandExecutor = executor;
        this.hostPlayerName = player.getName();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        if(rule!=null){
            this.rule = rule;
            for(int i=0; i<RULES.length; i++){
                if(RULES[i][0].equalsIgnoreCase(rule)){
                    this.rule = RULES[i][1];
                    break;
                }
            }
        }else{
            this.rule = "dummy";
        }
        LOGGER.info(this.rule+"でゲームを生成しました");
        this.sidebar = scoreboard.registerNewObjective(OBJECTIVE_NAME, this.rule, "");
        this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(scoreboard);
        updateSidebarName();
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

    public int getTime(){
        return this.time;
    }

    public void setMaxPlayer(int max) {
        this.max = max;
    }

    public int getMaxPlayer() {
        return max;
    }

    public Set<String> getPlayers(){ return players;}

    private ChatColor getColor(String team){
        if(team!=null) {
            for (int i = 0; i < teamColors.length; i++) {
                if (teamColors[i].name().equalsIgnoreCase(team)) return teamColors[i];
            }
        }
        return null;
    }

    private Team createTteam(ChatColor color){
        Team team = scoreboard.getTeam(color.name());
        if(team==null){
            team = scoreboard.registerNewTeam(color.name());
            team.setDisplayName(color.name());
            team.setColor(color);
            team.setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.ALWAYS);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
            team.setPrefix(color.toString());
            team.setSuffix(ChatColor.RESET.toString());
            team.setAllowFriendlyFire(false);
            team.setCanSeeFriendlyInvisibles(true);
        }
        return team;
    }

    public boolean setTeam(Player player, String teamColor) {
        ChatColor color = getColor(teamColor);
        if(color!=null) {
            Team team = createTteam(color);
            team.addEntry(fixName17(player.getName()));
            return true;
        }else{
            return false;
        }
    }

    public void addPlayer(Player player, String teamColor){
        sidebar.getScore(fixName17(player.getName())).setScore(0);
        this.players.add(player.getName());

        //チームの指定がない場合は白にいれる
        ChatColor color = getColor(teamColor);
        if(color == null) color = ChatColor.WHITE;

        Team team = createTteam(color);
        team.addEntry(fixName17(player.getName()));
        player.setScoreboard(scoreboard);
    }

    public void removePlayer(Player player){
        this.players.remove(player.getName());

        for(Iterator<Team> iterator=this.scoreboard.getTeams().iterator(); iterator.hasNext(); ){
            Team t = iterator.next();
            if(t.hasEntry(fixName17(player.getName()))){
                t.removeEntry(fixName17(player.getName()));
                break;
            }
        }

        scoreboard.resetScores(fixName17(player.getName()));
        if(!player.getName().equals(hostPlayerName)){
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public String fixName17(String name){
        if ( name.length() > 17 ) {
            return name.substring(0, 17);
        }
        return name;
    }

    public String getHostPlayerName() {
        return hostPlayerName;
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

    public void release(){
        if(scoreboard != null){
            if(sidebar!=null){
                sidebar.unregister();
            }
            scoreboard.getTeams().stream().forEach(team -> team.unregister());
        }
    }

    public boolean cancel() {
        try {
            cancel = true;
            if (future != null) {
                future.cancel(true);
                future = null;

                return true;
            }
            return false;
        }finally {
            //release();
        }
    }

    public void finish() {
        cancel();
        release();

        players.stream().forEach(playerName->{
            scoreboard.resetScores(playerName);
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                sendMessage(player.getName(), "&6ゲームを解散しました");
            }
        });
        if(!isJoining(hostPlayerName)){
            sendMessage(hostPlayerName, "&6ゲームを解散しました");
        }
    }


    public void start(ExecutorService pool) {
        //スコアのリセット
        scoreboard.getEntries().stream().forEach(e->sidebar.getScore(e).setScore(0));

        this.future = pool.submit(this);
        showTitle("ゲームスタート！", "");
    }

    public void end() {
        showTitle("ゲーム終了", "");
        this.future = null;
        //finish();
    }

    public void gather() {
        Player host = Bukkit.getPlayerExact(hostPlayerName);
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
        if (!players.contains(hostPlayerName)) {
            Player player = Bukkit.getPlayerExact(hostPlayerName);
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
                    //gameCommandExecutor.remove(hostPlayerName);
                    break;
                }

                updateSidebarName();

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            LOGGER.info("Interrupted - "
                    + Thread.currentThread().getId());
        }
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

}
