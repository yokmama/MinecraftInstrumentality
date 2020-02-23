package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.Threading;
import jp.minecraftday.minecraftinstrumentality.utils.TitleSender;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameCommandExecutor implements CommandExecutor {
    private static final Logger logger = Logger.getLogger("minecraftday");
    final Main plugin;
    static AtomicInteger ids = new AtomicInteger();
    final ExecutorService pool;
    static Map<String, GameMaker> games = new ConcurrentHashMap<>();
    TitleSender titleSender = new TitleSender();;


    public GameCommandExecutor(Main ref) {
        plugin = ref;

        pool = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "MinecraftDay Thread " + ids.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mg") && sender instanceof Player) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("set")) {
                initGame((Player)sender, Arrays.copyOfRange(args, 1, args.length));
            }
            else if(cmd0.equals("start")) {
                startGame((Player)sender, Arrays.copyOfRange(args, 1, args.length));
            }
            else if(cmd0.equals("cancel")){
                cancelGame((Player)sender);
            }

            return true;
        }
        return false;
    }

    private void initGame(Player hostplayer, String[] players){
        GameMaker gameMaker = games.get(hostplayer.getName());
        if(gameMaker != null && gameMaker.isInGame()){
            hostplayer.sendMessage("すでにゲームがスタートしています。一度キャンセルしましょう");
            return;
        }

        List<String> players_list = new ArrayList<>();
        for (String name :players){
            if(checkJoiningOtherGame(gameMaker, name)){
                hostplayer.sendMessage(name+"さんが他のゲームに参加しています");
                return;
            }

            Player targetplayer = findPlayer(name);
            if(targetplayer!=null) {
                players_list.add(name);
            }
        }
        if(players_list.size() == 0){
            hostplayer.sendMessage("参加できるプレイヤーが一人もいません");
            return;
        }

        gameMaker = new GameMaker(hostplayer, players_list);
        games.put(hostplayer.getName(), gameMaker);
        gameMaker.showTitle("ゲームの準備はいい？");
    }

    private void startGame(Player player, String[] cmds) {
        GameMaker gameMaker = games.get(player.getName());
        if(gameMaker!=null) {
            int time = 0;
            if (cmds.length > 0) {
                try {
                    time = Integer.parseInt(cmds[0]);
                } catch (Exception e) {
                    player.sendMessage("ゲーム時間は数字でいれてください");
                    return;
                }
            } else {
                player.sendMessage("/game start <ゲーム時間>");
                return;
            }
            gameMaker.start(pool, time);
        }else{
            player.sendMessage("/game set 参加プレイヤー名・・・");
        }
    }

    private void cancelGame(Player player){
        GameMaker gameMaker = games.get(player.getName());
        if(gameMaker != null) {
            if(gameMaker.isInGame()){
                gameMaker.setCancel();
            }
            games.remove(player.getName());
        }
    }

    private boolean checkJoiningOtherGame(GameMaker gameMaker, String s){
        for(GameMaker g : games.values()){
            if((gameMaker == null || gameMaker!= g) && g.isJoining(s)) return true;
        }
        return false;
    }

    private Player findPlayer(String name){
        return Bukkit.getPlayerExact(name);
    }

    public class GameMaker implements Runnable{
        final String hostplayer;
        final List<String> players;
        Future future;
        boolean cancel = false;
        long startedTime;
        int time;
        Location location;

        public GameMaker(Player hostplayer, List<String> players_list) {
            this.hostplayer = hostplayer.getName();
            this.players = players_list;
            this.location = new Location(
                    hostplayer.getLocation().getWorld(),
                    hostplayer.getLocation().getX(),
                    hostplayer.getLocation().getY(),
                    hostplayer.getLocation().getZ());
        }

        String getHostplayer(){ return hostplayer;}

        boolean isInGame(){ return future!=null; }

        boolean isJoining(String name){
            for (String p: players) {
                if(p.equals(name)) return true;
            }
            if(hostplayer.equals(name)) return true;
            return false;
        }

        public void setCancel() {
            if(future!=null) {
                future.cancel(true);
                future = null;
            }
            cancel = true;
        }


        public void start(ExecutorService pool, int time) {
            this.time = time;
            this.future = pool.submit(this);
            showTitle("ゲームスタート！");
        }

        public void end(){
            showTitle("ゲーム終了");
            for(String p : players)
                Threading.postToServerThread(new Threading.Task(plugin) {
                    @Override
                    public void execute() {
                        Player player = findPlayer(p);
                        if(player!=null) {
                            player.teleport(location);
                        }
                    }
                });
        }

        void showTitle(String msg){
            for(String p : players){
                Player player = findPlayer(p);
                if(player!=null) {
                    titleSender.sendTitle(player, msg, "", "");
                }
            }
        }

        void broadcastMessage(String msg){
            for(String p : players){
                Player player = findPlayer(p);
                if(player!=null) {
                    player.sendMessage("<Bot> " + msg);
                }
            }
        }

        @Override
        public void run() {
            startedTime = Calendar.getInstance().getTime().getTime();
            long TIME_SPAN = time*60*1000;
            long harfTime = new Double(Math.ceil(time/2.0)).longValue();
            try {
                while(!pool.isShutdown()){
                    long distance = Calendar.getInstance().getTime().getTime() - startedTime;
                    if(distance>=TIME_SPAN){
                        //終了処理
                        end();
                        games.remove(hostplayer);
                        break;
                    }

                    long m = new Double(Math.ceil(distance/(60*1000))).longValue();
                    long lm = time - m;
                    long s = (distance - m*(60*1000))/1000;

                    //logger.info(lm + ":"+ s + "("+harfTime+")");
                    if(lm == harfTime && s == 0){
                        //半分経過
                        broadcastMessage("残り"+lm+"分です");
                    }else if( m == (time-1)){
                        //残り一分以内
                        if(s == 0){
                            //残り1分
                            broadcastMessage("残り1分です");
                        }else if ( s == 30){
                            //残り30秒
                            broadcastMessage("残り30秒です");
                        }else if ( s == 45){
                            //残り15秒
                            broadcastMessage("残り15秒です");
                        }else if ( s >= 50){
                            //残り10秒カウントダウン
                            long t = 60 - s;
                            if(t == 0){
                                //終了処理
                                end();
                                games.remove(hostplayer);
                                break;
                            }else{
                                //メッセージ
                                broadcastMessage(Long.toString(t));
                            }
                        }
                    }


                    Thread.sleep(1000);

                }
            } catch (InterruptedException e) {
                logger.info("Interrupted - "
                        + Thread.currentThread().getId());
            }
        }
    }

}
