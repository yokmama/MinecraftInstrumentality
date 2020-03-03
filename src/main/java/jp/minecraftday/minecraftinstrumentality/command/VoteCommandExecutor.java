package jp.minecraftday.minecraftinstrumentality.command;

import jp.minecraftday.minecraftinstrumentality.Main;
import jp.minecraftday.minecraftinstrumentality.Threading;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class VoteCommandExecutor implements CommandExecutor {
    final Main plugin;
    Timer theTimer = null;
    VoteTask currentTask = null;

    public VoteCommandExecutor(Main ref) {
        plugin = ref;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("vote") && args.length > 0 && sender instanceof Player) {
            String cmd0 = args[0].toLowerCase();
            if (cmd0.equals("jail") && args.length>1) {
                voteJail((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("mute") && args.length>1) {
                voteMute((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("unmute") && args.length>1) {
                voteUnMute((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("unjail") && args.length>1) {
                voteUnJail((Player) sender, Arrays.copyOfRange(args, 1, args.length));
            } else if (cmd0.equals("day")) {
                voteDay((Player) sender);
            } else if (cmd0.equals("night")) {
                voteNight((Player) sender);
            } else if (cmd0.equals("sun")) {
                voteSun((Player) sender);
            } else if (cmd0.equals("rain")) {
                voteRain((Player) sender);
            } else if (cmd0.equals("yes")) {
                voteYes((Player) sender);
            } else if (cmd0.equals("no")) {
                voteNo((Player) sender);
            }

            return true;
        }else if(command.getName().equalsIgnoreCase("voteyes") && sender instanceof Player){
            voteYes((Player) sender);
            return true;
        }else if(command.getName().equalsIgnoreCase("voteno") && sender instanceof Player){
            voteNo((Player) sender);
            return true;
        }
        return false;
    }

    private void voteDay(Player player) {
        vote(player, new VoteTask(player, null, "時間を朝にする") {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set day");

            }
        });
    }

    private void voteNight(Player player) {
        vote(player, new VoteTask(player, null, "時間を夜にする") {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set night");

            }
        });
    }

    private void voteSun(Player player) {
        vote(player, new VoteTask(player, null, "天気を晴れにする") {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:weather clear");

            }
        });
    }

    private void voteRain(Player player) {
        vote(player, new VoteTask(player, null, "天気を雨にする") {
            @Override
            void execute() {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:weather rain");

            }
        });
    }

    private void voteMute(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            vote(player, new VoteTask(player, target, args[0]+" をおくちチャックする", 3) {
                @Override
                void execute() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute "+args[0]);
                }
            });
        }else{
            player.sendMessage("対象のプレイヤーがいません");
        }
    }

    private void voteUnMute(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            vote(player, new VoteTask(player, target, args[0]+" をしゃべれるようにする") {
                @Override
                void execute() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unmute "+args[0]);
                }
            });
        }else{
            player.sendMessage("対象のプレイヤーがいません");
        }
    }

    private void voteJail(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            vote(player, new VoteTask(player, target, args[0]+" をろうやに入れる", 3) {
                @Override
                void execute() {
                    String jailname = plugin.getConfig().getString("vote.jailname");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "jail "+args[0] + " " + jailname + " 30m");
                }
            });
        }else{
            player.sendMessage("対象のプレイヤーがいません");
        }
    }

    private void voteUnJail(Player player, String[] args) {
        Player target = null;
        if(args.length>0){
            target = Bukkit.getPlayerExact(args[0]);
        }

        if(target!=null) {
            vote(player, new VoteTask(player, target, args[0]+" をろうやから出す") {
                @Override
                void execute() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unjail "+args[0]);
                }
            });
        }else{
            player.sendMessage("対象のプレイヤーがいません");
        }
    }

    private void voteYes(Player player) {
        if (currentTask != null && currentTask.isVotingRight(player)) {
            currentTask.numSupporters++;
            if (currentTask.isFinish()) {
                currentTask.run();
            }
        }
    }

    private void voteNo(Player player) {
        if (currentTask != null && currentTask.isVotingRight(player)) {
            currentTask.numOpponents++;
            if (currentTask.isFinish()) {
                currentTask.run();
            }
        }

    }

    private void vote(Player player, VoteTask task) {
        if (theTimer != null) {
            player.sendMessage("前の投票がおわっていません");
            return;
        }

        if(task.minimumRequired>0){
            if(player.getWorld().getPlayers().size()<task.minimumRequired){
                player.sendMessage("その投票は最低 " + task.minimumRequired + "人いないと始めれません");
                return;
            }
        }

        player.getWorld().getPlayers().forEach(p -> {
            if (!task.senderName.equals(p.getName()) && (task.targetName == null ||!task.targetName.equals(p.getName()))) {
                StringBuilder builder = new StringBuilder();
                builder.append("&c").append(player.getName()).append(" &6が「").append(task.question()).append("」の投票を始めました！\n");
                builder.append("いいよ！に票を入れるには&c/voteyes &6を使って投票しましょう！\n");
                builder.append("だめだよ！に票を入れるには &c/voteno &6を使って投票しましょう！\n");
                builder.append("&c120秒&6以内に答えてね！。 投票をしないといいよ！になるよ。");
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', builder.toString()));
                task.numVotersCount++;
            }
        });

        if (task.numVotersCount == 0) {
            task.run();
        } else {
            currentTask = task;
            theTimer = new Timer();
            theTimer.schedule(task, 1000 * 60 * 2);
        }
    }

    void sendMessage(Player p, String msg) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }


    abstract class VoteTask extends TimerTask {
        int numVotersCount = 0;
        int numSupporters = 0;
        int numOpponents = 0;
        int minimumRequired = 0;
        String senderName = null;
        String targetName = null;

        String question = "";

        VoteTask(Player player, Player target, String question) {
            senderName = player.getName();
            if (target != null) targetName = target.getName();
            this.question = question;
        }

        VoteTask(Player player, Player target, String question, int minimumRequired) {
            senderName = player.getName();
            this.minimumRequired = minimumRequired;
            if (target != null) targetName = target.getName();
            this.question = question;
        }

        String question() {
            return question;
        }

        boolean isFinish() {
            return numVotersCount == (numSupporters + numOpponents);
        }

        boolean isVotingRight(Player p) {
            if (p.getName().equals(senderName)) return false;
            if (targetName != null && p.getName().equals(targetName)) return false;

            return true;
        }

        abstract void execute();


        @Override
        public void run() {
            try {
                int fixNumSupporters = numSupporters + (numVotersCount - (numSupporters + numOpponents));
                boolean check = numVotersCount == 0 || ((double) fixNumSupporters / numVotersCount) > 0.8;
                Player player = Bukkit.getPlayer(senderName);
                if (check) {
                    player.getWorld().getPlayers().forEach(p -> sendMessage(p, "&6投票はいいね！にきまりました"));
                    Threading.postToServerThread(new Threading.Task(plugin) {
                        @Override
                        public void execute() {
                            VoteTask.this.execute();
                        }
                    });
                } else {
                    player.getWorld().getPlayers().forEach(p -> sendMessage(p, "&6投票はだめ！にきまりました"));
                }
            } finally {
                theTimer = null;
                currentTask = null;
            }
        }
    }

}
