package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.Threading;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class GameCommandExecutor implements CommandExecutor {
    final Main plugin;
    static AtomicInteger ids = new AtomicInteger();
    final ExecutorService pool;
    static Map<String, GameMaker> games = new ConcurrentHashMap<>();


    public GameCommandExecutor(Main ref) {
        plugin = ref;

        pool = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "MinecraftDay Game Execution Thread " + ids.incrementAndGet());
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

        List<Player> players_list = new ArrayList<>();
        players_list.add(hostplayer);
        for (String name :players){
            if(!hostplayer.getName().equals(name)) {
                if(checkJoiningOtherGame(gameMaker, name)){
                    hostplayer.sendMessage(name+"さんが他のゲームに参加しています");
                    return;
                }

                Player targetplayer = Bukkit.getServer().getPlayer(name);
                players_list.add(targetplayer);
            }
        }

        gameMaker = new GameMaker(hostplayer, players_list);
        games.put(hostplayer.getName(), gameMaker);
        gameMaker.broadcast("ゲームの準備はいいですか？");
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
            player.sendMessage("/game init 参加プレイヤー名・・・");
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

    public class GameMaker implements Runnable{
        final Player hostplayer;
        final List<Player> players;
        Future future;
        boolean cancel = false;
        long startedTime;
        int time;
        Location location;

        public GameMaker(Player hostplayer, List<Player> players_list) {
            this.hostplayer = hostplayer;
            this.players = players_list;
            this.location = new Location(
                    hostplayer.getLocation().getWorld(),
                    hostplayer.getLocation().getX(),
                    hostplayer.getLocation().getY(),
                    hostplayer.getLocation().getZ());
        }

        Player getHostplayer(){ return hostplayer;}

        boolean isInGame(){ return future!=null; }

        boolean isJoining(String name){
            for (Player p: players) {
                if(p.getName().equals(name)) return true;
            }
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
            broadcast("ゲームスタート！");
        }

        public void end(){
            broadcast("ゲーム終了");
            for(Player p : players)
                Threading.postToServerThread(new Threading.Task(plugin) {
                    @Override
                    public void execute() {
                        p.teleport(location);
                    }
                });
        }

        void broadcast(String msg){
            for(Player p : players){
                p.sendMessage(msg);
                System.out.println(msg);
            }
        }

        @Override
        public void run() {
            System.out.println("######################## start");
            startedTime = Calendar.getInstance().getTime().getTime();
            long TIME_SPAN = time*60*1000;
            try {
                while(!pool.isShutdown()){
                    long distance = Calendar.getInstance().getTime().getTime() - startedTime;
                    if(distance>=TIME_SPAN){
                        //終了処理
                        end();
                        games.remove(hostplayer.getName());
                        break;
                    }

                    long m = new Double(Math.ceil(distance/(60*1000))).longValue();
                    long s = (distance - m*(60*1000))/1000;
                    System.out.println(m + "分"+ s+"秒経過");

                    if(m == time/2 && s == 0){
                        //半分経過
                        broadcast("残り"+m+"分です");
                    }else if( m == (time-1)){
                        //残り一分以内
                        if(s == 0){
                            //残り1分
                            broadcast("残り1分です");
                        }else if ( s == 30){
                            //残り30秒
                            broadcast("残り30秒です");
                        }else if ( s == 45){
                            //残り15秒
                            broadcast("残り15秒です");
                        }else if ( s >= 50){
                            //残り10秒カウントダウン
                            long t = 60 - s;
                            if(t == 0){
                                //終了処理
                                end();
                                games.remove(hostplayer.getName());
                                break;
                            }else{
                                //メッセージ
                                broadcast(Long.toString(t));
                            }
                        }
                    }


                    Thread.sleep(1000);

                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted - "
                        + Thread.currentThread().getId());
            }
            System.out.println("######################## end");
        }
    }

}
